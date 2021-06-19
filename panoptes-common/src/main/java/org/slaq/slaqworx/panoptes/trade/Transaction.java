package org.slaq.slaqworx.panoptes.trade;

import java.util.Collection;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A component of a {@link Trade} which modifies a single {@link Portfolio} by altering (increasing
 * or decreasing) the net position of one or more of its {@link Security} holdings. The {@link
 * Position}s of a {@link Trade} are also known as allocations.
 *
 * @author jeremy
 */
public class Transaction implements PositionSupplier, ProtobufSerializable {
  private final TransactionKey key;
  private final PortfolioKey portfolioKey;
  private final PositionSet<TaxLot> positions;

  /**
   * Creates a new {@link Transaction}, with a generated ID, acting on the given {@link Portfolio}
   * with the given allocations.
   *
   * @param portfolioKey
   *     the {@link PortfolioKey} identifying the {@link Portfolio} affected by this {@link
   *     Transaction}
   * @param allocations
   *     the allocations of the {@link Transaction}
   */
  public Transaction(PortfolioKey portfolioKey, Collection<TaxLot> allocations) {
    this(null, portfolioKey, allocations);
  }

  /**
   * Creates a new {@link Transaction} with the given ID, acting on the given {@link Portfolio} with
   * the given allocations.
   *
   * @param key
   *     the unique key of the {@link Transaction}
   * @param portfolioKey
   *     the {@link PortfolioKey} identifying the {@link Portfolio} affected by this {@link
   *     Transaction}
   * @param allocations
   *     the allocations of the {@link Transaction}
   */
  public Transaction(TransactionKey key, PortfolioKey portfolioKey,
      Collection<TaxLot> allocations) {
    this.key = (key == null ? new TransactionKey(null) : key);
    this.portfolioKey = portfolioKey;
    if (allocations.isEmpty()) {
      throw new IllegalArgumentException("cannot create Transaction with no allocations");
    }

    positions = new PositionSet<>(allocations, portfolioKey);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Transaction)) {
      return false;
    }
    Transaction other = (Transaction) obj;

    return key.equals(other.getKey());
  }

  /**
   * Obtains this {@link Transaction}'s unique key.
   *
   * @return the key
   */
  public TransactionKey getKey() {
    return key;
  }

  @Override
  public double getMarketValue(EvaluationContext evaluationContext) {
    return evaluationContext.getMarketValue(positions);
  }

  @Override
  public PortfolioKey getPortfolioKey() {
    return portfolioKey;
  }

  @Nonnull
  @Override
  public Stream<TaxLot> getPositions() {
    return positions.getPositions();
  }

  /**
   * Obtains the {@link TradeKey} corresponding to this {@link Trade} owning this {@link
   * Transaction}.
   *
   * @return a {@link TradeKey}
   */
  public TradeKey getTradeKey() {
    // there is always at least one allocation/TaxLot; all will have the same TradeKey so just
    // grab one
    return getPositions().findAny().get().getTradeKey();
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public int size() {
    return positions.size();
  }
}
