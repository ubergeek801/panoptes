package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;
import java.util.UUID;

/**
 * AssetKey is a key used to reference most asset types (using trivial subclasses).
 *
 * @author jeremy
 */
public abstract class AssetKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private long version;

    /**
     * Creates a new AssetKey with the given ID and version.
     *
     * @param id
     *            the ID to assign to the key, or null to generate one
     * @param version
     *            the version to assign to the key
     */
    public AssetKey(String id, long version) {
        this.id = (id == null ? UUID.randomUUID().toString() : id);
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
        if (!(obj instanceof AssetKey)) {
            return false;
        }
        AssetKey other = (AssetKey)obj;
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
        return getClass().getSimpleName() + "[" + id + ":" + version + "]";
    }
}
