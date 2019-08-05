package org.slaq.slaqworx.panoptes;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    public static SecurityAttribute<String> cusip = SecurityAttribute.of("cusip", 0, String.class);
    public static SecurityAttribute<String> isin = SecurityAttribute.of("isin", 1, String.class);
    public static SecurityAttribute<String> description =
            SecurityAttribute.of("description", 2, String.class);
    public static SecurityAttribute<String> country =
            SecurityAttribute.of("country", 3, String.class);
    public static SecurityAttribute<String> region =
            SecurityAttribute.of("region", 4, String.class);
    public static SecurityAttribute<String> sector =
            SecurityAttribute.of("sector", 5, String.class);
    public static SecurityAttribute<String> currency =
            SecurityAttribute.of("currency", 6, String.class);
    public static SecurityAttribute<BigDecimal> coupon =
            SecurityAttribute.of("coupon", 7, BigDecimal.class);
    public static SecurityAttribute<LocalDate> maturityDate =
            SecurityAttribute.of("maturityDate", 8, LocalDate.class);
    public static SecurityAttribute<String> ratingSymbol =
            SecurityAttribute.of("ratingSymbol", 9, String.class);
    public static SecurityAttribute<Double> ratingValue =
            SecurityAttribute.of("ratingValue", 10, Double.class);
    public static SecurityAttribute<BigDecimal> yield =
            SecurityAttribute.of("yield", 11, BigDecimal.class);
    public static SecurityAttribute<Double> duration =
            SecurityAttribute.of("duration", 12, Double.class);

    public static SecurityAttribute<String> issuer =
            SecurityAttribute.of("issuer", 13, String.class);
    public static final SecurityAttribute<Double> moovyRating =
            SecurityAttribute.of("Moovy", 14, Double.class);
    public static final SecurityAttribute<Double> npRating =
            SecurityAttribute.of("N&P", 15, Double.class);

    public static final SecurityAttribute<Double> fetchRating =
            SecurityAttribute.of("Fetch", 16, Double.class);

    private static final TestSecurityProvider securityProvider = new TestSecurityProvider();
    private static final TestPositionProvider positionProvider = new TestPositionProvider();

    public static final Map<SecurityAttribute<?>, ? super Object> s1Attributes =
            Map.of(moovyRating, 90d, npRating, 92d, fetchRating, 88d, duration, 4d, country, "US");

    public static final Security s1 = securityProvider.newSecurity(s1Attributes);
    public static final Map<SecurityAttribute<?>, ? super Object> s2Attributes =
            Map.of(moovyRating, 85d, npRating, 78d, duration, 4d, country, "NZ");

    public static final Security s2 = securityProvider.newSecurity(s2Attributes);
    public static final Set<Position> p1Positions =
            Set.of(positionProvider.newPosition(null, 1000, s1.getKey()),
                    positionProvider.newPosition(null, 500, s2.getKey()));

    public static final Portfolio p1 =
            new Portfolio(new PortfolioKey("TestUtilP1", 1), "TestUtilP1", p1Positions);
    public static final Set<Position> p2Positions =
            Set.of(positionProvider.newPosition(null, 500, s1.getKey()),
                    positionProvider.newPosition(null, 1000, s2.getKey()));

    public static final Portfolio p2 =
            new Portfolio(new PortfolioKey("TestUtilP2", 1), "TestUtilP2", p2Positions);

    /**
     * Obtains a PositionProvider suitable for unit testing using Positions created by TestUtil.
     *
     * @return a SecurityProvider
     */
    public static TestPositionProvider testPositionProvider() {
        return positionProvider;
    }

    /**
     * Obtains a SecurityProvider suitable for unit testing using Securities created by TestUtil.
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
