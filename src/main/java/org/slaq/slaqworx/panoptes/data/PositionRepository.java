package org.slaq.slaqworx.panoptes.data;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;

/**
 * PositionRepository is a CrudRepository used to access Position data.
 *
 * @author jeremy
 */
public interface PositionRepository extends CrudRepositoryWithAllIdsQuery<Position, PositionKey> {
    // trivial extension
}
