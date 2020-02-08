package org.slaq.slaqworx.panoptes.asset;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * {@code AbstractPosition} is a partial implementation of {@code Position} provided to facilitate
 * full {@code Position} implementations.
 *
 * @author jeremy
 */
public abstract class AbstractPosition implements Position {
    private final PositionKey key;

    /**
     * Creates a new {@code AbstractPosition} with a generated key.
     */
    protected AbstractPosition() {
        this(null);
    }

    /**
     * Creates a new {@code AbstractPosition} with the specified key.
     *
     * @param key
     *            the {@code PositionKey} identifying this {@code Position}
     */
    protected AbstractPosition(PositionKey key) {
        this.key = (key == null ? new PositionKey(null) : key);
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

    @Override
    public PositionKey getKey() {
        return key;
    }

    @Override
    public Stream<? extends Position> getLookthroughPositions() {
        // FIXME implement getLookthroughPositions()
        return Stream.of(this);
    }

    @Override
    public double getMarketValue(EvaluationContext evaluationContext) {
        BigDecimal price = getAttributeValue(SecurityAttribute.price, evaluationContext);
        return price.multiply(BigDecimal.valueOf(getAmount())).doubleValue();
    }

    @Override
    public Stream<? extends Position> getTaxLots() {
        return Stream.of(this);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
