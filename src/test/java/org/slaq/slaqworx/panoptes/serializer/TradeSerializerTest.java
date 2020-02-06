package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.trade.TaxLot;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * {@code TradeSerializerTest} tests the functionality of the {@code TradeSerializer}.
 *
 * @author jeremy
 */
public class TradeSerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        TradeSerializer serializer = new TradeSerializer();

        TaxLot p1 = new TaxLot(100d, TestUtil.s1.getKey());
        TaxLot p2 = new TaxLot(200d, TestUtil.s2.getKey());
        Transaction t1 = new Transaction(TestUtil.p1.getKey(), List.of(p1, p2));
        TaxLot p3 = new TaxLot(300d, TestUtil.s2.getKey());
        TaxLot p4 = new TaxLot(400d, TestUtil.s3.getKey());
        Transaction t2 = new Transaction(TestUtil.p2.getKey(), List.of(p3, p4));

        LocalDate tradeDate = LocalDate.of(2020, 2, 1);
        LocalDate settlementDate = LocalDate.of(2020, 2, 4);
        Trade trade = new Trade(tradeDate, settlementDate,
                Map.of(t1.getPortfolioKey(), t1, t2.getPortfolioKey(), t2));

        byte[] buffer = serializer.write(trade);
        Trade deserialized = serializer.read(buffer);

        assertEquals(trade, deserialized, "deserialized value should equals() original value");
        assertEquals(trade.getKey(), deserialized.getKey(),
                "deserialized value should have same key as original");
        assertEquals(trade.getTradeDate(), deserialized.getTradeDate(),
                "deserialized value should have same trade date as original");
        assertEquals(trade.getSettlementDate(), deserialized.getSettlementDate(),
                "deserialized value should have same settlement date as original");
        assertEquals(trade.getTransactions().size(), deserialized.getTransactions().size(),
                "deserialized value should have same number of transactions as original");

        Comparator<Transaction> transactionComparator =
                (tx1, tx2) -> tx1.getKey().getId().compareTo(tx2.getKey().getId());

        // sort the Transaction lists so we can compare elements
        ArrayList<Transaction> originalTransactions = new ArrayList<>();
        trade.getTransactions().values().forEach(originalTransactions::add);
        originalTransactions.sort(transactionComparator);
        ArrayList<Transaction> deserializedTransactions = new ArrayList<>();
        deserialized.getTransactions().values().forEach(deserializedTransactions::add);
        deserializedTransactions.sort(transactionComparator);

        Iterator<Transaction> tradeTransactionIter = originalTransactions.iterator();
        Iterator<Transaction> deserializedTransactionIter = deserializedTransactions.iterator();
        while (tradeTransactionIter.hasNext()) {
            Transaction transaction = tradeTransactionIter.next();
            Transaction deserializedTransaction = deserializedTransactionIter.next();

            assertEquals(transaction, deserializedTransaction,
                    "deserialized Transaction should equals() original");
            assertEquals(transaction.getKey(), deserializedTransaction.getKey(),
                    "deserialized Transaction should have same key as original");
            assertEquals(transaction.getPortfolioKey(), deserializedTransaction.getPortfolioKey(),
                    "deserialized Transaction should have same Portfolio as original");
            assertEquals(transaction.getPositions().count(),
                    deserializedTransaction.getPositions().count(),
                    "deserialized Transaction should have same number of allocations as original");

            Iterator<? extends Position> tradeAllocationIter =
                    transaction.getPositions().iterator();
            Iterator<? extends Position> deserializedAllocationIter =
                    deserializedTransaction.getPositions().iterator();
            while (tradeAllocationIter.hasNext()) {
                Position allocation = tradeAllocationIter.next();
                Position deserializedAllocation = deserializedAllocationIter.next();

                assertEquals(allocation, deserializedAllocation,
                        "deserialized allocation should equals() original");
                assertEquals(allocation.getKey(), deserializedAllocation.getKey(),
                        "deserialized allocation should have same key as original");
                assertEquals(allocation.getAmount(), deserializedAllocation.getAmount(),
                        TestUtil.EPSILON,
                        "deserialized allocation should have same amount as original");
                assertEquals(allocation.getSecurityKey(), deserializedAllocation.getSecurityKey(),
                        "deserialized allocation should have same SecurityKey as original");
            }
        }
    }
}
