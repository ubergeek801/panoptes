package org.slaq.slaqworx.panoptes.data;

import java.io.IOException;
import java.io.Serializable;
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

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.MapStore;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.GroovyPositionFilter;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.rule.SecurityAttributeGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.TopNSecurityAttributeAggregator;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;

/**
 * DummyPortfolioMapLoader is a MapStore that initializes the Hazelcast cache with random Portfolio
 * data. (For some reason a MapStore needs to be Serializable.)
 *
 * @author jeremy
 */
public class DummyPortfolioMapLoader
        implements MapStore<PortfolioKey, Portfolio>, RuleProvider, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(DummyPortfolioMapLoader.class);

    private final Portfolio[] benchmarks;
    private final Map<RuleKey, Rule> ruleMap = new HashMap<>();

    private transient final PimcoBenchmarkDataSource dataSource;

    private final GroovyPositionFilter currencyUsdFilter;
    private final GroovyPositionFilter currencyBrlFilter;
    private final GroovyPositionFilter duration3Filter;
    private final GroovyPositionFilter regionEmergingMarketFilter;

    /**
     * Creates a new DummyPortfolioMapLoader.
     *
     * @throws IOException
     *             if Porfolio data could not be loaded
     */
    public DummyPortfolioMapLoader() throws IOException {
        dataSource = PimcoBenchmarkDataSource.getInstance();

        benchmarks =
                new Portfolio[] { null, dataSource.getBenchmark(PimcoBenchmarkDataSource.EMAD_KEY),
                        dataSource.getBenchmark(PimcoBenchmarkDataSource.GLAD_KEY),
                        dataSource.getBenchmark(PimcoBenchmarkDataSource.ILAD_KEY),
                        dataSource.getBenchmark(PimcoBenchmarkDataSource.PGOV_KEY) };

        currencyUsdFilter = new GroovyPositionFilter("s.currency == \"USD\"");
        currencyBrlFilter = new GroovyPositionFilter("s.currency == \"BRL\"");
        duration3Filter = new GroovyPositionFilter("s.duration > 3.0");
        regionEmergingMarketFilter = new GroovyPositionFilter("s.region == \"Emerging Markets\"");
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
    public Rule getRule(RuleKey key) {
        return ruleMap.get(key);
    }

    @Override
    public Portfolio load(PortfolioKey key) {
        // if the key corresponds to a benchmark, return the corresponding benchmark
        Portfolio benchmark = dataSource.getBenchmark(key);
        if (benchmark != null) {
            return benchmark;
        }

        // otherwise generate a random Portfolio
        int seed;
        try {
            seed = Integer.parseInt(key.getId().substring(4));
        } catch (Exception e) {
            seed = 0;
        }
        Random random = new Random(seed);

        List<Security> securityList =
                Collections.unmodifiableList(new ArrayList<>(dataSource.getSecurityMap().values()));
        Set<Position> positions = generatePositions(securityList, random);
        Set<Rule> rules = generateRules(random);
        Portfolio portfolio =
                new Portfolio(key, "Test Portfolio " + RandomStringUtils.randomAlphabetic(40),
                        positions, benchmarks[random.nextInt(5)], rules);
        LOG.info("created Portfolio {} with {} Positions", key, portfolio.size());

        return portfolio;
    }

    @Override
    public Map<PortfolioKey, Portfolio> loadAll(Collection<PortfolioKey> keys) {
        LOG.info("loading Portfolios for {} keys", keys.size());
        return keys.stream().collect(Collectors.toMap(k -> k, k -> load(k)));
    }

    @Override
    public Iterable<PortfolioKey> loadAllKeys() {
        LOG.info("loading all keys");

        // This Iterable produces 504 Portfolio IDs; the first four are the PIMCO benchmarks and
        // the remaining 500 are randomly-generated Portfolios
        return () -> new Iterator<>() {
            private int currentPosition = -4;

            @Override
            public boolean hasNext() {
                return currentPosition < 500;
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
     * Generates a random set of Positions from the given Securities.
     *
     * @param securities
     *            a List from which to source Securities
     * @param random
     *            the random number generator to use
     * @return a new Set of random Positions
     */
    protected Set<Position> generatePositions(List<Security> securities, Random random) {
        ArrayList<Security> securitiesCopy = new ArrayList<>(securities);
        // generate between 1000 and 2000 positions
        int numPositions = 1000 + random.nextInt(1001);
        HashSet<Position> positions = new HashSet<>(numPositions * 2);
        for (int i = 0; i < numPositions; i++) {
            // generate an amount in the approximate range of 1_000.00 ~ 10_000_000.00
            double amount =
                    (long)((1000 + (Math.pow(10, 3 + random.nextInt(6)) * random.nextDouble()))
                            * 100) / 100d;
            // use a given security at most once
            Security security = securitiesCopy.remove(random.nextInt(securitiesCopy.size()));
            positions.add(new MaterializedPosition(amount, security.getKey()));
        }

        return positions;
    }

    /**
     * Generates a random set of Rules.
     *
     * @param random
     *            the random number generator to use
     * @return a new Set of random Rules
     */
    protected Set<Rule> generateRules(Random random) {
        HashSet<Rule> rules = new HashSet<>(400);

        for (int i = 1; i <= 200; i++) {
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
                compareAttribute = TestUtil.ratingValue;
                break;
            default:
                compareAttribute = TestUtil.duration;
            }

            EvaluationGroupClassifier groupClassifier;
            switch (random.nextInt(9)) {
            case 0:
                groupClassifier = new SecurityAttributeGroupClassifier(TestUtil.currency);
                break;
            case 1:
                // description is a proxy for issuer
                groupClassifier = new SecurityAttributeGroupClassifier(TestUtil.description);
                break;
            case 2:
                groupClassifier = new SecurityAttributeGroupClassifier(TestUtil.region);
                break;
            case 3:
                groupClassifier = new SecurityAttributeGroupClassifier(TestUtil.country);
                break;
            case 4:
                groupClassifier = new TopNSecurityAttributeAggregator(TestUtil.currency, 5);
                break;
            case 5:
                groupClassifier = new TopNSecurityAttributeAggregator(TestUtil.description, 5);
                break;
            case 6:
                groupClassifier = new TopNSecurityAttributeAggregator(TestUtil.region, 5);
                break;
            case 7:
                groupClassifier = new TopNSecurityAttributeAggregator(TestUtil.country, 5);
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
