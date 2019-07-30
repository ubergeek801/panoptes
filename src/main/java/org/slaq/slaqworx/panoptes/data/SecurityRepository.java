package org.slaq.slaqworx.panoptes.data;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * SecurityRepository is a CrudRepository used to access Security data.
 *
 * @author jeremy
 */
public interface SecurityRepository extends CrudRepositoryWithAllIdsQuery<Security, SecurityKey> {
    // trivial extension
}
