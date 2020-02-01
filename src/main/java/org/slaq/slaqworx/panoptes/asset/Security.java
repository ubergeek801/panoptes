package org.slaq.slaqworx.panoptes.asset;

import java.util.Map;

import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A {@code Security} is an investable instrument. Unlike most other asset-related entities, a
 * {@code Security} is implicitly "versioned" by hashing its attributes: the resulting hash is used
 * as an alternate key. Thus when a {@code Security} changes (due to a change in some analytic field
 * such as yield or rating), the new version will use a different hash as the alternate key.
 *
 * @author jeremy
 */
public class Security implements Keyed<SecurityKey> {
    private final SecurityKey key;
    private final SecurityAttributes attributes;

    /**
     * Creates a new {@code Security} with the given {@code SecurityAttribute} values. The key is
     * taken from the attribute containing the ISIN; this is the only attribute that is required.
     *
     * @param attributes
     *            a {@code Map} of {@code SecurityAttribute} to attribute value
     */
    public Security(Map<SecurityAttribute<?>, ? super Object> attributes) {
        this.attributes = new SecurityAttributes(attributes);
        String assetId = (String)attributes.get(SecurityAttribute.isin);
        if (assetId == null) {
            throw new IllegalArgumentException("SecurityAttribute.isin cannot be null");
        }
        key = new SecurityKey(assetId);
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
        return attributes.equals(other.attributes);
    }

    /**
     * Obtains the {@code Security}'s attributes.
     *
     * @return a {@code SecurityAttributes} comprising this {@code Security}'s attributes
     */
    public SecurityAttributes getAttributes() {
        return attributes;
    }

    /**
     * Obtains the value of the specified attribute index. This form of {@code getAttributeValue()}
     * is intended for the rare cases when the index is already known.
     *
     * @param attributeIndex
     *            the index corresponding to the associated {@code SecurityAttribute}
     * @param context
     *            the {@code EvaluationContext} in which the attribute value is being retrieved
     * @return the value of the given attribute, or {@code null} if not assigned
     */
    public Object getAttributeValue(int attributeIndex, EvaluationContext context) {
        SecurityAttributes overrideAttributes = context.getSecurityOverrides().get(key);
        if (overrideAttributes != null) {
            Object overrideValue = overrideAttributes.getValue(attributeIndex);
            if (overrideValue != null) {
                return overrideValue;
            }
        }

        return attributes.getValue(attributeIndex);
    }

    /**
     * Obtains the value of the specified attribute.
     *
     * @param <T>
     *            the expected type of the attribute value
     * @param attribute
     *            the {@code SecurityAttribute} identifying the attribute
     * @param context
     *            the {@code EvaluationContext} in which the attribute value is being retrieved
     * @return the value of the given attribute, or {@code null} if not assigned
     */
    public <T> T getAttributeValue(SecurityAttribute<T> attribute, EvaluationContext context) {
        SecurityAttributes overrideAttributes = context.getSecurityOverrides().get(key);
        if (overrideAttributes != null) {
            T overrideValue = overrideAttributes.getValue(attribute);
            if (overrideValue != null) {
                return overrideValue;
            }
        }

        return attributes.getValue(attribute);
    }

    @Override
    public SecurityKey getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return attributes.hashCode();
    }

    @Override
    public String toString() {
        return "Security[" + key + "]";
    }
}
