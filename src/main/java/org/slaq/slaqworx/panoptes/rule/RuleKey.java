package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.IdVersionKey;

/**
 * RuleKey is a key used to reference Rules.
 *
 * @author jeremy
 */
public class RuleKey extends IdVersionKey {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new RuleKey with the given ID and version.
     *
     * @param id
     *            the ID to assign to the key, or null to generate one
     * @param version
     *            the version to assign to the key
     */
    public RuleKey(String id, long version) {
        super(id, version);
    }
}
