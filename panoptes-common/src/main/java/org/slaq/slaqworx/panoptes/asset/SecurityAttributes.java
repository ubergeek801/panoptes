package org.slaq.slaqworx.panoptes.asset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slaq.slaqworx.panoptes.util.SerializerUtil;

/**
 * A container for {@code SecurityAttribute}s which provides type-safe access when referencing an
 * attribute by its type. It also enforces immutability of a {@code Security}'s attributes.
 *
 * @author jeremy
 */
public class SecurityAttributes implements Serializable {
    private static final long serialVersionUID = 1L;

    // while a Map is more convenient, attribute lookups are a very hot piece of code during Rule
    // evaluation, and an array lookup speeds things up by ~13%, so an ArrayList is used for lookups
    private final ArrayList<? super Object> attributeValues = new ArrayList<>();

    private String hash = null;

    /**
     * Creates a new {@code SecurityAttributes} container of the given attributes.
     *
     * @param attributes
     *            a {@code Map} of {@code SecurityAttribute} to attribute value
     */
    public SecurityAttributes(Map<SecurityAttribute<?>, ? super Object> attributes) {
        attributes.forEach((a, v) -> {
            attributeValues.ensureCapacity(a.getIndex() + 1);
            while (attributeValues.size() < a.getIndex() + 1) {
                attributeValues.add(null);
            }
            attributeValues.set(a.getIndex(), v);
        });
    }

    /**
     * Obtains the {@code SecurityAttributes} as a {@code Map}. This can be a somewhat expensive
     * operation if there are a lot of attributes; currently its only expected use is when
     * serializing a {@code Security}.
     *
     * @return a {@code Map} of {@code SecurityAttribute} to value
     */
    public Map<SecurityAttribute<?>, ? super Object> asMap() {
        return IntStream.range(0, attributeValues.size()).boxed()
                .filter(i -> attributeValues.get(i) != null)
                .collect(Collectors.toMap(SecurityAttribute::of, i -> attributeValues.get(i)));
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
        SecurityAttributes other = (SecurityAttributes)obj;

        return hash().equals(other.hash());
    }

    /**
     * Obtains the value of the specified attribute index. This form of {@code getValue()} is
     * intended for the rare cases when the index is already known.
     *
     * @param attributeIndex
     *            the index corresponding to the associated {@code SecurityAttribute}
     * @return the value of the given attribute, or {@code null} if not assigned
     */
    public Object getValue(int attributeIndex) {
        if (attributeIndex >= attributeValues.size()) {
            // the attribute is not assigned
            return null;
        }

        return attributeValues.get(attributeIndex);
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
    public <T> T getValue(SecurityAttribute<T> attribute) {
        @SuppressWarnings("unchecked") T value = (T)getValue(attribute.getIndex());
        return value;
    }

    /**
     * Produces a hash of the attribute values.
     *
     * @return the calculated hash value
     */
    public String hash() {
        // lazily calculate the hash; no need to worry about race conditions with read-only data

        if (hash != null) {
            return hash;
        }

        // serialize the attribute collection contents
        ByteArrayOutputStream attributeBytes = new ByteArrayOutputStream();
        for (Object attributeValue : attributeValues) {
            Object v = (attributeValue == null ? "" : attributeValue);

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
        hash = Base64.getEncoder().encodeToString(sha256.digest(attributeBytes.toByteArray()));
        return hash;
    }

    @Override
    public int hashCode() {
        return hash().hashCode();
    }

    @Override
    public String toString() {
        return asSortedMap().toString();
    }

    /**
     * Obtains the {@code SecurityAttributes} as a {@code SortedMap}. Reliance on this method should
     * be limited to diagnostic purposes such as {@code toString()}.
     *
     * @return a {@code SortedMap} of {@code SecurityAttribute} to value, sorted on attribute name
     */
    protected SortedMap<SecurityAttribute<?>, ? super Object> asSortedMap() {
        return IntStream.range(0, attributeValues.size()).boxed()
                .filter(i -> attributeValues.get(i) != null).collect(Collectors
                        .toMap(SecurityAttribute::of, i -> attributeValues.get(i), (v1, v2) -> {
                            throw new RuntimeException(
                                    String.format("Duplicate key for values %s and %s", v1, v2));
                        }, () -> new TreeMap<>((a1, a2) -> a1.compareTo(a2))));
    }
}
