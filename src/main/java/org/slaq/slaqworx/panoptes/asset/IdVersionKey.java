package org.slaq.slaqworx.panoptes.asset;

import java.util.UUID;

/**
 * IdVersionKey is a key used to reference some asset types (using trivial subclasses).
 *
 * @author jeremy
 */
public abstract class IdVersionKey {
    /**
     * Obtains a generated ID of the form used by an IdVersionKey.
     *
     * @return a generated ID
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    private String id;
    private long version;

    /**
     * Creates a new IdVersionKey with the given ID and version.
     *
     * @param id
     *            the ID to assign to the key, or null to generate one
     * @param version
     *            the version to assign to the key
     */
    public IdVersionKey(String id, long version) {
        this.id = (id == null ? generateId() : id);
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IdVersionKey)) {
            return false;
        }
        IdVersionKey other = (IdVersionKey)obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

    /**
     * Obtains this key's ID.
     *
     * @return the ID portion of the key
     */
    public String getId() {
        return id;
    }

    /**
     * Obtains this key's version.
     *
     * @return the version portion of the key
     */
    public long getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (int)(version ^ (version >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return id + ":" + version;
    }
}
