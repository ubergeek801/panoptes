package org.slaq.slaqworx.panoptes.asset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A Security is an investable instrument. Unlike most other asset-related entities, a Security is
 * implicitly "versioned" by hashing its attributes: the resulting hash is used as the primary key.
 * Thus when a Security changes (due to a change in some analytic field such as yield or rating),
 * the new version will use a different hash as the ID.
 *
 * @author jeremy
 */
public class Security implements Keyed<SecurityKey>, Serializable {
    private static final long serialVersionUID = 1L;

    private SecurityKey key;

    // while the Map is more convenient, attribute lookups are a very hot piece of code during Rule
    // evaluation, and an array lookup speeds things up by ~13%, so an ArrayList is used for lookups
    private ArrayList<? super Object> attributeValues = new ArrayList<>();

    /**
     * Creates a new Security with the given key and SecurityAttribute values. The ID is calculated
     * from a hash of the attributes.
     *
     * @param attributes
     *            a (possibly empty) Map of SecurityAttribute to attribute value
     */
    public Security(Map<SecurityAttribute<?>, ? super Object> attributes) {
        key = new SecurityKey(hash(attributes));

        attributes.forEach((a, v) -> {
            attributeValues.ensureCapacity(a.getIndex() + 1);
            while (attributeValues.size() < a.getIndex() + 1) {
                attributeValues.add(null);
            }
            attributeValues.set(a.getIndex(), v);
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
     * Obtains this Security's attributes as a Map. This can be a somewhat expensive operation if
     * there are a lot of attributes; currently its only expected use is when serializing a
     * Security.
     *
     * @return a Map of SecurityAttribute to value
     */
    public Map<SecurityAttribute<?>, ? super Object> getAttributes() {
        HashMap<SecurityAttribute<?>, ? super Object> attributeMap =
                new HashMap<>(attributeValues.size() * 2);
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

    @Override
    public String toString() {
        return "Security[" + key + "]";
    }

    /**
     * Produces a hash of the given attributes.
     *
     * @param attributes
     *            the SecurityAttributes from which to compute the hash
     * @return the calculated hash value
     */
    protected String hash(Map<SecurityAttribute<?>, ? super Object> attributes) {
        // sort the attributes in a stable order
        TreeMap<SecurityAttribute<?>, ? super Object> sortedAttributes =
                new TreeMap<>((a1, a2) -> a1.getName().compareTo(a2.getName()));
        sortedAttributes.putAll(attributes);

        // serialize the attribute map contents, using Java serialization only where necessary
        ByteArrayOutputStream attributeBytes = new ByteArrayOutputStream();
        sortedAttributes.forEach((a, v) -> {
            try {
                attributeBytes.write(a.getName().getBytes());
                attributeBytes.write('=');
                new ObjectOutputStream(attributeBytes).writeObject(v);
            } catch (IOException e) {
                // FIXME find a better way to handle this
                throw new RuntimeException("could not serialize " + v.getClass(), e);
            }
            attributeBytes.write(';');
        });

        // compute the hash on the serialized data
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // FIXME find a better way to handle this
            throw new RuntimeException("could not get SHA-256 algorithm", e);
        }

        // return the hash in base64
        return Base64.getEncoder().encodeToString(sha256.digest(attributeBytes.toByteArray()));
    }
}
