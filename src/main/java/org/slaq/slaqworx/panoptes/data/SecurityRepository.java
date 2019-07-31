package org.slaq.slaqworx.panoptes.data;

import org.slaq.slaqworx.panoptes.asset.Security;

/**
 * SecurityRepository is a CrudRepository used to access Security data.
 *
 * @author jeremy
 */
public interface SecurityRepository extends CrudRepositoryWithAllIdsQuery<Security, String> {
    // trivial extension
}
