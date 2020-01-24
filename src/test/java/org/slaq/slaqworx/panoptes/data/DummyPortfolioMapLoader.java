package org.slaq.slaqworx.panoptes.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.hazelcast.map.MapStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EligibilityListRule;
import org.slaq.slaqworx.panoptes.rule.EligibilityListRule.EligibilityListType;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.GroovyPositionFilter;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.rule.TopNSecurityAttributeAggregator;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;

/**
 * {@code DummyPortfolioMapLoader} is a {@code MapStore} that initializes the Hazelcast cache with
 * random {@code Portfolio} data.
 *
 * @author jeremy
 */
public class DummyPortfolioMapLoader
        implements MapStore<PortfolioKey, Portfolio>, RuleProvider, PortfolioProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DummyPortfolioMapLoader.class);

    private static final String PORTFOLIO_NAMES_FILE = "portfolionames.txt";

    private static final int NUM_PORTFOLIOS = 500;
    private static final int MIN_POSITIONS = 1000;
    private static final int MAX_ADDITIONAL_POSITIONS = 1000;

    private final Portfolio[] benchmarks;
    private final ArrayList<String> portfolioNames;
    private final HashMap<PortfolioKey, Portfolio> portfolioMap = new HashMap<>();
    private final HashMap<RuleKey, ConfigurableRule> ruleMap = new HashMap<>();

    private final PimcoBenchmarkDataSource dataSource;

    private final GroovyPositionFilter belowInvestmentGradeFilter =
            GroovyPositionFilter.of("s.rating1Value < 70");
    private final GroovyPositionFilter regionEmergingMarketFilter =
            GroovyPositionFilter.of("s.region == \"Emerging Markets\"");
    private final GroovyPositionFilter nonUsInternalBondFilter =
            GroovyPositionFilter.of("s.country != \"US\" && s.sector == \"Internal Bond\"");
    private final EvaluationGroupClassifier top5IssuerClassifier =
            new TopNSecurityAttributeAggregator(SecurityAttribute.issuer, 5);

    private int portfolioIndex;

    /**
     * Creates a new {@code DummyPortfolioMapLoader}.
     *
     * @throws IOException
     *             if {@code Porfolio} data could not be loaded
     */
    public DummyPortfolioMapLoader() throws IOException {
        dataSource = PimcoBenchmarkDataSource.getInstance();

        benchmarks =
                new Portfolio[] { null, dataSource.getBenchmark(PimcoBenchmarkDataSource.EMAD_KEY),
                        dataSource.getBenchmark(PimcoBenchmarkDataSource.GLAD_KEY),
                        dataSource.getBenchmark(PimcoBenchmarkDataSource.ILAD_KEY),
                        dataSource.getBenchmark(PimcoBenchmarkDataSource.PGOV_KEY) };

        portfolioNames = new ArrayList<>(1000);
        try (BufferedReader portfolioNameReader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(PORTFOLIO_NAMES_FILE)))) {
            String portfolioName;
            while ((portfolioName = portfolioNameReader.readLine()) != null) {
                portfolioNames.add(portfolioName);
            }
        }
    }

    @Override
    public void delete(PortfolioKey key) {
        // nothing to do
    }

    @Override
    public void deleteAll(Collection<PortfolioKey> keys) {
        // nothing to do
    }

    @Override
    public Portfolio getPortfolio(PortfolioKey key) {
        return portfolioMap.get(key);
    }

    @Override
    public ConfigurableRule getRule(RuleKey key) {
        return ruleMap.get(key);
    }

    @Override
    public Portfolio load(PortfolioKey key) {
        return portfolioMap.computeIfAbsent(key, k -> {
            // if the key corresponds to a benchmark, return the corresponding benchmark
            Portfolio benchmark = dataSource.getBenchmark(k);
            if (benchmark != null) {
                return benchmark;
            }

            // otherwise generate a random Portfolio
            int seed;
            try {
                seed = Integer.parseInt(k.getId().substring(4));
            } catch (Exception e) {
                seed = 0;
            }
            Random random = new Random(seed);

            List<Security> securityList = Collections
                    .unmodifiableList(new ArrayList<>(dataSource.getSecurityMap().values()));
            Set<Position> positions = generatePositions(securityList, random);
            Portfolio portfolioBenchmark = benchmarks[random.nextInt(5)];
            Set<ConfigurableRule> rules = generateRules(random, portfolioBenchmark != null);
            Portfolio portfolio = new Portfolio(k, portfolioNames.get(portfolioIndex++), positions,
                    portfolioBenchmark, rules);
            LOG.info("created Portfolio {} with {} Positions", k, portfolio.size());

            return portfolio;
        });
    }

    @Override
    public Map<PortfolioKey, Portfolio> loadAll(Collection<PortfolioKey> keys) {
        LOG.info("loading Portfolios for {} keys", keys.size());
        return keys.stream().collect(Collectors.toMap(k -> k, k -> load(k)));
    }

    @Override
    public Iterable<PortfolioKey> loadAllKeys() {
        LOG.info("loading all keys");

        // This Iterable produces NUM_PORTFOLIOS + 4 Portfolio IDs; the first four are the PIMCO
        // benchmarks and the remaining NUM_PORTFOLIOS are randomly-generated Portfolios
        return () -> new Iterator<>() {
            private int currentPosition = -4;

            @Override
            public boolean hasNext() {
                return currentPosition < NUM_PORTFOLIOS;
            }

            @Override
            public PortfolioKey next() {
                switch (++currentPosition) {
                case -3:
                    return PimcoBenchmarkDataSource.EMAD_KEY;
                case -2:
                    return PimcoBenchmarkDataSource.GLAD_KEY;
                case -1:
                    return PimcoBenchmarkDataSource.ILAD_KEY;
                case 0:
                    return PimcoBenchmarkDataSource.PGOV_KEY;
                default:
                    return new PortfolioKey("test" + currentPosition, 1);
                }
            }
        };
    }

    @Override
    public void store(PortfolioKey key, Portfolio value) {
        // nothing to do
    }

    @Override
    public void storeAll(Map<PortfolioKey, Portfolio> map) {
        // nothing to do
    }

    /**
     * Generates a random set of {@code Position}s from the given {@code Securities}.
     *
     * @param securities
     *            a {@code List} from which to source {@code Securities}
     * @param random
     *            the random number generator to use
     * @return a new {@code Set} of random {@code Positions}
     */
    protected Set<Position> generatePositions(List<Security> securities, Random random) {
        ArrayList<Security> securitiesCopy = new ArrayList<>(securities);
        // generate between MIN_POSITIONS and MIN_POSITIONS + MAX_ADDITIONAL_POSITIONS positions
        int numPositions = MIN_POSITIONS + random.nextInt(MAX_ADDITIONAL_POSITIONS + 1);
        HashSet<Position> positions = new HashSet<>(numPositions * 2);
        for (int i = 0; i < numPositions; i++) {
            // generate an amount in the approximate range of 100.00 ~ 10_000.00
            double amount =
                    100 + (long)(Math.pow(10, 2 + random.nextInt(3)) * random.nextDouble() * 100)
                            / 100d;
            // use a given Security at most once
            Security security = securitiesCopy.remove(random.nextInt(securitiesCopy.size()));
            positions.add(new Position(amount, security));
        }

        return positions;
    }

    /**
     * Generates a random-ish set of {@code Rule}s.
     *
     * @param random
     *            the random number generator to use
     * @param hasBenchmark
     *            {@code true} if the target {@code Portfolio} has an associated benchmark,
     *            {@code false} otherwise
     * @return a new {@code Set} of random {@code Rule}s
     */
    protected Set<ConfigurableRule> generateRules(Random random, boolean hasBenchmark) {
        HashSet<ConfigurableRule> rules = new HashSet<>(60);

        // with high probability, only investment-grade Securities will be permitted (disallow
        // rating < 70)
        double rand = random.nextDouble();
        if (rand < 0.9) {
            rules.add(new EligibilityListRule(null, "Investment-Grade Only",
                    belowInvestmentGradeFilter, EligibilityListType.WHITELIST,
                    SecurityAttribute.isin, Collections.emptySet()));
        }

        // with high probability, a minimum level of average quality will be required
        rand = random.nextDouble();
        if (hasBenchmark) {
            if (rand < 0.5) {
                // require average quality somewhat close to the benchmark
                rules.add(new WeightedAverageRule(null, "Average Quality >= 85% of Benchmark", null,
                        SecurityAttribute.rating1Value, 0.85, null, null));
            } else if (rand < 0.8) {
                // require average quality somewhat closer to the benchmark
                rules.add(new WeightedAverageRule(null, "Average Quality >= 90% of Benchmark", null,
                        SecurityAttribute.rating1Value, 0.90, null, null));
            }
        } else {
            if (rand < 0.5) {
                // require somewhat high average quality
                rules.add(new WeightedAverageRule(null, "Average Quality >= A3", null,
                        SecurityAttribute.rating1Value, 79d, null, null));
            } else if (rand < 0.8) {
                // require somewhat higher average quality
                rules.add(new WeightedAverageRule(null, "Average Quality >= A1", null,
                        SecurityAttribute.rating1Value, 85d, null, null));
            }
        }

        // with moderate probability, Emerging Markets will be limited or disallowed entirely
        rand = random.nextDouble();
        if (rand < 0.1) {
            // disallow Emerging Markets altogether
            rules.add(new EligibilityListRule(null, "No Emerging Markets", null,
                    EligibilityListType.BLACKLIST, SecurityAttribute.region,
                    Set.of("Emerging Markets")));
        } else if (rand < 0.3) {
            if (hasBenchmark) {
                // permit Emerging Markets relative to the benchmark
                rules.add(new ConcentrationRule(null, "Emerging Markets <= 120% of Benchmark",
                        regionEmergingMarketFilter, null, 1.2, null));
            } else {
                // permit a limited concentration in Emerging Benchmarks
                if (random.nextBoolean()) {
                    // permit a little
                    rules.add(new ConcentrationRule(null, "Emerging Markets <= 10% of Portfolio",
                            regionEmergingMarketFilter, null, 0.1, null));
                } else {
                    // permit a little more
                    rules.add(new ConcentrationRule(null, "Emerging Markets <= 20% of Portfolio",
                            regionEmergingMarketFilter, null, 0.2, null));
                }
            }
        }

        // with moderate probability, for Portfolios with a benchmark, concentrations in the top 5
        // issuers will be restricted relative to the benchmark
        rand = random.nextDouble();
        if (hasBenchmark) {
            if (rand < 0.1) {
                rules.add(new ConcentrationRule(null,
                        "Top 5 Issuer Concentrations Within 20% of Benchmark", null, 0.8, 1.2,
                        top5IssuerClassifier));
            } else if (rand < 0.3) {
                rules.add(new ConcentrationRule(null,
                        "Top 5 Issuer Concentrations Within 30% of Benchmark", null, 0.7, 1.3,
                        top5IssuerClassifier));
            }
        }

        // with low probability, Anheuser-Busch issues will be disallowed
        rand = random.nextDouble();
        if (rand < 0.2) {
            rules.add(new EligibilityListRule(null, "No Anheuser-Busch Issues", null,
                    EligibilityListType.BLACKLIST, SecurityAttribute.issuer,
                    Set.of("Anheuser-Busch")));
        }

        // with moderate probability, issues in certain currencies will be disallowed
        rand = random.nextDouble();
        if (rand < 0.5) {
            rules.add(new EligibilityListRule(null, "No PLN-, RON- or RUB-Denominated Issues", null,
                    EligibilityListType.BLACKLIST, SecurityAttribute.currency,
                    Set.of("PLN", "RON", "RUB")));
        }

        // with moderate probability, non-US internal bonds will be disallowed
        rand = random.nextDouble();
        if (rand < 0.5) {
            rules.add(new EligibilityListRule(null, "No Non-US Internal Bonds",
                    nonUsInternalBondFilter, EligibilityListType.WHITELIST, SecurityAttribute.isin,
                    Collections.emptySet()));
        }

        ruleMap.putAll(rules.stream().collect(Collectors.toMap(r -> r.getKey(), r -> r)));
        return rules;
    }
}
