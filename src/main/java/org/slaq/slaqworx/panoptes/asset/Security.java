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

    private final String assetId;
    // while a HashMap would be more convenient, attribute lookups are a very hot piece of code
    // during Rule evaluation, and an array lookup speeds things up by ~13%, so ArrayList it is
    private final ArrayList<? super Object> attributes = new ArrayList<>();

    /**
     * Creates a new Security with the given asset ID and SecurityAttribute values.
     *
     * @param assetId
     *            the unique ID identifying the Security
     * @param attributes
     *            a (possibly empty) Map of SecurityAttribute to attribute value
     */
    public Security(String assetId, Map<SecurityAttribute<?>, ? super Object> attributes) {
        this.assetId = assetId;
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
        return assetId.equals(other.assetId);
    }

    /**
     * Obtains the unique asset ID identifying this Security.
     *
     * @return the asset ID
     */
    public String getAssetId() {
        return assetId;
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

    @Override
    public int hashCode() {
        return assetId.hashCode();
    }
}
