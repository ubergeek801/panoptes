package org.slaq.slaqworx.panoptes.trade;

import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioPosition;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;

/**
 * A {@link Position} that arises from posting a {@link Trade} against a {@link Portfolio}. A {@link
 * Portfolio}'s {@link Position}s are in fact aggregations of {@link TaxLot}s (see {@link
 * PortfolioPosition}.
 *
 * <p>Note that a {@link TaxLot} is always associated with a {@link Trade}, but since {@link
 * TaxLot}s are supplied to a {@link Trade} at construction time, the relationship must be set
 * during this process. Thus {@code tradeKey} may be regarded as effectively non-{@code null} and
 * immutable.
 *
 * @author jeremy
 */
public class TaxLot extends SimplePosition {
  private TradeKey tradeKey;

  /**
   * Creates a new {@link TaxLot} of the specified amount of the given {@link Security}, with a
   * generated key.
   *
   * @param amount the amount of the {@link Security} exchanged in this {@link TaxLot}
   * @param securityKey the {@link SecurityKey} identifying the {@link Security} being exchanged
   */
  public TaxLot(double amount, SecurityKey securityKey) {
    super(amount, securityKey);
  }

  /**
   * Creates a new {@link TaxLot} with the specified key, amount and {@link Security}.
   *
   * @param positionKey a {@link PositionKey} identifying this {@link TaxLot}
   * @param amount the amount of the {@link Security} exchanged in this {@link TaxLot}
   * @param securityKey the {@link SecurityKey} identifying the {@link Security} being exchanged
   */
  public TaxLot(PositionKey positionKey, double amount, SecurityKey securityKey) {
    super(positionKey, amount, securityKey);
  }

  /**
   * Obtains a {@link TradeKey} identifying the {@link Trade} that produced this {@link TaxLot}.
   *
   * @return a {@link TradeKey} identifying this {@link TaxLot}'s {@link Trade}
   */
  @Nonnull
  public TradeKey getTradeKey() {
    return tradeKey;
  }

  /**
   * Specifies the {@link Trade} corresponding to this {@link TaxLot}. Restricted because this must
   * be set at {@link Trade} creation time and is immutable thereafter.
   *
   * @param tradeKey a {@link TradeKey} identifying this {@link TaxLot}'s {@link Trade}
   */
  protected void setTradeKey(@Nonnull TradeKey tradeKey) {
    this.tradeKey = tradeKey;
  }
}
