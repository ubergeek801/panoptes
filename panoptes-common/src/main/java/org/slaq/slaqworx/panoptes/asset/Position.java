package org.slaq.slaqworx.panoptes.asset;

import java.util.EnumSet;
import java.util.stream.Stream;
import org.slaq.slaqworx.panoptes.NoDataException;
import org.slaq.slaqworx.panoptes.asset.HierarchicalPositionSupplier.PositionHierarchyOption;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.trade.TaxLot;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A holding of some amount of a particular {@link Security} by some {@link Portfolio}. A {@link
 * Position} may be durable (e.g. sourced from a database/cache) or ephemeral (e.g. supplied by a
 * proposed {@link Trade} or even a unit test).
 * <p>
 * {@link Position}s are inherently hierarchical on a number of dimensions:
 * <ul>
 * <li>The {@link Security} held in a {@link Position} of a {@link Portfolio} may itself represent
 * another {@link Portfolio} with its own {@link Position}s. Relative to the "parent"
 * {@link Portfolio}, these "child" {@link Position}s are known as "lookthrough" {@link Position}s.
 * Note that lookthrough may be recursive, forming (what is assumed to be) a directed acyclic graph
 * of {@link Position} holdings.</li>
 * <li>In an investment {@link Portfolio}, the {@link Position}s are aggregations of
 * {@link Transaction}s on the same {@link Security} occurring on that {@link Portfolio} over time.
 * Each {@link Transaction} produces a {@link TaxLot}, which is merely a direct (non-aggregate)
 * {@link Position} in the {@link Security}.</li>
 * </ul>
 *
 * @author jeremy
 */
public interface Position extends Keyed<PositionKey>, MarketValued, ProtobufSerializable {
  /**
   * Obtains the amount held by this {@link Position}.
   *
   * @return the amount
   */
  public double getAmount();

  /**
   * Obtains the value of the specified attribute of this {@link Position}'s held {@link Security}.
   * This is merely a convenience method to avoid an intermediate call to {@link
   * #getSecurity(EvaluationContext)}.
   *
   * @param <T>
   *     the expected type of the attribute value
   * @param attribute
   *     the {@link SecurityAttribute} identifying the attribute
   * @param isRequired
   *     {@code true} if a return value is required, {@code false otherwise}
   * @param evaluationContext
   *     the {@link EvaluationContext} in which the attribute value is being retrieved
   *
   * @return the value of the given attribute, or {@code null} if not assigned and {@code
   *     isRequired} is {@code false}
   *
   * @throws NoDataException
   *     if the attribute value is not assigned and {@code isRequired} is {@code true}
   */
  public default <T> T getAttributeValue(SecurityAttribute<T> attribute, boolean isRequired,
      EvaluationContext evaluationContext) {
    return getSecurity(evaluationContext)
        .getEffectiveAttributeValue(attribute, isRequired, evaluationContext);
  }

  /**
   * Obtains the value of the specified attribute of this {@link Position}'s held {@link Security}.
   * This is merely a convenience method to avoid an intermediate call to {@link
   * #getSecurity(EvaluationContext)}.
   *
   * @param <T>
   *     the expected type of the attribute value
   * @param attribute
   *     the {@link SecurityAttribute} identifying the attribute
   * @param evaluationContext
   *     the {@link EvaluationContext} in which the attribute value is being retrieved
   *
   * @return the value of the given attribute
   *
   * @throws NoDataException
   *     if the requested attribute has no assigned value
   */
  public default <T> T getAttributeValue(SecurityAttribute<T> attribute,
      EvaluationContext evaluationContext) {
    return getAttributeValue(attribute, true, evaluationContext);
  }

  /**
   * Obtains the lookthrough {@link Position}s of this {@link Position} as a {@link Stream}.
   *
   * @param evaluationContext
   *     the {@link EvaluationContext} in which the {@link Position}s are being obtained
   *
   * @return a {@link Stream} of this {@link Position}'s lookthrough {@link Position}s, or the
   *     {@link Position} itself if not applicable
   */
  public default Stream<? extends Position> getLookthroughPositions(
      EvaluationContext evaluationContext) {
    PortfolioKey lookthroughPortfolioKey = getSecurity(evaluationContext)
        .getEffectiveAttributeValue(SecurityAttribute.portfolio, false, evaluationContext);
    if (lookthroughPortfolioKey == null) {
      // lookthrough not applicable
      return Stream.of(this);
    }

    Portfolio lookthroughPortfolio =
        evaluationContext.getPortfolioProvider().getPortfolio(lookthroughPortfolioKey);
    // For a Security representing a fund, the amount attribute represents the number of fund
    // shares outstanding, so the effective lookthrough Positions should have their amounts
    // scaled by (this Position's amount in fund) / (total amount of fund). Each Position of the
    // held Portfolio is mapped to a ScaledPosition using this multiplier.
    double portfolioAmount = getAttributeValue(SecurityAttribute.amount, evaluationContext);
    return lookthroughPortfolio
        .getPositions(EnumSet.of(PositionHierarchyOption.LOOKTHROUGH), evaluationContext)
        .map(p -> new ScaledPosition(p, getAmount() / portfolioAmount));
  }

  @Override
  public default double getMarketValue(EvaluationContext evaluationContext) {
    return getAttributeValue(SecurityAttribute.price, evaluationContext) * getAmount();
  }

  /**
   * Obtains the {@link Security} held by this {@link Position}.
   *
   * @param evaluationContext
   *     the {@link EvaluationContext} in which an evaluation is taking place
   *
   * @return the {@link Security} held by this {@link Position}
   */
  public default Security getSecurity(EvaluationContext evaluationContext) {
    return evaluationContext.getSecurityProvider().getSecurity(getSecurityKey(), evaluationContext);
  }

  /**
   * Obtains the {@link SecurityKey} identifying the {@link Security} held by this {@link
   * Position}.
   *
   * @return the key of the {@link Security} held by this {@link Position}
   */
  public SecurityKey getSecurityKey();

  /**
   * Obtains the tax lots of this {@link Position} as a {@link Stream}.
   *
   * @return a {@link Stream} of this {@link Position}'s constituent tax lots, or the {@link
   *     Position} itself if not applicable
   */
  public default Stream<? extends Position> getTaxLots() {
    return Stream.of(this);
  }
}
