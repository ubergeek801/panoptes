package org.slaq.slaqworx.panoptes.rule;

import java.io.Serializable;

import org.slaq.slaqworx.panoptes.asset.IdVersionKey;

/**
 * RuleKey is a key used to reference Rules.
 *
 * @author jeremy
 */
public class RuleKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;

    /**
     * Creates a new RuleKey with the given ID.
     *
     * @param id
     *            the ID to assign to the key, or null to generate one
     */
    public RuleKey(String id) {
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
        RuleKey other = (RuleKey)obj;
        return id.equals(other.id);
    }

    /**
     * Obtains this RuleKey ID.
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
        return "RuleKey[" + id + "]";
    }
}
