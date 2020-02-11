package org.slaq.slaqworx.panoptes.asset;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.HierarchicalPositionSupplier.PositionHierarchyOption;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A {@code Position} is a holding of some amount of a particular {@code Security} by some
 * {@code Portfolio}. A {@code Position} may be durable (e.g. sourced from a database/cache) or
 * ephemeral (e.g. supplied by a proposed {@code Trade} or even a unit test).
 * <p>
 * {@code Position}s are inherently hierarchical on a number of dimensions:
 * <ul>
 * <li>The {@code Security} held in a {@code Position} of a {@code Portfolio} may itself represent
 * another {@code Portfolio} with its own {@code Position}s. Relative to the "parent"
 * {@code Portfolio}, these "child" {@code Position}s are known as "lookthrough" {@code Position}s.
 * Note that lookthrough may be recursive, forming (what is assumed to be) a directed acyclic graph
 * of {@code Position} holdings.</li>
 * <li>In an investment {@code Portfolio}, the {@code Position}s are aggregations of
 * {@code Transaction}s on the same {@code Security} occurring on that {@code Portfolio} over time.
 * Each {@code Transaction} produces a {@code TaxLot}, which is merely a direct (non-aggregate)
 * {@code Position} in the {@code Security}.</li>
 * </ul>
 *
 * @author jeremy
 */
public interface Position extends Keyed<PositionKey> {
    /**
     * Obtains the amount held by this {@code Position}.
     *
     * @return the amount
     */
    public double getAmount();

    /**
     * Obtains the value of the specified attribute of this {@code Position}'s held
     * {@code Security}. This is merely a convenience method to avoid an intermediate call to
     * {@code getSecurity()}.
     *
     * @param <T>
     *            the expected type of the attribute value
     * @param attribute
     *            the {@code SecurityAttribute} identifying the attribute
     * @param evaluationContext
     *            the {@code EvaluationContext} in which the attribute value is being retrieved
     * @return the value of the given attribute, or {@code null} if not assigned
     */
    public default <T> T getAttributeValue(SecurityAttribute<T> attribute,
            EvaluationContext evaluationContext) {
        return getSecurity(evaluationContext).getEffectiveAttributeValue(attribute,
                evaluationContext);
    }

    /**
     * Obtains the lookthrough {@code Position}s of this {@code Position} as a {@code Stream}.
     *
     * @param evaluationContext
     *            the {@code EvaluationContext} in which the {@code Position}s are being obtained
     * @return a {@code Stream} of this {@code Position}'s lookthrough {@code Position}s, or the
     *         {@code Position} itself if not applicable
     */
    public default Stream<? extends Position>
            getLookthroughPositions(EvaluationContext evaluationContext) {
        PortfolioKey lookthroughPortfolioKey =
                getAttributeValue(SecurityAttribute.portfolio, evaluationContext);
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

    /**
     * Obtains the market value of this {@code Position}.
     *
     * @param evaluationContext
     *            the {@code EvaluationContext} in which to obtain the market value
     * @return the market value
     */
    public default double getMarketValue(EvaluationContext evaluationContext) {
        BigDecimal price = getAttributeValue(SecurityAttribute.price, evaluationContext);
        return price.multiply(BigDecimal.valueOf(getAmount())).doubleValue();
    }

    /**
     * Obtains the {@code Security} held by this {@code Position}.
     *
     * @param evaluationContext
     *            the {@code EvaluationContext} in which an evaluation is taking place
     * @return the {@code Security} held by this {@code Position}
     */
    public default Security getSecurity(EvaluationContext evaluationContext) {
        return evaluationContext.getSecurityProvider().getSecurity(getSecurityKey(),
                evaluationContext);
    }

    /**
     * Obtains the {@code SecurityKey} identifying the {@code Security} held by this
     * {@code Position}.
     *
     * @return the key of the {@code Security} held by this {@code Position}
     */
    public SecurityKey getSecurityKey();

    /**
     * Obtains the tax lots of this {@code Position} as a {@code Stream}.
     *
     * @return a {@code Stream} of this {@code Position}'s constituent tax lots, or the
     *         {@code Position} itself if not applicable
     */
    public default Stream<? extends Position> getTaxLots() {
        return Stream.of(this);
    }
}
