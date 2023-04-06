package org.slaq.slaqworx.panoptes.test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.slaq.slaqworx.panoptes.asset.EligibilityListProvider;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.ValueProvider;
import org.slaq.slaqworx.panoptes.trade.TaxLot;

/**
 * Provides common utilities to support Panoptes testing.
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
  public static final Map<SecurityAttribute<?>, ? super Object> s1Attributes =
      SecurityAttribute.mapOf(
          moovyRating,
          90d,
          npRating,
          92d,
          fetchRating,
          88d,
          SecurityAttribute.duration,
          4d,
          SecurityAttribute.country,
          "US",
          SecurityAttribute.price,
          1d);
  public static final Map<SecurityAttribute<?>, ? super Object> s2Attributes =
      SecurityAttribute.mapOf(
          moovyRating,
          85d,
          npRating,
          78d,
          SecurityAttribute.duration,
          4d,
          SecurityAttribute.country,
          "NZ",
          SecurityAttribute.price,
          1d);
  public static final Map<SecurityAttribute<?>, ? super Object> s3Attributes =
      SecurityAttribute.mapOf(
          moovyRating,
          80d,
          npRating,
          82d,
          SecurityAttribute.duration,
          2.1d,
          SecurityAttribute.country,
          "CA",
          SecurityAttribute.price,
          1d);
  private static final TestEligibilityListProvider eligibilityListProvider =
      new TestEligibilityListProvider();
  private static final TestPortfolioProvider portfolioProvider = new TestPortfolioProvider();
  private static final TestPositionProvider positionProvider = new TestPositionProvider();
  private static final TestSecurityProvider securityProvider = new TestSecurityProvider();
  public static final Security s1 = securityProvider.newSecurity("TestUtilS1", s1Attributes);
  public static final Security s2 = securityProvider.newSecurity("TestUtilS2", s2Attributes);
  public static final Set<Position> p1Positions =
      Set.of(
          positionProvider.newPosition(null, 1000, s1),
          positionProvider.newPosition(null, 500, s2));
  public static final Portfolio p1 =
      portfolioProvider.newPortfolio(
          "TestUtilP1", "TestUtilP1", p1Positions, null, Collections.emptyList());
  public static final Set<Position> p2Positions =
      Set.of(
          positionProvider.newPosition(null, 500, s1),
          positionProvider.newPosition(null, 1000, s2));
  public static final Portfolio p2 =
      portfolioProvider.newPortfolio(
          "TestUtilP2", "TestUtilP2", p2Positions, null, Collections.emptyList());
  public static final Security s3 = securityProvider.newSecurity("TestUtilS3", s3Attributes);
  public static final Set<Position> p3Positions =
      Set.of(
          positionProvider.newPosition(null, 500, s1),
          positionProvider.newPosition(null, 1000, s2),
          positionProvider.newPosition(null, 200, s3));
  public static final Portfolio p3 =
      portfolioProvider.newPortfolio(
          "TestUtilP3", "TestUtilP3", p3Positions, null, Collections.emptyList());

  /** Creates a new {@link TestUtil}. Restricted to enforce class utility semantics. */
  private TestUtil() {
    // nothing to do
  }

  /**
   * Caches the given eligibility list.
   *
   * @param name the name identifying the eligibility list
   * @param members the eligibility list members
   * @return the eligibility list members
   */
  public static Set<String> createTestEligibilityList(String name, Set<String> members) {
    return eligibilityListProvider.newEligibilityList(name, members);
  }

  /**
   * Creates and caches a {@link Portfolio} with the given key, name, {@link Position}s, benchmark
   * and {@link Rule}s.
   *
   * @param assetCache the {@link AssetCache} in which to cache the created {@link Portfolio}
   * @param id the ID to be used in the {@link Portfolio} key, or {@code null} to generate one
   * @param name the {@link Portfolio} name/description
   * @param positions the {@link Position}s comprising the {@link Portfolio}
   * @param benchmarkKey the (possibly {@code null}) {@link Portfolio} that acts a benchmark for the
   *     {@link Portfolio}
   * @param rules the (possibly empty) {@link Collection} of {@link Rule}s associated with the
   *     {@link Portfolio}
   * @return a {@link Portfolio} with the specified configuration
   */
  public static Portfolio createTestPortfolio(
      AssetCache assetCache,
      String id,
      String name,
      Set<Position> positions,
      PortfolioKey benchmarkKey,
      Collection<? extends Rule> rules) {
    Portfolio portfolio =
        new Portfolio(new PortfolioKey(id, 1), name, positions, benchmarkKey, rules);
    assetCache.getPortfolioCache().set(portfolio.getKey(), portfolio);

    return portfolio;
  }

  /**
   * Creates and caches a {@link Position} with the given parameters.
   *
   * @param assetCache the {@link AssetCache} in which to cache the created {@link Position}
   * @param amount the amount held by the {@link Position}
   * @param security the {@link Security} held by the {@link Position}
   * @return the {@link Position} that was created
   */
  public static Position createTestPosition(
      AssetCache assetCache, double amount, Security security) {
    return createTestPosition(assetCache, amount, security.getKey());
  }

  /**
   * Creates and caches a {@link Position} with the given parameters.
   *
   * @param assetCache the {@link AssetCache} in which to cache the created {@link Position}
   * @param amount the amount held by the {@link Position}
   * @param securityKey the {@link SecurityKey} identifying the {@link Security} held by the {@link
   *     Position}
   * @return the {@link Position} that was created
   */
  public static Position createTestPosition(
      AssetCache assetCache, double amount, SecurityKey securityKey) {
    Position position = new SimplePosition(amount, securityKey);
    assetCache.getPositionCache().set(position.getKey(), position);

    return position;
  }

  /**
   * Creates and caches a {@link Security} with the given parameters.
   *
   * @param assetCache the {@link AssetCache} in which to cache the created {@link Security}
   * @param assetId the asset ID to assign to the {@link Security}; may be {@code null} iff
   *     attributes contains ISIN
   * @param attributes the additional attributes to associate with the {@link Security}
   * @return the {@link Security} that was created
   */
  public static Security createTestSecurity(
      AssetCache assetCache, String assetId, Map<SecurityAttribute<?>, Object> attributes) {
    Security security = TestUtil.testSecurityProvider().newSecurity(assetId, attributes);
    assetCache.getSecurityCache().set(security.getKey(), security);

    return security;
  }

  /**
   * Creates and caches a {@link Security} with the given parameters.
   *
   * @param assetCache the {@link AssetCache} in which to cache the created {@link Security}
   * @param assetId the asset ID to assign to the {@link Security}; may be {@code null} iff
   *     attributes contains ISIN
   * @param issuer the {@link Security} issuer
   * @param price the {@link Security} price
   * @return the {@link Security} that was created
   */
  public static Security createTestSecurity(
      AssetCache assetCache, String assetId, String issuer, double price) {
    return createTestSecurity(
        assetCache,
        assetId,
        SecurityAttribute.mapOf(SecurityAttribute.issuer, issuer, SecurityAttribute.price, price));
  }

  /**
   * Creates a new {@link TaxLot} with the given parameters.
   *
   * @param amount the amount held by the {@link TaxLot}
   * @param security the {@link Security} held by the {@link TaxLot}
   * @return the {@link TaxLot} that was created
   */
  public static TaxLot createTestTaxLot(double amount, Security security) {
    return createTestTaxLot(amount, security.getKey());
  }

  /**
   * Creates a new {@link TaxLot} with the given parameters.
   *
   * @param amount the amount held by the {@link TaxLot}
   * @param securityKey the {@link SecurityKey} identifying the {@link Security} held by the {@link
   *     TaxLot}
   * @return the {@link TaxLot} that was created
   */
  public static TaxLot createTestTaxLot(double amount, SecurityKey securityKey) {
    return new TaxLot(amount, securityKey);
  }

  /**
   * Obtains an {@link EvaluationContext} suitable for most unit test purposes.
   *
   * @return an {@link EvaluationContext}
   */
  public static EvaluationContext defaultTestEvaluationContext() {
    return new EvaluationContext(eligibilityListProvider, securityProvider, portfolioProvider);
  }

  /**
   * Obtains an {@link EligibilityListProvider} suitable for unit testing.
   *
   * @return an {@link EligibilityListProvider}
   */
  public static EligibilityListProvider testEligibilityListProvider() {
    return eligibilityListProvider;
  }

  /**
   * Obtains a {@link PortfolioProvider} suitable for unit testing using {@link Portfolio}s created
   * by {@link TestUtil}.
   *
   * @return a {@link TestPortfolioProvider}
   */
  public static TestPortfolioProvider testPortfolioProvider() {
    return portfolioProvider;
  }

  /**
   * Obtains a {@link PositionProvider} suitable for unit testing using {@link Position}s created by
   * {@link TestUtil}.
   *
   * @return a {@link TestPositionProvider}
   */
  public static TestPositionProvider testPositionProvider() {
    return positionProvider;
  }

  /**
   * Obtains a {@link SecurityProvider} suitable for unit testing using {@link Security} objects
   * created by {@link TestUtil}.
   *
   * @return a {@link TestSecurityProvider}
   */
  public static TestSecurityProvider testSecurityProvider() {
    return securityProvider;
  }
}
