package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * A Security is an investable instrument.
 *
 * @author jeremy
 */
public class Security implements Serializable {
    private static final long serialVersionUID = 1L;

    private final SecurityKey key;
    // while a HashMap would be more convenient, attribute lookups are a very hot piece of code
    // during Rule evaluation, and an array lookup speeds things up by ~13%, so ArrayList it is
    private final ArrayList<? super Object> attributes = new ArrayList<>();

    /**
     * Creates a new Security with the given key and SecurityAttribute values.
     *
     * @param key
     *            the unique ID identifying the Security
     * @param attributes
     *            a (possibly empty) Map of SecurityAttribute to attribute value
     */
    public Security(SecurityKey key, Map<SecurityAttribute<?>, ? super Object> attributes) {
        this.key = key;
        attributes.forEach((a, v) -> {
            this.attributes.ensureCapacity(a.getIndex() + 1);
            while (this.attributes.size() < a.getIndex() + 1) {
                this.attributes.add(null);
            }
            this.attributes.set(a.getIndex(), v);
        });
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
        Security other = (Security)obj;
        return key.equals(other.key);
    }

    /**
     * Obtains the value of the specified attribute.
     *
     * @param <T>
     *            the expected type of the attribute value
     * @param attribute
     *            the SecurityAttribute identifying the attribute
     * @return the value of the given attribute, or null if not assigned
     */
    public <T> T getAttributeValue(SecurityAttribute<T> attribute) {
        try {
            @SuppressWarnings("unchecked")
            T attributeValue = (T)attributes.get(attribute.getIndex());

            return attributeValue;
        } catch (IndexOutOfBoundsException e) {
            // this attribute must not exist; prevent future IndexOutOfBoundsExceptions
            attributes.ensureCapacity(attribute.getIndex() + 1);
            while (attributes.size() < attribute.getIndex() + 1) {
                attributes.add(null);
            }
            return null;
        }
    }

    /**
     * Obtains the unique key identifying this Security.
     *
     * @return the SecurityKey
     */
    public SecurityKey getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "Security[" + key + "]";
    }
}
