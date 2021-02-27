package org.slaq.slaqworx.panoptes.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * {@code TradeTest} tests the functionality of {@code Trade}.
 *
 * @author jeremy
 */
public class TradeTest {
  /**
   * Tests that {@code getAllocations()} (and related methods) behaves as expected.
   */
  @Test
  public void testGetAllocations() {
    TaxLot t1Lot1 = new TaxLot(1_000, TestUtil.s1.getKey());
    TaxLot t1Lot2 = new TaxLot(1_000, TestUtil.s2.getKey());
    List<TaxLot> t1Allocations = List.of(t1Lot1, t1Lot2);
    Transaction transaction1 = new Transaction(TestUtil.p1.getKey(), t1Allocations);

    TaxLot t2Lot1 = new TaxLot(2_000, TestUtil.s1.getKey());
    TaxLot t2Lot2 = new TaxLot(2_000, TestUtil.s2.getKey());
    List<TaxLot> t2Allocations = List.of(t2Lot1, t2Lot2);
    Transaction transaction2 = new Transaction(TestUtil.p2.getKey(), t2Allocations);

    Map<PortfolioKey, Transaction> transactions = Map.of(transaction1.getPortfolioKey(),
        transaction1, transaction2.getPortfolioKey(), transaction2);

    LocalDate date = LocalDate.now();
    Trade trade = new Trade(date, date, transactions);

    Transaction t1 = trade.getTransaction(TestUtil.p1.getKey());
    assertEquals(transaction1, t1, "unexpected transaction for p1");
    Transaction t2 = trade.getTransaction(TestUtil.p2.getKey());
    assertEquals(transaction2, t2, "unexpected transaction for p2");

    assertEquals(4, trade.getAllocationCount(), "unexpected number of allocations");

    List<TaxLot> t1Lots =
        trade.getAllocations(TestUtil.p1.getKey()).collect(Collectors.toList());
    assertEquals(2, t1Lots.size(), "unexpected number of allocations for p1");
    assertTrue(t1Lots.contains(t1Lot1), "allocations for p1 should have contained t1Lot1");
    assertTrue(t1Lots.contains(t1Lot2), "allocations for p1 should have contained t1Lot2");

    List<TaxLot> t2Lots =
        trade.getAllocations(TestUtil.p2.getKey()).collect(Collectors.toList());
    assertEquals(2, t2Lots.size(), "unexpected number of allocations for p2");
    assertTrue(t2Lots.contains(t2Lot1), "allocations for p2 should have contained t2Lot1");
    assertTrue(t2Lots.contains(t2Lot2), "allocations for p2 should have contained t2Lot2");

    List<TaxLot> noLots =
        trade.getAllocations(TestUtil.p3.getKey()).collect(Collectors.toList());
    assertTrue(noLots.isEmpty(), "should not have found allocations for nonexistent Portfolio");
  }
}
