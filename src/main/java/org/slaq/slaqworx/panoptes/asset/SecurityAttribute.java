package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * A SecurityAttribute identifies a particular attribute of a Security.
 *
 * @author jeremy
 *
 * @param <T>
 *            the type which values of this attribute implement
 */
public class SecurityAttribute<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Map<String, SecurityAttribute<?>> attributes = new HashMap<>(100, 0.5f);

    // some well-known attributes follow, but security attributes are not limited to these

    public static SecurityAttribute<String> isin = SecurityAttribute.of("isin", 0, String.class);
    public static SecurityAttribute<String> description =
            SecurityAttribute.of("description", 1, String.class);
    public static SecurityAttribute<String> country =
            SecurityAttribute.of("country", 2, String.class);
    public static SecurityAttribute<String> region =
            SecurityAttribute.of("region", 3, String.class);
    public static SecurityAttribute<String> sector =
            SecurityAttribute.of("sector", 4, String.class);
    public static SecurityAttribute<String> currency =
            SecurityAttribute.of("currency", 5, String.class);
    public static SecurityAttribute<BigDecimal> coupon =
            SecurityAttribute.of("coupon", 6, BigDecimal.class);
    public static SecurityAttribute<LocalDate> maturityDate =
            SecurityAttribute.of("maturityDate", 7, LocalDate.class);
    public static SecurityAttribute<String> ratingSymbol =
            SecurityAttribute.of("ratingSymbol", 8, String.class);
    public static SecurityAttribute<Double> ratingValue =
            SecurityAttribute.of("ratingValue", 9, Double.class);
    public static SecurityAttribute<BigDecimal> yield =
            SecurityAttribute.of("yield", 10, BigDecimal.class);
    public static SecurityAttribute<Double> duration =
            SecurityAttribute.of("duration", 11, Double.class);
    public static SecurityAttribute<String> issuer =
            SecurityAttribute.of("issuer", 12, String.class);

    /**
     * Obtains (or creates) a SecurityAttribute with the given name and value type.
     *
     * @param <T>
     *            the value type of the SecurityAttribute
     * @param name
     *            the unique name of the SecurityAttribute
     * @param index
     *            the index of the SecurityAttribute in an attributes array
     * @param clazz
     *            the Class of the value type
     * @return an existing SecurityAttribute if already defined, otherwise a new SecurityAttribute
     */
    public static <T> SecurityAttribute<T> of(String name, int index, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        SecurityAttribute<T> attribute = (SecurityAttribute<T>)attributes.computeIfAbsent(name,
                n -> new SecurityAttribute<>(name, index));
        return attribute;
    }

    private final String name;
    private final int index;

    /**
     * Creates a new SecurityAttribute with the given name and index. Restricted to enforce use of
     * the of() factory method.
     *
     * @param name
     *            the unique name of the SecurityAttribute
     */
    private SecurityAttribute(String name, int index) {
        this.name = name;
        this.index = index;
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
        SecurityAttribute<?> other = (SecurityAttribute<?>)obj;

        return name.equals(other.name);
    }

    /**
     * Obtains the index at which this SecurityAttribute may be found in an attributes array.
     *
     * @return the array index for this SecurityAttribute
     */
    public int getIndex() {
        return index;
    }

    /**
     * Obtains the name of this SecurityAttribute.
     *
     * @return the attribute name
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "SecurityAttribute[\"" + name + "\"]";
    }
}
