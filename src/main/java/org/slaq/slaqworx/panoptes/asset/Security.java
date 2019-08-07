package org.slaq.slaqworx.panoptes.asset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slaq.slaqworx.panoptes.serializer.SerializerUtil;
import org.slaq.slaqworx.panoptes.util.Keyed;

import groovy.lang.MissingPropertyException;

/**
 * A Security is an investable instrument. Unlike most other asset-related entities, a Security is
 * implicitly "versioned" by hashing its attributes: the resulting hash is used as the primary key.
 * Thus when a Security changes (due to a change in some analytic field such as yield or rating),
 * the new version will use a different hash as the ID.
 *
 * @author jeremy
 */
public class Security implements Keyed<SecurityKey> {
    private final SecurityKey key;

    // while the Map is more convenient, attribute lookups are a very hot piece of code during Rule
    // evaluation, and an array lookup speeds things up by ~13%, so an ArrayList is used for lookups
    private final ArrayList<? super Object> attributeValues = new ArrayList<>();

    /**
     * Creates a new Security with the given key and SecurityAttribute values. The ID is calculated
     * from a hash of the attributes.
     *
     * @param attributes
     *            a (possibly empty) Map of SecurityAttribute to attribute value
     */
    public Security(Map<SecurityAttribute<?>, ? super Object> attributes) {
        attributes.forEach((a, v) -> {
            attributeValues.ensureCapacity(a.getIndex() + 1);
            while (attributeValues.size() < a.getIndex() + 1) {
                attributeValues.add(null);
            }
            attributeValues.set(a.getIndex(), v);
        });

        key = new SecurityKey(hash(attributeValues));
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
     * Obtains this Security's attributes as a Map. This can be a somewhat expensive operation if
     * there are a lot of attributes; currently its only expected use is when serializing a
     * Security.
     *
     * @return a Map of SecurityAttribute to value
     */
    public Map<SecurityAttribute<?>, ? super Object> getAttributes() {
        return IntStream.range(0, attributeValues.size()).boxed()
                .filter(i -> attributeValues.get(i) != null).collect(Collectors
                        .toMap(i -> SecurityAttribute.of(i), i -> attributeValues.get(i)));
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
        if (attribute == null) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            T attributeValue = (T)attributeValues.get(attribute.getIndex());

            return attributeValue;
        } catch (IndexOutOfBoundsException e) {
            // this attribute must not exist; prevent future IndexOutOfBoundsExceptions
            attributeValues.ensureCapacity(attribute.getIndex() + 1);
            while (attributeValues.size() < attribute.getIndex() + 1) {
                attributeValues.add(null);
            }
            return null;
        }
    }

    @Override
    public SecurityKey getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Provided as a convenience to Groovy filters to allow shorthand SecurityAttribute access.
     *
     * @param name
     *            the name of the SecurityAttribute to resolve
     * @return the value of the attribute if it exists, or null if it does not
     */
    public Object propertyMissing(String name) {
        SecurityAttribute<?> attribute = SecurityAttribute.of(name);
        if (attribute == null) {
            throw new MissingPropertyException(name);
        }

        return getAttributeValue(attribute);
    }

    @Override
    public String toString() {
        return "Security[" + key + "]";
    }

    /**
     * Produces a hash of the given attribute values.
     *
     * @param attributeValues
     *            the SecurityAttribute values from which to compute the hash
     * @return the calculated hash value
     */
    protected String hash(ArrayList<Object> attributeValues) {
        // serialize the attribute collection contents
        ByteArrayOutputStream attributeBytes = new ByteArrayOutputStream();
        for (int i = 0; i < attributeValues.size(); i++) {
            Object v = (attributeValues.get(i) == null ? "" : attributeValues.get(i));

            try {
                attributeBytes
                        .write(SerializerUtil.defaultJsonMapper().writeValueAsString(v).getBytes());
            } catch (IOException e) {
                // TODO throw a better exception
                throw new RuntimeException("could not serialize " + v.getClass(), e);
            }
            attributeBytes.write(';');
        }

        // compute the hash on the serialized data
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // TODO throw a better exception
            throw new RuntimeException("could not get SHA-256 algorithm", e);
        }

        // return the hash in base64
        return Base64.getEncoder().encodeToString(sha256.digest(attributeBytes.toByteArray()));
    }
}
