package org.slaq.slaqworx.panoptes.trade;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * An aggregate of {@code Transaction}s that modify one or more {@code Portfolio}s by altering
 * (increasing or decreasing) the net position of one or more {@code Security} holdings.
 *
 * @author jeremy
 */
public class Trade implements ProtobufSerializable {
  private final TradeKey key;
  private final LocalDate tradeDate;
  private final LocalDate settlementDate;
  private final Map<PortfolioKey, Transaction> transactions;

  /**
   * Creates a new {@code Trade} with a generated key and consisting of the given {@code
   * Transaction}s.
   *
   * @param tradeDate
   *     the date on which the {@code Trade} is effective
   * @param settlementDate
   *     the date on which the {@code Trade} settles
   * @param transactions
   *     the {@code Transaction}s comprising this {@code Trade}, mapped by {@code PortfolioKey}
   */
  public Trade(LocalDate tradeDate, LocalDate settlementDate,
               Map<PortfolioKey, Transaction> transactions) {
    this(null, tradeDate, settlementDate, transactions);
  }

  /**
   * Creates a new {@code Trade} with the given key and consisting of the given {@code
   * Transaction}s.
   *
   * @param key
   *     the unique key to assign to this {@code Trade}, or {@code null} to generate one
   * @param tradeDate
   *     the date on which the {@code Trade} is effective
   * @param settlementDate
   *     the date on which the {@code Trade} settles
   * @param transactions
   *     the {@code Transaction}s comprising this {@code Trade}, mapped by {@code PortfolioKey}
   */
  public Trade(TradeKey key, LocalDate tradeDate, LocalDate settlementDate,
               Map<PortfolioKey, Transaction> transactions) {
    this.key = (key == null ? new TradeKey(null) : key);
    this.tradeDate = tradeDate;
    this.settlementDate = settlementDate;
    this.transactions = transactions;

    // Trade and TaxLot creation are a chicken-and-egg situation, so Trade is responsible for
    // updating its TaxLots at create time
    transactions.values().stream().flatMap(Transaction::getPositions)
        .forEach(a -> a.setTradeKey(this.key));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Trade)) {
      return false;
    }
    Trade other = (Trade) obj;

    return key.equals(other.getKey());
  }

  /**
   * Obtains the total number of allocations over all {@code Transaction}s.
   *
   * @return the total allocation count
   */
  public int getAllocationCount() {
    return transactions.values().stream().collect(Collectors.summingInt(Transaction::size));
  }

  /**
   * Obtains allocations of this {@code Trade} corresponding to the specified {@code Portfolio}.
   *
   * @param portfolioKey
   *     the {@code PortfolioKey} identifying the {@code Portfolio} for which to obtain
   *     allocations
   *
   * @return a {@code Stream} of {@code Position}s representing the allocations for the specified
   *     {@code Portfolio}
   */
  public Stream<TaxLot> getAllocations(PortfolioKey portfolioKey) {
    Transaction transaction = transactions.get(portfolioKey);

    if (transaction == null) {
      return Stream.empty();
    }

    return transaction.getPositions();
  }

  /**
   * Obtains this {@code Trade}'s unique key.
   *
   * @return the key
   */
  public TradeKey getKey() {
    return key;
  }

  /**
   * Obtains the date on which this {@code Trade} settles.
   *
   * @return the settlement date
   */
  public LocalDate getSettlementDate() {
    return settlementDate;
  }

  /**
   * Obtains the date on which this {@code Trade} is effective.
   *
   * @return the trade date
   */
  public LocalDate getTradeDate() {
    return tradeDate;
  }

  /**
   * Obtains the {@code Transaction} corresponding to the specified {@code Portfolio}.
   *
   * @param portfolioKey
   *     the {@code PortfolioKey} identifying the {@code Portfolio} for which to obtain the
   *     transaction
   *
   * @return the {@code Transaction} corresponding to the specified {@code Portfolio}, or {@code
   *     null} if it does not exist
   */
  public Transaction getTransaction(PortfolioKey portfolioKey) {
    return transactions.get(portfolioKey);
  }

  /**
   * Obtains all {@code Transactions} of this {@code Trade} grouped by impacted {@code Portfolio}.
   *
   * @return a {@code Map} of {@code PortfolioKey} to the {@code Transaction} impacting it
   */
  public Map<PortfolioKey, Transaction> getTransactions() {
    return transactions;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }
}
