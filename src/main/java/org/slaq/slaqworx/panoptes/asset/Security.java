package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A Security is an investable instrument.
 *
 * @author jeremy
 */
@Entity
public class Security implements Keyed<SecurityKey>, Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private SecurityKey id;

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonBinaryType")
    @Column(columnDefinition = "jsonb")
    private Map<SecurityAttribute<?>, ? super Object> attributes;

    // while the Map is more convenient, attribute lookups are a very hot piece of code during Rule
    // evaluation, and an array lookup speeds things up by ~13%, so an ArrayList is used for lookups
    @Transient
    private ArrayList<? super Object> attributeList = new ArrayList<>();

    /**
     * Creates a new Security with the given key and SecurityAttribute values.
     *
     * @param id
     *            the unique ID identifying the Security
     * @param attributes
     *            a (possibly empty) Map of SecurityAttribute to attribute value
     */
    public Security(SecurityKey id, Map<SecurityAttribute<?>, ? super Object> attributes) {
        this.id = id;
        this.attributes = attributes;
        attributes.forEach((a, v) -> {
            attributeList.ensureCapacity(a.getIndex() + 1);
            while (attributeList.size() < a.getIndex() + 1) {
                attributeList.add(null);
            }
            attributeList.set(a.getIndex(), v);
        });
    }

    /**
     * Creates a new Security. Restricted because this should only be used by Hibernate.
     */
    protected Security() {
        // nothing to do
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
        return id.equals(other.id);
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
            T attributeValue = (T)attributeList.get(attribute.getIndex());

            return attributeValue;
        } catch (IndexOutOfBoundsException e) {
            // this attribute must not exist; prevent future IndexOutOfBoundsExceptions
            attributeList.ensureCapacity(attribute.getIndex() + 1);
            while (attributeList.size() < attribute.getIndex() + 1) {
                attributeList.add(null);
            }
            return null;
        }
    }

    @Override
    public SecurityKey getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Security[" + id + "]";
    }
}
