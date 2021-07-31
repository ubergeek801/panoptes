package org.slaq.slaqworx.panoptes.asset;

import javax.annotation.Nonnull;

/**
 * A pseudo-{@link Position} typically arising from an indirect holding of a {@link Security}. For
 * example, if {@link Portfolio} A holds some amount of {@link Portfolio} B, and {@link Portfolio} B
 * holds some amount of {@link Security} S, then {@link Portfolio} A indirectly holds some amount of
 * S, where that amount is given by (B's amount in S) * (A's shares of B) / (total shares of B). The
 * quotient portion of this equation is taken to be the {@code scale}.
 * <p>
 * Indirect holdings may be transitive, meaning that the above example could be extended with {@link
 * Portfolio} C, D, etc. The relationships between held {@link Portfolio}s form a directed acyclic
 * graph, where the "acyclic" property is assumed.
 *
 * @author jeremy
 */
public class ScaledPosition extends AbstractPosition {
  @Nonnull
  private final Position sourcePosition;
  private final double scaledAmount;

  /**
   * Creates a new {@link ScaledPosition} which represents a fractional (scaled) share of the source
   * {@link Position}.
   *
   * @param sourcePosition
   *     the {@link Position} of which this {@link ScaledPosition} represents a fractional share
   * @param scale
   *     the fraction of the source {@link Position} represented by this {@link ScaledPosition}
   */
  public ScaledPosition(@Nonnull Position sourcePosition, double scale) {
    this.sourcePosition = sourcePosition;
    scaledAmount = sourcePosition.getAmount() * scale;
  }

  @Override
  public double getAmount() {
    return scaledAmount;
  }

  @Override
  @Nonnull
  public SecurityKey getSecurityKey() {
    return sourcePosition.getSecurityKey();
  }
}
