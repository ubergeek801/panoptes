package org.slaq.slaqworx.panoptes.asset;

/**
 * PositionKey is a key used to reference Positions.
 *
 * @author jeremy
 */
public class PositionKey extends IdVersionKey {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new PositionKey with the given ID and version.
     *
     * @param id
     *            the ID to assign to the key, or null to generate one
     * @param version
     *            the version to assign to the key
     */
    public PositionKey(String id, long version) {
        super(id, version);
    }
}
