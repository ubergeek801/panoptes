package org.slaq.slaqworx.panoptes.asset;

/**
 * SecurityKey is a key used to reference Securities.
 *
 * @author jeremy
 */
public class SecurityKey extends IdVersionKey {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new SecurityKey with the given ID and version.
     *
     * @param id
     *            the ID to assign to the key, or null to generate one
     * @param version
     *            the version to assign to the key
     */
    public SecurityKey(String id, long version) {
        super(id, version);
    }

    /**
     * Creates a new SecurityKey. Restricted because this should only be used by Hibernate.
     */
    protected SecurityKey() {
        // nothing to do
    }
}
