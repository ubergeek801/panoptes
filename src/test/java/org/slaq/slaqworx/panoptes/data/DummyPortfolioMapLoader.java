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
import java.util.function.Predicate;
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
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.GroovyPositionFilter;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.rule.SecurityAttributeGroupClassifier;
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
    private static final int NUM_RULES = 30;

    private final Portfolio[] benchmarks;
    private final ArrayList<String> portfolioNames;
    private final HashMap<PortfolioKey, Portfolio> portfolioMap = new HashMap<>();
    private final HashMap<RuleKey, ConfigurableRule> ruleMap = new HashMap<>();

    private final PimcoBenchmarkDataSource dataSource;

    private final GroovyPositionFilter currencyUsdFilter;
    private final GroovyPositionFilter currencyBrlFilter;
    private final GroovyPositionFilter duration3Filter;
    private final GroovyPositionFilter regionEmergingMarketFilter;

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

        currencyUsdFilter = GroovyPositionFilter.of("s.currency == \"USD\"");
        currencyBrlFilter = GroovyPositionFilter.of("s.currency == \"BRL\"");
        duration3Filter = GroovyPositionFilter.of("s.duration > 3.0");
        regionEmergingMarketFilter = GroovyPositionFilter.of("s.region == \"Emerging Markets\"");

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
            Set<ConfigurableRule> rules = generateRules(random);
            Portfolio portfolio = new Portfolio(k, portfolioNames.get(portfolioIndex++), positions,
                    benchmarks[random.nextInt(5)], rules);
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
     * Generates a random set of {@code Rule}s.
     *
     * @param random
     *            the random number generator to use
     * @return a new {@code Set} of random {@code Rule}s
     */
    protected Set<ConfigurableRule> generateRules(Random random) {
        HashSet<ConfigurableRule> rules = new HashSet<>(NUM_RULES * 2);

        for (int i = 1; i <= NUM_RULES; i++) {
            Predicate<PositionEvaluationContext> filter = null;
            SecurityAttribute<Double> compareAttribute = null;
            switch (random.nextInt(6)) {
            case 0:
                filter = currencyUsdFilter;
                break;
            case 1:
                filter = currencyBrlFilter;
                break;
            case 2:
                filter = duration3Filter;
                break;
            case 3:
                filter = regionEmergingMarketFilter;
                break;
            case 4:
                compareAttribute = SecurityAttribute.rating1Value;
                break;
            default:
                compareAttribute = SecurityAttribute.duration;
            }

            EvaluationGroupClassifier groupClassifier;
            switch (random.nextInt(9)) {
            case 0:
                groupClassifier = new SecurityAttributeGroupClassifier(SecurityAttribute.currency);
                break;
            case 1:
                // description is a proxy for issuer
                groupClassifier =
                        new SecurityAttributeGroupClassifier(SecurityAttribute.description);
                break;
            case 2:
                groupClassifier = new SecurityAttributeGroupClassifier(SecurityAttribute.region);
                break;
            case 3:
                groupClassifier = new SecurityAttributeGroupClassifier(SecurityAttribute.country);
                break;
            case 4:
                groupClassifier =
                        new TopNSecurityAttributeAggregator(SecurityAttribute.currency, 5);
                break;
            case 5:
                groupClassifier =
                        new TopNSecurityAttributeAggregator(SecurityAttribute.description, 5);
                break;
            case 6:
                groupClassifier = new TopNSecurityAttributeAggregator(SecurityAttribute.region, 5);
                break;
            case 7:
                groupClassifier = new TopNSecurityAttributeAggregator(SecurityAttribute.country, 5);
                break;
            default:
                groupClassifier = null;
            }

            if (filter != null) {
                rules.add(new ConcentrationRule(null, "ConcentrationRule " + i, filter, 0.8, 1.2,
                        groupClassifier));
            } else if (compareAttribute != null) {
                rules.add(new WeightedAverageRule(null, "WeightedAverageRule " + i, null,
                        compareAttribute, 0.8, 1.2, groupClassifier));
            }
        }

        ruleMap.putAll(rules.stream().collect(Collectors.toMap(r -> r.getKey(), r -> r)));
        return rules;
    }
}
