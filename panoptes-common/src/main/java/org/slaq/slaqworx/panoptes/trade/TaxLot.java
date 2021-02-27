package org.slaq.slaqworx.panoptes.trade;

import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;

/**
 * A {@code Position} that arises from posting a {@code Trade} against a {@code Portfolio}. A {@code
 * Portfolio}'s {@code Position}s are in fact aggregations of {@code TaxLot}s (see {@code
 * PortfolioPosition}.
 * <p>
 * Note that a {@code TaxLot} is always associated with a {@code Trade}, but since {@code TaxLot}s
 * are supplied to a {@code Trade} at construction time, the relationship must be set during this
 * process. Thus {@code tradeKey} may be regarded as non-{@code null} and immutable.
 *
 * @author jeremy
 */
public class TaxLot extends SimplePosition {
  private TradeKey tradeKey;

  /**
   * Creates a new {@code TaxLot} of the specified amount of the given {@code Security}, with a
   * generated key.
   *
   * @param amount
   *     the amount of the {@code Security} exchanged in this {@code TaxLot}
   * @param securityKey
   *     the {@code SecurityKey} identifying the {@code Security} being exchanged
   */
  public TaxLot(double amount, SecurityKey securityKey) {
    super(amount, securityKey);
  }

  /**
   * Creates a new {@code TaxLot} with the specified key, amount and {@code Security}.
   *
   * @param positionKey
   *     a {@code PositionKey} identifying this {@code TaxLot}
   * @param amount
   *     the amount of the {@code Security} exchanged in this {@code TaxLot}
   * @param securityKey
   *     the {@code SecurityKey} identifying the {@code Security} being exchanged
   */
  public TaxLot(PositionKey positionKey, double amount, SecurityKey securityKey) {
    super(positionKey, amount, securityKey);
  }

  /**
   * Obtains a {@code TradeKey} identifying the {@code Trade} that produced this {@code TaxLot}.
   *
   * @return a {@code TradeKey} identifying this {@code TaxLot}'s {@code Trade}
   */
  public TradeKey getTradeKey() {
    return tradeKey;
  }

  /**
   * Specifies the {@code Trade} corresponding to this {@code TaxLot}. Restricted because this
   * must
   * be set at {@code Trade} creation time and is immutable thereafter.
   *
   * @param tradeKey
   *     a {@code TradeKey} identifying this {@code TaxLot}'s {@code Trade}
   */
  protected void setTradeKey(TradeKey tradeKey) {
    this.tradeKey = tradeKey;
  }
}
