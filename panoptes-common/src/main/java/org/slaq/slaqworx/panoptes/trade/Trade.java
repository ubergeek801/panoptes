package org.slaq.slaqworx.panoptes.trade;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * An aggregate of {@link Transaction}s that modify one or more {@link Portfolio}s by altering
 * (increasing or decreasing) the net position of one or more {@link Security} holdings.
 *
 * @author jeremy
 */
public class Trade implements ProtobufSerializable {
  private final TradeKey key;
  private final LocalDate tradeDate;
  private final LocalDate settlementDate;
  private final Map<PortfolioKey, Transaction> transactions;

  /**
   * Creates a new {@link Trade} with a generated key and consisting of the given {@link
   * Transaction}s.
   *
   * @param tradeDate the date on which the {@link Trade} is effective
   * @param settlementDate the date on which the {@link Trade} settles
   * @param transactions the {@link Transaction}s comprising this {@link Trade}, mapped by {@link
   *     PortfolioKey}
   */
  public Trade(
      LocalDate tradeDate, LocalDate settlementDate, Map<PortfolioKey, Transaction> transactions) {
    this(null, tradeDate, settlementDate, transactions);
  }

  /**
   * Creates a new {@link Trade} with the given key and consisting of the given {@link
   * Transaction}s.
   *
   * @param key the unique key to assign to this {@link Trade}, or {@code null} to generate one
   * @param tradeDate the date on which the {@link Trade} is effective
   * @param settlementDate the date on which the {@link Trade} settles
   * @param transactions the {@link Transaction}s comprising this {@link Trade}, mapped by {@link
   *     PortfolioKey}
   */
  public Trade(
      TradeKey key,
      LocalDate tradeDate,
      LocalDate settlementDate,
      Map<PortfolioKey, Transaction> transactions) {
    this.key = (key == null ? new TradeKey(null) : key);
    this.tradeDate = tradeDate;
    this.settlementDate = settlementDate;
    this.transactions = transactions;

    // Trade and TaxLot creation are a chicken-and-egg situation, so Trade is responsible for
    // updating its TaxLots at create time
    transactions.values().stream()
        .flatMap(Transaction::getPositions)
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
   * Obtains the total number of allocations over all {@link Transaction}s.
   *
   * @return the total allocation count
   */
  public int getAllocationCount() {
    return transactions.values().stream().mapToInt(Transaction::size).sum();
  }

  /**
   * Obtains allocations of this {@link Trade} corresponding to the specified {@link Portfolio}.
   *
   * @param portfolioKey the {@link PortfolioKey} identifying the {@link Portfolio} for which to
   *     obtain allocations
   * @return a {@link Stream} of {@link Position}s representing the allocations for the specified
   *     {@link Portfolio}
   */
  public Stream<TaxLot> getAllocations(PortfolioKey portfolioKey) {
    Transaction transaction = transactions.get(portfolioKey);

    if (transaction == null) {
      return Stream.empty();
    }

    return transaction.getPositions();
  }

  /**
   * Obtains this {@link Trade}'s unique key.
   *
   * @return the key
   */
  public TradeKey getKey() {
    return key;
  }

  /**
   * Obtains the date on which this {@link Trade} settles.
   *
   * @return the settlement date
   */
  public LocalDate getSettlementDate() {
    return settlementDate;
  }

  /**
   * Obtains the date on which this {@link Trade} is effective.
   *
   * @return the trade date
   */
  public LocalDate getTradeDate() {
    return tradeDate;
  }

  /**
   * Obtains the {@link Transaction} corresponding to the specified {@link Portfolio}.
   *
   * @param portfolioKey the {@link PortfolioKey} identifying the {@link Portfolio} for which to
   *     obtain the transaction
   * @return the {@link Transaction} corresponding to the specified {@link Portfolio}, or {@code
   *     null} if it does not exist
   */
  public Transaction getTransaction(PortfolioKey portfolioKey) {
    return transactions.get(portfolioKey);
  }

  /**
   * Obtains all {@link Transaction}s of this {@link Trade} grouped by impacted {@link Portfolio}.
   *
   * @return a {@link Map} of {@link PortfolioKey} to the {@link Transaction} impacting it
   */
  public Map<PortfolioKey, Transaction> getTransactions() {
    return transactions;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }
}
