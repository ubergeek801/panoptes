package org.slaq.slaqworx.panoptes;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueProvider;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;

/**
 * {@code TestUtil} provides common utilities to support Panoptes testing.
 *
 * @author jeremy
 */
public class TestUtil {
    public static final double EPSILON = 0.00001;

    // additional SecurityAttributes used by test cases
    public static final SecurityAttribute<Double> moovyRating =
            SecurityAttribute.of("Moovy", 16, Double.class, ValueProvider.forDouble());
    public static final SecurityAttribute<Double> npRating =
            SecurityAttribute.of("N&P", 17, Double.class, ValueProvider.forDouble());
    public static final SecurityAttribute<Double> fetchRating =
            SecurityAttribute.of("Fetch", 18, Double.class, ValueProvider.forDouble());

    private static final TestPortfolioProvider portfolioProvider = new TestPortfolioProvider();
    private static final TestPositionProvider positionProvider = new TestPositionProvider();
    private static final TestSecurityProvider securityProvider = new TestSecurityProvider();
    private static final TestRuleProvider ruleProvider = new TestRuleProvider();

    public static final Map<SecurityAttribute<?>, ? super Object> s1Attributes = Map.of(moovyRating,
            90d, npRating, 92d, fetchRating, 88d, SecurityAttribute.duration, 4d,
            SecurityAttribute.country, "US", SecurityAttribute.price, new BigDecimal("1.00"));
    public static final Security s1 = securityProvider.newSecurity("TestUtilS1", s1Attributes);

    public static final Map<SecurityAttribute<?>, ? super Object> s2Attributes = Map.of(moovyRating,
            85d, npRating, 78d, SecurityAttribute.duration, 4d, SecurityAttribute.country, "NZ",
            SecurityAttribute.price, new BigDecimal("1.00"));
    public static final Security s2 = securityProvider.newSecurity("TestUtilS2", s2Attributes);

    public static final Map<SecurityAttribute<?>, ? super Object> s3Attributes = Map.of(moovyRating,
            80d, npRating, 82d, SecurityAttribute.duration, 2.1d, SecurityAttribute.country, "CA",
            SecurityAttribute.price, new BigDecimal("1.00"));
    public static final Security s3 = securityProvider.newSecurity("TestUtilS3", s3Attributes);

    public static final Set<Position> p1Positions =
            Set.of(positionProvider.newPosition(null, 1000, s1),
                    positionProvider.newPosition(null, 500, s2));
    public static final Portfolio p1 = portfolioProvider.newPortfolio("TestUtilP1", "TestUtilP1",
            p1Positions, null, Collections.emptyList());

    public static final Set<Position> p2Positions =
            Set.of(positionProvider.newPosition(null, 500, s1),
                    positionProvider.newPosition(null, 1000, s2));
    public static final Portfolio p2 = portfolioProvider.newPortfolio("TestUtilP2", "TestUtilP2",
            p2Positions, null, Collections.emptyList());

    public static final Set<Position> p3Positions =
            Set.of(positionProvider.newPosition(null, 500, s1),
                    positionProvider.newPosition(null, 1000, s2),
                    positionProvider.newPosition(null, 200, s3));
    public static final Portfolio p3 = portfolioProvider.newPortfolio("TestUtilP3", "TestUtilP3",
            p3Positions, null, Collections.emptyList());

    public static final EvaluationContext defaultTestEvaluationContext =
            new EvaluationContext(securityProvider);

    /**
     * Creates and caches a {@code ConcentrationRule} with the given parameters.
     *
     * @param assetCache
     *            the {@code AssetCache} in which to cache the created {@code Rule}
     * @param key
     *            the unique key of this {@code Rule}, or {@code null} to generate one
     * @param description
     *            the {@code Rule} description
     * @param positionFilter
     *            the filter to be applied to {@code Position}s to determine concentration
     * @param lowerLimit
     *            the lower limit of acceptable concentration values
     * @param upperLimit
     *            the upper limit of acceptable concentration values
     * @param groupClassifier
     *            the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may
     *            also implement {@code GroupAggregator}
     */
    public static ConcentrationRule createTestConcentrationRule(AssetCache assetCache, RuleKey key,
            String description, Predicate<PositionEvaluationContext> positionFilter,
            Double lowerLimit, Double upperLimit, EvaluationGroupClassifier groupClassifier) {
        ConcentrationRule rule = new ConcentrationRule(key, description, positionFilter, lowerLimit,
                upperLimit, groupClassifier);
        assetCache.getRuleCache().put(rule.getKey(), rule);

        return rule;
    }

    /**
     * Creates and caches a {@code Portfolio} with the given key, name, {@code Position}s, benchmark
     * and {@code Rule}s.
     *
     * @param assetCache
     *            the {@code AssetCache} in which to cache the created {@code Portfolio}
     * @param id
     *            the ID to be used in the {@code Portfolio} key, or {@code null} to generate one
     * @param name
     *            the {@code Portfolio} name/description
     * @param positions
     *            the {@code Position}s comprising the {@code Portfolio}
     * @param benchmarkKey
     *            the (possibly null) {@code Portfolio} that acts a benchmark for the
     *            {@code Portfolio}
     * @param rules
     *            the (possibly empty) {@code Collection} of {@code Rule}s associated with the
     *            {@code Portfolio}
     */
    public static Portfolio createTestPortfolio(AssetCache assetCache, String id, String name,
            Set<? extends Position> positions, PortfolioKey benchmarkKey,
            Collection<? extends Rule> rules) {
        Portfolio portfolio =
                new Portfolio(new PortfolioKey(id, 1), name, positions, benchmarkKey, rules);
        assetCache.getPortfolioCache().put(portfolio.getKey(), portfolio);

        return portfolio;
    }

    /**
     * Creates and caches a {@code Position} with the given parameters.
     *
     * @param assetCache
     *            the {@code AssetCache} in which to cache the created {@code Position}
     * @param amount
     *            the amount held by the {@code Position}
     * @param security
     *            the {@code Security} held by the {@code Position}
     * @return the {@code Position} that was created
     */
    public static Position createTestPosition(AssetCache assetCache, double amount,
            Security security) {
        return createTestPosition(assetCache, amount, security.getKey());
    }

    /**
     * Creates and caches a {@code Position} with the given parameters.
     *
     * @param assetCache
     *            the {@code AssetCache} in which to cache the created {@code Position}
     * @param amount
     *            the amount held by the {@code Position}
     * @param securityKey
     *            the {@code SecurityKey} identifying the {@code Security} held by the
     *            {@code Position}
     * @return the {@code Position} that was created
     */
    public static Position createTestPosition(AssetCache assetCache, double amount,
            SecurityKey securityKey) {
        Position position = new Position(amount, securityKey);
        assetCache.getPositionCache().put(position.getKey(), position);

        return position;
    }

    /**
     * Creates and caches a {@code Security} with the given parameters.
     *
     * @param assetCache
     *            the {@code AssetCache} in which to cache the created {@code Security}
     * @param assetId
     *            the asset ID to assign to the {@code Security}; may be null iff attributes
     *            contains ISIN
     * @param attributes
     *            the additional attributes to associate with the {@code Security}
     * @return the {@code Security} that was created
     */
    public static Security createTestSecurity(AssetCache assetCache, String assetId,
            Map<SecurityAttribute<?>, Object> attributes) {
        Security security = TestUtil.testSecurityProvider().newSecurity(assetId, attributes);
        assetCache.getSecurityCache().put(security.getKey(), security);

        return security;
    }

    /**
     * Creates and caches a {@code Security} with the given parameters.
     *
     * @param assetCache
     *            the {@code AssetCache} in which to cache the created {@code Security}
     * @param assetId
     *            the asset ID to assign to the {@code Security}; may be null iff attributes
     *            contains ISIN
     * @param issuer
     *            the {@code Security} issuer
     * @param price
     *            the {@code Security} price
     * @return the {@code Security} that was created
     */
    public static Security createTestSecurity(AssetCache assetCache, String assetId, String issuer,
            BigDecimal price) {
        return createTestSecurity(assetCache, assetId,
                Map.of(SecurityAttribute.issuer, issuer, SecurityAttribute.price, price));
    }

    /**
     * Creates and caches a {@code WeightedAverageRule} with the given parameters.
     *
     * @param assetCache
     *            the {@code AssetCache} in which to cache the created {@code Rule}
     * @param key
     *            the unique key of this {@code Rule}, or {@code null} to generate one
     * @param description
     *            the {@code Rule} description
     * @param positionFilter
     *            the filter to be applied to {@code Position}s to determine concentration
     * @param calculationAttribute
     *            the {@code SecurityAttribute} on which to calculate
     * @param lowerLimit
     *            the lower limit of acceptable concentration values
     * @param upperLimit
     *            the upper limit of acceptable concentration values
     * @param groupClassifier
     *            the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may
     *            also implement {@code GroupAggregator}
     */
    public static WeightedAverageRule<Double> createTestWeightedAverageRule(AssetCache assetCache,
            RuleKey key, String description, Predicate<PositionEvaluationContext> positionFilter,
            SecurityAttribute<Double> calculationAttribute, Double lowerLimit, Double upperLimit,
            EvaluationGroupClassifier groupClassifier) {
        WeightedAverageRule<Double> rule = new WeightedAverageRule<>(key, description,
                positionFilter, calculationAttribute, lowerLimit, upperLimit, groupClassifier);
        assetCache.getRuleCache().put(rule.getKey(), rule);

        return rule;
    }

    /**
     * Obtains a {@code PortfolioProvider} suitable for unit testing using {@code Portfolio}s
     * created by {@code TestUtil}.
     *
     * @return a {@code TestPortfolioProvider}
     */
    public static TestPortfolioProvider testPortfolioProvider() {
        return portfolioProvider;
    }

    /**
     * Obtains a {@code PositionProvider} suitable for unit testing using {@code Position}s created
     * by {@code TestUtil}.
     *
     * @return a {@code TestPositionProvider}
     */
    public static TestPositionProvider testPositionProvider() {
        return positionProvider;
    }

    /**
     * Obtains a {@code RuleProvider} suitable for unit testing using {@code Rule}s created by
     * {@code TestUtil}.
     *
     * @return a {@code TestRuleProvider}
     */
    public static TestRuleProvider testRuleProvider() {
        return ruleProvider;
    }

    /**
     * Obtains a {@code SecurityProvider} suitable for unit testing using {@code Securities} created
     * by {@code TestUtil}.
     *
     * @return a {@code TestSecurityProvider}
     */
    public static TestSecurityProvider testSecurityProvider() {
        return securityProvider;
    }

    /**
     * Creates a new {@code TestUtil}. Restricted to enforce class utility semantics.
     */
    private TestUtil() {
        // nothing to do
    }
}
