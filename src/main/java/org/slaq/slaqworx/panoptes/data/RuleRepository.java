package org.slaq.slaqworx.panoptes.data;

import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * RuleRepository is a CrudRepository used to access Rule data.
 *
 * @author jeremy
 */
public interface RuleRepository extends CrudRepositoryWithAllIdsQuery<Rule, RuleKey> {
    // trivial extension
}
