package org.slaq.slaqworx.panoptes;

import java.util.Map;
import java.util.Set;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * TestUtil provides common utilities to support Panoptes testing.
 *
 * @author jeremy
 */
public class TestUtil {
    public static final double EPSILON = 0.00001;

    private static final TestSecurityProvider securityProvider = new TestSecurityProvider();

    public static final SecurityAttribute<Double> moovyRating =
            SecurityAttribute.of("Moovy", 13, Double.class);
    public static final SecurityAttribute<Double> npRating =
            SecurityAttribute.of("N&P", 14, Double.class);
    public static final SecurityAttribute<Double> fetchRating =
            SecurityAttribute.of("Fetch", 15, Double.class);

    public static final Map<SecurityAttribute<?>, ? super Object> s1Attributes = Map.of(moovyRating,
            90d, npRating, 92d, fetchRating, 88d, SecurityAttribute.duration, 4d);
    public static final Security s1 = securityProvider.newSecurity("TestUtilS1", s1Attributes);

    public static final Map<SecurityAttribute<?>, ? super Object> s2Attributes =
            Map.of(moovyRating, 85d, npRating, 78d, SecurityAttribute.duration, 4d);
    public static final Security s2 = securityProvider.newSecurity("TestUtilS2", s2Attributes);

    public static final Set<Position> p1Positions =
            Set.of(new Position(1000, s1), new Position(500, s2));
    public static final Portfolio p1 =
            new Portfolio(new PortfolioKey("TestUtilP1", 1), p1Positions);

    public static final Set<Position> p2Positions =
            Set.of(new Position(500, s1), new Position(1000, s2));
    public static final Portfolio p2 =
            new Portfolio(new PortfolioKey("TestUtilP2", 1), p2Positions);

    /**
     * Obtains a SecurityProvider suitable for unit testing.
     *
     * @return a SecurityProvider
     */
    public static TestSecurityProvider testSecurityProvider() {
        return securityProvider;
    }

    /**
     * Creates a new TestUtil. Restricted to enforce class utility semantics.
     */
    private TestUtil() {
        // nothing to do
    }
}
