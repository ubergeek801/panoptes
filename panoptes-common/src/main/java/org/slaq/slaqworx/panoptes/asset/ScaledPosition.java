package org.slaq.slaqworx.panoptes.asset;

/**
 * A {@code ScaledPosition} is a pseudo-{@code Position} typically arising from an indirect holding
 * of a {@code Security}. For example, if {@code Portfolio} A holds some amount of {@code Portfolio}
 * B, and {@code Portfolio} B holds some amount of {@code Security} S, then {@code Portfolio} A
 * indirectly holds some amount of S, where that amount is given by (B's amount in S) * (A's shares
 * of B) / (total shares of B). The quotient portion of this equation is taken to be the
 * {@code scale}.
 * <p>
 * Indirect holdings may be transitive, meaning that the above example could be extended with
 * {@code Portfolio} C, D, etc. The relationships between held {@code Portfolio}s form a directed
 * acyclic graph, where the "acyclic" property is assumed.
 *
 * @author jeremy
 */
public class ScaledPosition extends AbstractPosition {
    private final Position sourcePosition;
    private final double scaledAmount;

    /**
     * Creates a new {@code ScaledPosition} which represents a fractional (scaled) share of the
     * source {@code Position}.
     *
     * @param sourcePosition
     *            the {@code Position} of which this {@code ScaledPosition} represents a fractional
     *            share
     * @param scale
     *            the fraction of the source {@code Position} represented by this
     *            {@code ScaledPosition}
     */
    public ScaledPosition(Position sourcePosition, double scale) {
        this.sourcePosition = sourcePosition;
        scaledAmount = sourcePosition.getAmount() * scale;
    }

    @Override
    public double getAmount() {
        return scaledAmount;
    }

    @Override
    public SecurityKey getSecurityKey() {
        return sourcePosition.getSecurityKey();
    }
}
