package org.slaq.slaqworx.panoptes.asset;

import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.trade.TaxLot;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * A {@link Position} that is somewhat of a contrivance in that it is constructed "out of thin air,"
 * in contrast to a "real" {@link Position} which, in a {@link Portfolio}, is actually an
 * aggregation of {@link TaxLot}s derived from {@link Transaction}s against the same {@link
 * Security}. {@link SimplePosition} may be used to represent benchmark weights or other
 * artificially-constructed {@link Position}-like entities.
 *
 * @author jeremy
 */
public class SimplePosition extends AbstractPosition {
  private final double amount;
  @Nonnull
  private final SecurityKey securityKey;

  /**
   * Creates a new {@link SimplePosition} with a generated key and the specified amount and {@link
   * Security}.
   *
   * @param amount
   *     the amount of the {@link Security} held in this {@link Position}
   * @param securityKey
   *     a {@link SecurityKey} identifying the held {@link Security}
   */
  public SimplePosition(double amount, @Nonnull SecurityKey securityKey) {
    this(null, amount, securityKey);
  }

  /**
   * Creates a new {@link SimplePosition} with the specified key, amount and {@link Security}.
   *
   * @param key
   *     the unique key to assign to this {@link Position}, or {@code null} to generate one
   * @param amount
   *     the amount of the {@link Security} held in this {@link Position}
   * @param securityKey
   *     a {@link SecurityKey} identifying the held {@link Security}
   */
  public SimplePosition(PositionKey key, double amount, @Nonnull SecurityKey securityKey) {
    super(key);
    this.amount = amount;
    this.securityKey = securityKey;
  }

  @Override
  public double getAmount() {
    return amount;
  }

  @Override
  @Nonnull
  public SecurityKey getSecurityKey() {
    return securityKey;
  }
}
