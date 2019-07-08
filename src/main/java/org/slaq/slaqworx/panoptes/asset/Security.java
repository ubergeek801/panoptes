package org.slaq.slaqworx.panoptes.asset;

import java.util.HashMap;
import java.util.Map;

/**
 * A Security is an investable instrument.
 *
 * @author jeremy
 */
public class Security {
    private final String assetId;
    private final Map<SecurityAttribute<?>, ? super Object> attributes = new HashMap<>();

    /**
     * Creates a new Security with the given asset ID and SecurityAttribute values.
     * 
     * @param assetId    the unique ID identifying the Security
     * @param attributes a (possibly empty) Map of SecurityAttribute to attribute value
     */
    public Security(String assetId, Map<SecurityAttribute<?>, ? super Object> attributes) {
        this.assetId = assetId;
        this.attributes.putAll(attributes);
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
        Security other = (Security) obj;
        if (assetId == null) {
            if (other.assetId != null) {
                return false;
            }
        } else if (!assetId.equals(other.assetId)) {
            return false;
        }
        return true;
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
     * @param <T>       the expected type of the attribute value
     * @param attribute the SecurityAttribute identifying the attribute
     * @return the value of the given attribute, or null if not assigned
     */
    public <T> T getAttributeValue(SecurityAttribute<T> attribute) {
        @SuppressWarnings("unchecked")
        T attributeValue = (T) attributes.get(attribute);

        return attributeValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((assetId == null) ? 0 : assetId.hashCode());
        return result;
    }
}
