package org.slaq.slaqworx.panoptes.asset;

/**
 * {@code SecurityKey} is a key used to reference {@code Securities}.
 *
 * @author jeremy
 */
public class SecurityKey {
    private final String id;

    /**
     * Creates a new {@code SecurityKey} with the given ID.
     *
     * @param id
     *            the ID to assign to the key
     */
    public SecurityKey(String id) {
        this.id = id;
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
        SecurityKey other = (SecurityKey)obj;
        return id.equals(other.id);
    }

    /**
     * Obtains this {@code SecurityKey}'s ID.
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
        return id;
    }
}
