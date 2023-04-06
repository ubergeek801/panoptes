package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.test.TestUtil;
import org.slaq.slaqworx.panoptes.trade.TaxLot;

/**
 * Tests the functionality of {@link PortfolioPosition}.
 *
 * @author jeremy
 */
public class PortfolioPositionTest {
  /** Tests that {@link PortfolioPosition#getTaxLots()} behaves as expected. */
  @Test
  public void getTaxLots() {
    Security security = TestUtil.s1;
    ArrayList<TaxLot> taxLots = new ArrayList<>();
    PositionKey t1Key = new PositionKey(null);
    taxLots.add(new TaxLot(t1Key, 100, security.getKey()));
    PositionKey t2Key = new PositionKey(null);
    taxLots.add(new TaxLot(t2Key, 200, security.getKey()));
    PositionKey t3Key = new PositionKey(null);
    taxLots.add(new TaxLot(t3Key, 300, security.getKey()));
    PositionKey t4Key = new PositionKey(null);
    taxLots.add(new TaxLot(t4Key, -400, security.getKey()));
    PortfolioPosition position = new PortfolioPosition(taxLots);

    Map<PositionKey, ? extends Position> positionTaxLots =
        position.getTaxLots().collect(Collectors.toMap(Position::getKey, p -> p));
    assertEquals(taxLots.size(), positionTaxLots.size(), "unexpected number of TaxLots");
    // there is no guarantee of the Stream ordering, so check for each expected key
    assertTrue(positionTaxLots.containsKey(t1Key), "expected TaxLots to contain t1Key");
    assertTrue(positionTaxLots.containsKey(t2Key), "expected TaxLots to contain t2Key");
    assertTrue(positionTaxLots.containsKey(t3Key), "expected TaxLots to contain t3Key");
    assertTrue(positionTaxLots.containsKey(t4Key), "expected TaxLots to contain t4Key");
  }

  /** Tests that {@link PortfolioPosition#getAmount()} behaves as expected. */
  @Test
  public void testGetAmount() {
    Security security = TestUtil.s1;
    ArrayList<TaxLot> taxLots = new ArrayList<>();
    taxLots.add(new TaxLot(100, security.getKey()));
    taxLots.add(new TaxLot(200, security.getKey()));
    taxLots.add(new TaxLot(300, security.getKey()));
    taxLots.add(new TaxLot(-400, security.getKey()));
    PortfolioPosition position = new PortfolioPosition(taxLots);

    // the net position is 100 + 200 + 300 - 400 = 200
    assertEquals(200, position.getAmount(), "unexpected net Position amount");
  }

  /** Tests that {@link PortfolioPosition#getSecurityKey()} behaves as expected. */
  @Test
  public void testGetSecurityKey() {
    Security security = TestUtil.s1;
    ArrayList<TaxLot> taxLots = new ArrayList<>();
    taxLots.add(new TaxLot(100, security.getKey()));
    PortfolioPosition position = new PortfolioPosition(taxLots);

    assertEquals(security.getKey(), position.getSecurityKey(), "unexpected Security key");
  }
}
