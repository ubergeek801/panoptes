package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;

/**
 * PositionKey is a key used to reference Positions.
 *
 * @author jeremy
 */
public class PositionKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;

    /**
     * Creates a new PositionKey with the given ID.
     *
     * @param id
     *            the ID to assign to the key, or null to generate one
     */
    public PositionKey(String id) {
        this.id = (id == null ? IdVersionKey.generateId() : id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PositionKey other = (PositionKey)obj;
        return id.equals(other.id);
    }

    /**
     * Obtains this PositionKey ID.
     *
     * @return the ID underlying this key
     */
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "SecurityKey[" + id + "]";
    }
}
