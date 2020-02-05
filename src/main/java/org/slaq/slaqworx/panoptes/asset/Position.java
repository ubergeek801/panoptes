package org.slaq.slaqworx.panoptes.asset;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A {@code Position} is a holding of some amount of a particular {@code Security} by some
 * {@code Portfolio}. A {@code Position} may be durable (e.g. sourced from a database/cache) or
 * ephemeral (e.g. supplied by a proposed {@code Trade} or even a unit test).
 * <p>
 * This {@code Position} model is currently oversimplified in that it ignores the notion of tax
 * lots, which in a {@code Portfolio} are the individual transaction effects over which the
 * {@code Portfolio}'s {@code Position} is an aggregate.
 *
 * @author jeremy
 */
public class Position implements Keyed<PositionKey> {
    private final PositionKey key;
    private final double amount;
    private final SecurityKey securityKey;

    /**
     * Creates a new {@code Position} with a generated key and the specified amount and
     * {@code Security}.
     *
     * @param amount
     *            the amount of the {@code Security} held in this {@code Position}
     * @param securityKey
     *            a {@code SecurityKey} identifying the held {@code Security}
     */
    public Position(double amount, SecurityKey securityKey) {
        this(null, amount, securityKey);
    }

    /**
     * Creates a new {@code Position} with the specified ID, amount and {@code Security}.
     *
     * @param key
     *            the unique key to assign to this {@code Position}, or {@code null} to generate one
     * @param amount
     *            the amount of the {@code Security} held in this {@code Position}
     * @param securityKey
     *            a {@code SecurityKey} identifying the held {@code Security}
     */
    public Position(PositionKey key, double amount, SecurityKey securityKey) {
        this.key = (key == null ? new PositionKey(null) : key);
        this.amount = amount;
        this.securityKey = securityKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Position)) {
            return false;
        }
        Position other = (Position)obj;

        return key.equals(other.getKey());
    }

    /**
     * Obtains the amount held by this {@code Position}.
     *
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Obtains the value of the specified attribute of this {@code Position}'s held
     * {@code Security}. This is merely a convenience method to avoid an intermediate call to
     * {@code getSecurity()}.
     *
     * @param <T>
     *            the expected type of the attribute value
     * @param attribute
     *            the {@code SecurityAttribute} identifying the attribute
     * @param context
     *            the {@code EvaluationContext} in which the attribute value is being retrieved
     * @return the value of the given attribute, or {@code null} if not assigned
     */
    public <T> T getAttributeValue(SecurityAttribute<T> attribute, EvaluationContext context) {
        return getSecurity(context).getAttributeValue(attribute, context);
    }

    @Override
    public PositionKey getKey() {
        return key;
    }

    /**
     * Obtains the lookthrough {@code Position}s of this {@code Position} as a {@code Stream}.
     *
     * @return a {@code Stream} of this {@code Position}'s lookthrough {@code Position}s, or the
     *         {@code Position} itself if not applicable
     */
    public Stream<Position> getLookthroughPositions() {
        // FIXME implement getLookthroughPositions()
        return Stream.of(this);
    }

    /**
     * Obtains the market value of this {@code Position}.
     *
     * @param evaluationContext
     *            the {@code EvaluationContext} in which to obtain the market value
     * @return the market value
     */
    public double getMarketValue(EvaluationContext evaluationContext) {
        BigDecimal price = getAttributeValue(SecurityAttribute.price, evaluationContext);
        return price.multiply(BigDecimal.valueOf(amount)).doubleValue();
    }

    /**
     * Obtains the {@code Security} held by this {@code Position}.
     *
     * @param context
     *            the {@code EvaluationContext} in which an evaluation is taking place
     * @return the {@code Security} held by this {@code Position}
     */
    public Security getSecurity(EvaluationContext context) {
        return context.getSecurityProvider().getSecurity(securityKey, context);
    }

    /**
     * Obtains the {@code SecurityKey} identifying the {@code Security} held by this
     * {@code Position}.
     *
     * @return the key of the {@code Security} held by this {@code Position}
     */
    public SecurityKey getSecurityKey() {
        return securityKey;
    }

    /**
     * Obtains the tax lots of this {@code Position} as a {@code Stream}.
     *
     * @return a {@code Stream} of this {@code Position}'s constituent tax lots, or the
     *         {@code Position} itself if not applicable
     */
    public Stream<? extends Position> getTaxlots() {
        // FIXME implement getTaxlots()
        return Stream.of(this);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
