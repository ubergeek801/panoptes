package org.slaq.slaqworx.panoptes.asset;

import java.util.stream.Stream;

/**
 * A PositionSupplier supplies Positions. An implementor might be a customer portfolio or a "raw"
 * set of Positions. A PositionSupplier may provide access to a related Portfolio (which may be
 * itself), but note that the PositionSupplier's members may not be the same as the related
 * Portfolio's (the supplier may, for example, provide access to a filtered set).
 *
 * @author jeremy
 */
public interface PositionSupplier {
    /**
     * Obtains this PositionSupplier's related Portfolio, if any.
     *
     * @return the related Portfolio, or null if it does not exist
     */
    public Portfolio getPortfolio();

    /**
     * Obtains this PositionSupplier Positions as a (new) Stream.
     *
     * @return a Stream of Positions
     */
    public Stream<Position> getPositions();

    /**
     * Obtains the sum of the Position amounts of this PositionSupplier.
     *
     * @return the sum of Position amounts
     */
    public double getTotalAmount();

    /**
     * Obtains the number of Positions in this PositionSupplier.
     *
     * @return the number of Positions
     */
    public int size();
}
