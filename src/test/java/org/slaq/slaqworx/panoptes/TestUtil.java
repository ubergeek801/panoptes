package org.slaq.slaqworx.panoptes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * {@code TestUtil} provides common utilities to support Panoptes testing.
 *
 * @author jeremy
 */
public class TestUtil {
    public static final double EPSILON = 0.00001;

    // additional SecurityAttributes used by test cases
    public static final SecurityAttribute<Double> moovyRating =
            SecurityAttribute.of("Moovy", 15, Double.class);
    public static final SecurityAttribute<Double> npRating =
            SecurityAttribute.of("N&P", 16, Double.class);
    public static final SecurityAttribute<Double> fetchRating =
            SecurityAttribute.of("Fetch", 17, Double.class);

    private static final TestPortfolioProvider portfolioProvider = new TestPortfolioProvider();
    private static final TestPositionProvider positionProvider = new TestPositionProvider();
    private static final TestSecurityProvider securityProvider = new TestSecurityProvider();
    private static final TestRuleProvider ruleProvider = new TestRuleProvider();

    public static final Map<SecurityAttribute<?>, ? super Object> s1Attributes = Map.of(moovyRating,
            90d, npRating, 92d, fetchRating, 88d, SecurityAttribute.duration, 4d,
            SecurityAttribute.country, "US", SecurityAttribute.price, new BigDecimal("1.00"));
    public static final Security s1 = securityProvider.newSecurity(s1Attributes);

    public static final Map<SecurityAttribute<?>, ? super Object> s2Attributes = Map.of(moovyRating,
            85d, npRating, 78d, SecurityAttribute.duration, 4d, SecurityAttribute.country, "NZ",
            SecurityAttribute.price, new BigDecimal("1.00"));
    public static final Security s2 = securityProvider.newSecurity(s2Attributes);

    public static final Map<SecurityAttribute<?>, ? super Object> s3Attributes = Map.of(moovyRating,
            80d, npRating, 82d, SecurityAttribute.duration, 2.1d, SecurityAttribute.country, "CA",
            SecurityAttribute.price, new BigDecimal("1.00"));
    public static final Security s3 = securityProvider.newSecurity(s3Attributes);

    public static final Set<Position> p1Positions =
            Set.of(positionProvider.newPosition(null, 1000, s1),
                    positionProvider.newPosition(null, 500, s2));

    public static final Portfolio p1 =
            new Portfolio(new PortfolioKey("TestUtilP1", 1), "TestUtilP1", p1Positions);
    public static final Set<Position> p2Positions =
            Set.of(positionProvider.newPosition(null, 500, s1),
                    positionProvider.newPosition(null, 1000, s2));

    public static final Portfolio p2 =
            new Portfolio(new PortfolioKey("TestUtilP2", 1), "TestUtilP2", p2Positions);

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
