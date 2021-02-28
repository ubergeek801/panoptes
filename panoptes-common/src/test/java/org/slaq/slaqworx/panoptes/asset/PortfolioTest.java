package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.HierarchicalPositionSupplier.PositionHierarchyOption;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.test.TestPortfolioProvider;
import org.slaq.slaqworx.panoptes.test.TestSecurityProvider;
import org.slaq.slaqworx.panoptes.test.TestUtil;
import org.slaq.slaqworx.panoptes.trade.TaxLot;

/**
 * Tests the functionality of the {@link Portfolio}.
 *
 * @author jeremy
 */
@MicronautTest
public class PortfolioTest {
  private final TestSecurityProvider securityProvider = TestUtil.testSecurityProvider();
  private final TestPortfolioProvider portfolioProvider = TestUtil.testPortfolioProvider();
  @Inject
  private AssetCache assetCache;

  /**
   * Tests that {@link Portfolio#getPositions()} behaves as expected for non-hierarchy requests.
   */
  @Test
  public void testGetPositions() {
    Security dummySecurity =
        securityProvider.newSecurity("dummy", SecurityAttribute.mapOf(SecurityAttribute.price, 1d));
    HashSet<Position> positions = new HashSet<>();
    positions.add(new SimplePosition(100, dummySecurity.getKey()));
    positions.add(new SimplePosition(200, dummySecurity.getKey()));
    positions.add(new SimplePosition(300, dummySecurity.getKey()));
    positions.add(new SimplePosition(400, dummySecurity.getKey()));

    Portfolio portfolio = new Portfolio(new PortfolioKey("p1", 1), "test", positions);

    try (Stream<? extends Position> stream1 = portfolio.getPositions();
         Stream<? extends Position> stream2 = portfolio.getPositions()) {
      assertFalse(stream1 == stream2, "position streams should be distinct");
      assertFalse(stream1.equals(stream2), "position streams should be distinct");
      assertEquals(4, stream1.count(), "unexpected count for stream 1");
      assertEquals(4, stream2.count(), "unexpected count for stream 2");

      // ensure that both streams can be iterated independently/simultaneously
      Iterator<? extends Position> iter1 = portfolio.getPositions().iterator();
      Iterator<? extends Position> iter2 = portfolio.getPositions().iterator();
      int itemCount = 0;
      while (iter1.hasNext()) {
        iter1.next();
        iter2.next();
        itemCount++;
      }
      assertFalse(iter2.hasNext(), "stream 2 should be exhausted");
      assertEquals(4, itemCount, "unexpected count for parallel stream iteration");
    }
  }

  /**
   * Tests that {@link Portfolio#getPositions()} works as expected when lookthrough {@link
   * Position}s are requested.
   */
  @Test
  public void testGetPositions_lookthrough() {
    // just a plain old Security (no lookthrough)
    Security plainSecurity1 =
        securityProvider.newSecurity("sec1", SecurityAttribute.mapOf(SecurityAttribute.price, 1d));

    // these Securities have a Portfolio key associated and thus represent holdings in those
    // funds
    PortfolioKey p2Key = new PortfolioKey("p2", 1);
    Security p2Security = securityProvider.newSecurity("p2Sec", SecurityAttribute
        .mapOf(SecurityAttribute.price, 1d, SecurityAttribute.portfolio, p2Key,
            SecurityAttribute.amount, 1000d));
    PortfolioKey p3Key = new PortfolioKey("p3", 1);
    Security p3Security = securityProvider.newSecurity("p3Sec", SecurityAttribute
        .mapOf(SecurityAttribute.price, 1d, SecurityAttribute.portfolio, p3Key,
            SecurityAttribute.amount, 1000d));
    PortfolioKey p4Key = new PortfolioKey("p4", 1);
    Security p4Security = securityProvider.newSecurity("p4Sec", SecurityAttribute
        .mapOf(SecurityAttribute.price, 1d, SecurityAttribute.portfolio, p4Key,
            SecurityAttribute.amount, 1000d));

    // p1 is 20% p2 and 20% p3
    HashSet<Position> p1Positions = new HashSet<>();
    p1Positions.add(new SimplePosition(300, plainSecurity1.getKey()));
    p1Positions.add(new SimplePosition(100, p2Security.getKey()));
    p1Positions.add(new SimplePosition(100, p3Security.getKey()));
    Portfolio p1 =
        portfolioProvider.newPortfolio("p1", "test1", p1Positions, null, Collections.emptySet());

    // p2 is 20% p3 and 20% p4, with amount 1000
    HashSet<Position> p2Positions = new HashSet<>();
    p2Positions.add(new SimplePosition(600, plainSecurity1.getKey()));
    p2Positions.add(new SimplePosition(200, p3Security.getKey()));
    p2Positions.add(new SimplePosition(200, p4Security.getKey()));
    portfolioProvider.newPortfolio(p2Key, "test2", p2Positions, null, Collections.emptySet());

    // p3 is 50% p4, with amount 1000
    HashSet<Position> p3Positions = new HashSet<>();
    p3Positions.add(new SimplePosition(100, plainSecurity1.getKey()));
    p3Positions.add(new SimplePosition(100, p4Security.getKey()));
    portfolioProvider.newPortfolio(p3Key, "test3", p3Positions, null, Collections.emptySet());

    // p4 holds no other Portfolios and has amount 1000
    HashSet<Position> p4Positions = new HashSet<>();
    p4Positions.add(new SimplePosition(500, plainSecurity1.getKey()));
    portfolioProvider.newPortfolio(p4Key, "test4", p4Positions, null, Collections.emptySet());

    // lookthrough of p1 should see:
    // 300 sec1
    // + 100 p2
    // + 100 p3
    // which expands to
    // 300 sec1
    // + (100 / 1000) * 600 sec1 [p2]
    // + (100 / 1000) * 200 p3 [p2]
    // + (100 / 1000) * 200 p4 [p2]
    // + (100 / 1000) * 100 sec1 [p3]
    // + (100 / 1000) * 100 p4 [p3]
    // which again expands to
    // 300 sec1
    // + (100 / 1000) * 600 sec1 [p2]
    // + (100 / 1000) * (200 / 1000) * 100 sec1 [p2][p3]
    // + (100 / 1000) * (200 / 1000) * 100 p4 [p2][p3]
    // + (100 / 1000) * (200 / 1000) * 500 sec1 [p2][p4]
    // + (100 / 1000) * 100 sec1 [p3]
    // + (100 / 1000) * (100 / 1000) * 500 sec1 [p3][p4]
    // and finally to
    // 300 sec1
    // + (100 / 1000) * 600 sec1 [p2]
    // + (100 / 1000) * (200 / 1000) * 100 sec1 [p2][p3]
    // + (100 / 1000) * (200 / 1000) * (100 / 1000) * 500 sec1 [p2][p3][p4]
    // + (100 / 1000) * (200 / 1000) * 500 sec1 [p2][p4]
    // + (100 / 1000) * 100 sec1 [p3]
    // + (100 / 1000) * (100 / 1000) * 500 sec1 [p3][p4]
    // which amounts to these Positions in sec1:
    // 300
    // 60
    // 2
    // 1
    // 10
    // 10
    // 5

    EvaluationContext evaluationContext = TestUtil.defaultTestEvaluationContext();
    List<? extends Position> lookthroughPositions =
        p1.getPositions(EnumSet.of(PositionHierarchyOption.LOOKTHROUGH), evaluationContext)
            .collect(Collectors.toList());

    assertEquals(7, lookthroughPositions.size(),
        "unexpected total number of lookthrough Positions");
    // this is rather tedious
    boolean is300PositionFound = false;
    boolean is60PositionFound = false;
    boolean is2PositionFound = false;
    boolean is1PositionFound = false;
    boolean is10Position1Found = false;
    boolean is10Position2Found = false;
    boolean is5PositionFound = false;
    for (Position p : lookthroughPositions) {
      double amount = p.getAmount();
      if (amount == 300) {
        is300PositionFound = true;
      } else if (amount == 60) {
        is60PositionFound = true;
      } else if (amount == 2) {
        is2PositionFound = true;
      } else if (amount == 1) {
        is1PositionFound = true;
      } else if (amount == 10) {
        if (!is10Position1Found) {
          is10Position1Found = true;
        } else {
          is10Position2Found = true;
        }
      } else if (amount == 5) {
        is5PositionFound = true;
      } else {
        fail("found unexpected Position amount " + amount);
      }
    }
    assertTrue(is300PositionFound, "expected to find Position with amount 300");
    assertTrue(is60PositionFound, "expected to find Position with amount 60");
    assertTrue(is2PositionFound, "expected to find Position with amount 2");
    assertTrue(is1PositionFound, "expected to find Position with amount 1");
    assertTrue(is10Position1Found, "expected to find Position with amount 10");
    assertTrue(is10Position2Found, "expected to find second Position with amount 10");
    assertTrue(is5PositionFound, "expected to find Position with amount 5");
  }

  /**
   * Tests that {@link Portfolio#getPositions()} works as expected when {@link TaxLot}s are
   * requested.
   */
  @Test
  public void testGetPositions_taxLot() {
    Security sec1 = TestUtil.createTestSecurity(assetCache, "sec1",
        SecurityAttribute.mapOf(SecurityAttribute.price, 100d));
    Security sec2 = TestUtil.createTestSecurity(assetCache, "sec2",
        SecurityAttribute.mapOf(SecurityAttribute.price, 100d));
    Security sec3 = TestUtil.createTestSecurity(assetCache, "sec3",
        SecurityAttribute.mapOf(SecurityAttribute.price, 100d));

    TaxLot p1TaxLot1 = new TaxLot(100, sec1.getKey());
    TaxLot p1TaxLot2 = new TaxLot(-50, sec1.getKey());
    TaxLot p1TaxLot3 = new TaxLot(50, sec1.getKey());
    List<TaxLot> p1TaxLots = List.of(p1TaxLot1, p1TaxLot2, p1TaxLot3);
    PortfolioPosition p1 = new PortfolioPosition(p1TaxLots);

    TaxLot p2TaxLot1 = new TaxLot(200, sec2.getKey());
    TaxLot p2TaxLot2 = new TaxLot(-100, sec2.getKey());
    TaxLot p2TaxLot3 = new TaxLot(100, sec2.getKey());
    List<TaxLot> p2TaxLots = List.of(p2TaxLot1, p2TaxLot2, p2TaxLot3);
    PortfolioPosition p2 = new PortfolioPosition(p2TaxLots);

    TaxLot p3TaxLot1 = new TaxLot(300, sec3.getKey());
    TaxLot p3TaxLot2 = new TaxLot(-150, sec3.getKey());
    TaxLot p3TaxLot3 = new TaxLot(150, sec3.getKey());
    List<TaxLot> p3TaxLots = List.of(p3TaxLot1, p3TaxLot2, p3TaxLot3);
    PortfolioPosition p3 = new PortfolioPosition(p3TaxLots);

    Set<Position> positions = Set.of(p1, p2, p3);
    Portfolio portfolio = TestUtil
        .createTestPortfolio(assetCache, null, "TaxLotTestPortfolio", positions, null,
            Collections.emptySet());

    Map<PositionKey, ? extends Position> taxLots = portfolio
        .getPositions(EnumSet.of(PositionHierarchyOption.TAXLOT),
            TestUtil.defaultTestEvaluationContext())
        .collect(Collectors.toMap(Position::getKey, t -> t));

    assertEquals(p1TaxLots.size() + p2TaxLots.size() + p3TaxLots.size(), taxLots.size(),
        "size of Portfolio TaxLots should equal sum of Position TaxLots size");
    for (Position p : taxLots.values()) {
      assertTrue(p instanceof TaxLot, "each tax lot should be instanceof TaxLot");
    }
    assertTrue(taxLots.containsKey(p1TaxLot1.getKey()),
        "Portfolio TaxLots should contain p1TaxLot1");
    assertTrue(taxLots.containsKey(p1TaxLot2.getKey()),
        "Portfolio TaxLots should contain p1TaxLot2");
    assertTrue(taxLots.containsKey(p1TaxLot3.getKey()),
        "Portfolio TaxLots should contain p1TaxLot3");
    assertTrue(taxLots.containsKey(p2TaxLot1.getKey()),
        "Portfolio TaxLots should contain p2TaxLot1");
    assertTrue(taxLots.containsKey(p2TaxLot2.getKey()),
        "Portfolio TaxLots should contain p2TaxLot2");
    assertTrue(taxLots.containsKey(p2TaxLot3.getKey()),
        "Portfolio TaxLots should contain p2TaxLot3");
    assertTrue(taxLots.containsKey(p3TaxLot1.getKey()),
        "Portfolio TaxLots should contain p3TaxLot1");
    assertTrue(taxLots.containsKey(p3TaxLot2.getKey()),
        "Portfolio TaxLots should contain p3TaxLot2");
    assertTrue(taxLots.containsKey(p3TaxLot3.getKey()),
        "Portfolio TaxLots should contain p3TaxLot3");
  }

  /**
   * Tests that {@link Portfolio#getMarketValue(EvaluationContext)}} behaves as expected.
   */
  @Test
  public void testGetMarketValue() {
    Security dummySecurity =
        securityProvider.newSecurity("dummy", SecurityAttribute.mapOf(SecurityAttribute.price, 2d));
    HashSet<Position> positions = new HashSet<>();
    positions.add(new SimplePosition(100, dummySecurity.getKey()));
    positions.add(new SimplePosition(200, dummySecurity.getKey()));
    positions.add(new SimplePosition(300, dummySecurity.getKey()));
    positions.add(new SimplePosition(400, dummySecurity.getKey()));

    Portfolio portfolio = new Portfolio(new PortfolioKey("test", 1), "test", positions);
    // the total amount is merely the sum of the amounts (100 + 200 + 300 + 400) * 2.00 = 2000
    assertEquals(2000, portfolio.getMarketValue(TestUtil.defaultTestEvaluationContext()),
        TestUtil.EPSILON, "unexpected total amount");

    // changing the price should change the market value
    dummySecurity =
        securityProvider.newSecurity("dummy", SecurityAttribute.mapOf(SecurityAttribute.price, 4d));
    assertEquals(4000, portfolio.getMarketValue(TestUtil.defaultTestEvaluationContext()),
        TestUtil.EPSILON, "unexpected total amount after price change");
  }

  /**
   * Tests that {@link Portfolio}s are hashed in a reasonable way.
   */
  @Test
  public void testHash() {
    Portfolio p1 = new Portfolio(new PortfolioKey("p1", 1), "test", Collections.emptySet());
    Portfolio p2 = new Portfolio(new PortfolioKey("p2", 1), "test", Collections.emptySet());
    Portfolio p3 = new Portfolio(new PortfolioKey("p3", 1), "test", Collections.emptySet());
    Portfolio p4 = new Portfolio(new PortfolioKey("p4", 1), "test", Collections.emptySet());
    Portfolio p1a = new Portfolio(new PortfolioKey("p1", 1), "test", Collections.emptySet());
    Portfolio p2a = new Portfolio(new PortfolioKey("p2", 1), "test", Collections.emptySet());
    Portfolio p3a = new Portfolio(new PortfolioKey("p3", 1), "test", Collections.emptySet());
    Portfolio p4a = new Portfolio(new PortfolioKey("p4", 1), "test", Collections.emptySet());

    HashSet<Portfolio> portfolios = new HashSet<>();
    // adding the four distinct Portfolios any number of times should still result in four
    // distinct Portfolios
    portfolios.add(p1);
    portfolios.add(p2);
    portfolios.add(p3);
    portfolios.add(p4);
    portfolios.add(p1a);
    portfolios.add(p2a);
    portfolios.add(p3a);
    portfolios.add(p4a);
    portfolios.add(p1);
    portfolios.add(p2);
    portfolios.add(p4);
    portfolios.add(p2);
    portfolios.add(p4);
    portfolios.add(p1);
    portfolios.add(p3);

    assertEquals(4, portfolios.size(), "unexpected number of Portfolios");
    assertTrue(portfolios.contains(p1), "Portfolios should have contained p1");
    assertTrue(portfolios.contains(p2), "Portfolios should have contained p2");
    assertTrue(portfolios.contains(p3), "Portfolios should have contained p3");
    assertTrue(portfolios.contains(p4), "Portfolios should have contained p4");
  }
}
