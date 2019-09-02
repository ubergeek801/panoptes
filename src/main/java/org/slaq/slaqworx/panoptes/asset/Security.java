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
 * A {@code Security} is an investable instrument. Unlike most other asset-related entities, a
 * {@code Security} is implicitly "versioned" by hashing its attributes: the resulting hash is used
 * as the primary key. Thus when a {@code Security} changes (due to a change in some analytic field
 * such as yield or rating), the new version will use a different hash as the ID.
 *
 * @author jeremy
 */
public class Security implements Keyed<SecurityKey> {
    private final SecurityKey key;
    private final String assetId;

    // while a Map is more convenient, attribute lookups are a very hot piece of code during Rule
    // evaluation, and an array lookup speeds things up by ~13%, so an ArrayList is used for lookups
    private final ArrayList<? super Object> attributeValues = new ArrayList<>();

    /**
     * Creates a new {@code Security} with the given key and {@code SecurityAttribute} values. The
     * ID is calculated from a hash of the attributes.
     *
     * @param attributes
     *            a (possibly empty) {@code Map} of {@code SecurityAttribute} to attribute value
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
        assetId = (String)attributes.get(SecurityAttribute.isin);
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
     * Obtains the asset ID (currently defined to be the ISIN) of this {@code Security}.
     *
     * @return the asset ID
     */
    public String getAssetId() {
        return assetId;
    }

    /**
     * Obtains this {@code Security}'s attributes as a {@code Map}. This can be a somewhat expensive
     * operation if there are a lot of attributes; currently its only expected use is when
     * serializing a {@code Security}.
     *
     * @return a {@code Map} of {@code SecurityAttribute} to value
     */
    public Map<SecurityAttribute<?>, ? super Object> getAttributes() {
        return IntStream.range(0, attributeValues.size()).boxed()
                .filter(i -> attributeValues.get(i) != null).collect(Collectors
                        .toMap(i -> SecurityAttribute.of(i), i -> attributeValues.get(i)));
    }

    /**
     * Obtains the value of the specified attribute index. This form of {@code getAttributeValue()}
     * is intended for the rare cases when the index is already known.
     *
     * @param attributeIndex
     *            the index corresponding to the associated {@code SecurityAttribute}
     * @return the value of the given attribute, or {@code null} if not assigned
     */
    public Object getAttributeValue(int attributeIndex) {
        try {
            return attributeValues.get(attributeIndex);
        } catch (IndexOutOfBoundsException e) {
            // this attribute must not exist; prevent future IndexOutOfBoundsExceptions
            attributeValues.ensureCapacity(attributeIndex + 1);
            while (attributeValues.size() < attributeIndex + 1) {
                attributeValues.add(null);
            }
            return null;
        }
    }

    /**
     * Obtains the value of the specified attribute.
     *
     * @param <T>
     *            the expected type of the attribute value
     * @param attribute
     *            the {@code SecurityAttribute} identifying the attribute
     * @return the value of the given attribute, or {@code null} if not assigned
     */
    public <T> T getAttributeValue(SecurityAttribute<T> attribute) {
        if (attribute == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        T attributeValue = (T)getAttributeValue(attribute.getIndex());
        return attributeValue;
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
     * Obtains the value of the {@code SecurityAttribute} with the given name. Provided as a
     * convenience to Groovy filters (and named accordingly) to allow shorthand
     * {@code SecurityAttribute} access. Mostly obviated by compile-time optimizations that have
     * since been made to Groovy filters.
     *
     * @param name
     *            the name of the {@code SecurityAttribute} to resolve
     * @return the value of the attribute if it exists
     * @throws MissingPropertyException
     *             if the specified attribute name is not defined
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
     *            the {@code SecurityAttribute} values from which to compute the hash
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
