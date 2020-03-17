package org.slaq.slaqworx.panoptes.asset;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.slaq.slaqworx.panoptes.rule.ValueProvider;

/**
 * A {@code SecurityAttribute} identifies a particular attribute of a {@code Security}.
 *
 * @author jeremy
 * @param <T>
 *            the type which values of this attribute implement
 */
public class SecurityAttribute<T> implements Comparable<SecurityAttribute<?>> {
    private static final Map<String, SecurityAttribute<?>> attributesByName =
            new HashMap<>(100, 0.5f);
    private static final Map<Integer, SecurityAttribute<?>> attributesByIndex =
            new HashMap<>(100, 0.5f);

    // the "standard" SecurityAttributes; there may be more defined in the database
    public static final SecurityAttribute<String> cusip = of("cusip", 0, String.class, null);
    public static final SecurityAttribute<String> isin = of("isin", 1, String.class, null);
    public static final SecurityAttribute<String> description =
            of("description", 2, String.class, null);
    public static final SecurityAttribute<String> country = of("country", 3, String.class, null);
    public static final SecurityAttribute<String> region = of("region", 4, String.class, null);
    public static final SecurityAttribute<String> sector = of("sector", 5, String.class, null);
    public static final SecurityAttribute<String> currency = of("currency", 6, String.class, null);
    public static final SecurityAttribute<Double> coupon =
            of("coupon", 7, Double.class, ValueProvider.forDouble());
    public static final SecurityAttribute<LocalDate> maturityDate =
            of("maturityDate", 8, LocalDate.class, ValueProvider.forDaysUntilDate());
    public static final SecurityAttribute<Double> yield =
            of("yield", 9, Double.class, ValueProvider.forDouble());
    public static final SecurityAttribute<Double> duration =
            of("duration", 10, Double.class, ValueProvider.forDouble());
    public static final SecurityAttribute<String> issuer = of("issuer", 11, String.class, null);
    public static final SecurityAttribute<Double> price =
            of("price", 12, Double.class, ValueProvider.forDouble());
    public static final SecurityAttribute<PortfolioKey> portfolio =
            of("portfolio", 13, PortfolioKey.class, null);
    public static final SecurityAttribute<Double> amount =
            of("amount", 14, Double.class, ValueProvider.forDouble());
    public static final SecurityAttribute<String> rating1Symbol =
            of("rating1Symbol", 15, String.class, ValueProvider.forRatingSymbol());
    public static final SecurityAttribute<Double> rating1Value =
            of("rating1Value", 16, Double.class, ValueProvider.forDouble());
    public static final SecurityAttribute<String> rating2Symbol =
            of("rating2Symbol", 17, String.class, ValueProvider.forRatingSymbol());
    public static final SecurityAttribute<Double> rating2Value =
            of("rating2Value", 18, Double.class, ValueProvider.forDouble());
    public static final SecurityAttribute<String> rating3Symbol =
            of("rating3Symbol", 19, String.class, ValueProvider.forRatingSymbol());
    public static final SecurityAttribute<Double> rating3Value =
            of("rating3Value", 20, Double.class, ValueProvider.forDouble());

    /**
     * Type-safely creates a {@code Map} of a single {@code SecurityAttribute}.
     *
     * @param <A>
     *            the {@code SecurityAttribute} type
     * @param attribute
     *            the {@code SecurityAttribute}
     * @param value
     *            the attribute value
     * @return a {@code Map} containing the specified attribute
     */
    public static <A> Map<SecurityAttribute<?>, Object> mapOf(SecurityAttribute<A> attribute,
            A value) {
        return Map.of(attribute, value);
    }

    /**
     * Type-safely creates a {@code Map} of {@code SecurityAttribute}s.
     *
     * @param <A>
     *            the first {@code SecurityAttribute} type
     * @param <B>
     *            the second {@code SecurityAttribute} type
     * @param attribute1
     *            the first {@code SecurityAttribute}
     * @param value1
     *            the first attribute value
     * @param attribute2
     *            the second {@code SecurityAttribute}
     * @param value2
     *            the second attribute value
     * @return a {@code Map} containing the specified attributes
     */
    public static <A, B> Map<SecurityAttribute<?>, Object> mapOf(SecurityAttribute<A> attribute1,
            A value1, SecurityAttribute<B> attribute2, B value2) {
        return Map.of(attribute1, value1, attribute2, value2);
    }

    /**
     * Type-safely creates a {@code Map} of {@code SecurityAttribute}s.
     *
     * @param <A>
     *            the first {@code SecurityAttribute} type
     * @param <B>
     *            the second {@code SecurityAttribute} type
     * @param <C>
     *            the third {@code SecurityAttribute} type
     * @param attribute1
     *            the first {@code SecurityAttribute}
     * @param value1
     *            the first attribute value
     * @param attribute2
     *            the second {@code SecurityAttribute}
     * @param value2
     *            the second attribute value
     * @param attribute3
     *            the third {@code SecurityAttribute}
     * @param value3
     *            the third attribute value
     * @return a {@code Map} containing the specified attributes
     */
    public static <A, B, C> Map<SecurityAttribute<?>, Object> mapOf(SecurityAttribute<A> attribute1,
            A value1, SecurityAttribute<B> attribute2, B value2, SecurityAttribute<C> attribute3,
            C value3) {
        return Map.of(attribute1, value1, attribute2, value2, attribute3, value3);
    }

    /**
     * Type-safely creates a {@code Map} of {@code SecurityAttribute}s.
     *
     * @param <A>
     *            the first {@code SecurityAttribute} type
     * @param <B>
     *            the second {@code SecurityAttribute} type
     * @param <C>
     *            the third {@code SecurityAttribute} type
     * @param <D>
     *            the fourth {@code SecurityAttribute} type
     * @param attribute1
     *            the first {@code SecurityAttribute}
     * @param value1
     *            the first attribute value
     * @param attribute2
     *            the second {@code SecurityAttribute}
     * @param value2
     *            the second attribute value
     * @param attribute3
     *            the third {@code SecurityAttribute}
     * @param value3
     *            the third attribute value
     * @param attribute4
     *            the fourth {@code SecurityAttribute}
     * @param value4
     *            the fourth attribute value
     * @return a {@code Map} containing the specified attributes
     */
    public static <A, B, C, D> Map<SecurityAttribute<?>, Object> mapOf(
            SecurityAttribute<A> attribute1, A value1, SecurityAttribute<B> attribute2, B value2,
            SecurityAttribute<C> attribute3, C value3, SecurityAttribute<D> attribute4, D value4) {
        return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4,
                value4);
    }

    /**
     * Type-safely creates a {@code Map} of {@code SecurityAttribute}s.
     *
     * @param <A>
     *            the first {@code SecurityAttribute} type
     * @param <B>
     *            the second {@code SecurityAttribute} type
     * @param <C>
     *            the third {@code SecurityAttribute} type
     * @param <D>
     *            the fourth {@code SecurityAttribute} type
     * @param <E>
     *            the fifth {@code SecurityAttribute} type
     * @param attribute1
     *            the first {@code SecurityAttribute}
     * @param value1
     *            the first attribute value
     * @param attribute2
     *            the second {@code SecurityAttribute}
     * @param value2
     *            the second attribute value
     * @param attribute3
     *            the third {@code SecurityAttribute}
     * @param value3
     *            the third attribute value
     * @param attribute4
     *            the fourth {@code SecurityAttribute}
     * @param value4
     *            the fourth attribute value
     * @param attribute5
     *            the fifth {@code SecurityAttribute}
     * @param value5
     *            the fifth attribute value
     * @return a {@code Map} containing the specified attributes
     */
    public static <A, B, C, D, E> Map<SecurityAttribute<?>, Object> mapOf(
            SecurityAttribute<A> attribute1, A value1, SecurityAttribute<B> attribute2, B value2,
            SecurityAttribute<C> attribute3, C value3, SecurityAttribute<D> attribute4, D value4,
            SecurityAttribute<E> attribute5, E value5) {
        return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4,
                value4, attribute5, value5);
    }

    /**
     * Type-safely creates a {@code Map} of {@code SecurityAttribute}s.
     *
     * @param <A>
     *            the first {@code SecurityAttribute} type
     * @param <B>
     *            the second {@code SecurityAttribute} type
     * @param <C>
     *            the third {@code SecurityAttribute} type
     * @param <D>
     *            the fourth {@code SecurityAttribute} type
     * @param <E>
     *            the fifth {@code SecurityAttribute} type
     * @param <F>
     *            the sixth {@code SecurityAttribute} type
     * @param attribute1
     *            the first {@code SecurityAttribute}
     * @param value1
     *            the first attribute value
     * @param attribute2
     *            the second {@code SecurityAttribute}
     * @param value2
     *            the second attribute value
     * @param attribute3
     *            the third {@code SecurityAttribute}
     * @param value3
     *            the third attribute value
     * @param attribute4
     *            the fourth {@code SecurityAttribute}
     * @param value4
     *            the fourth attribute value
     * @param attribute5
     *            the fifth {@code SecurityAttribute}
     * @param value5
     *            the fifth attribute value
     * @param attribute6
     *            the sixth {@code SecurityAttribute}
     * @param value6
     *            the sixth attribute value
     * @return a {@code Map} containing the specified attributes
     */
    public static <A, B, C, D, E, F> Map<SecurityAttribute<?>, Object> mapOf(
            SecurityAttribute<A> attribute1, A value1, SecurityAttribute<B> attribute2, B value2,
            SecurityAttribute<C> attribute3, C value3, SecurityAttribute<D> attribute4, D value4,
            SecurityAttribute<E> attribute5, E value5, SecurityAttribute<F> attribute6, F value6) {
        return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4,
                value4, attribute5, value5, attribute6, value6);
    }

    /**
     * Type-safely creates a {@code Map} of {@code SecurityAttribute}s.
     *
     * @param <A>
     *            the first {@code SecurityAttribute} type
     * @param <B>
     *            the second {@code SecurityAttribute} type
     * @param <C>
     *            the third {@code SecurityAttribute} type
     * @param <D>
     *            the fourth {@code SecurityAttribute} type
     * @param <E>
     *            the fifth {@code SecurityAttribute} type
     * @param <F>
     *            the sixth {@code SecurityAttribute} type
     * @param <G>
     *            the seventh {@code SecurityAttribute} type
     * @param attribute1
     *            the first {@code SecurityAttribute}
     * @param value1
     *            the first attribute value
     * @param attribute2
     *            the second {@code SecurityAttribute}
     * @param value2
     *            the second attribute value
     * @param attribute3
     *            the third {@code SecurityAttribute}
     * @param value3
     *            the third attribute value
     * @param attribute4
     *            the fourth {@code SecurityAttribute}
     * @param value4
     *            the fourth attribute value
     * @param attribute5
     *            the fifth {@code SecurityAttribute}
     * @param value5
     *            the fifth attribute value
     * @param attribute6
     *            the sixth {@code SecurityAttribute}
     * @param value6
     *            the sixth attribute value
     * @param attribute7
     *            the seventh {@code SecurityAttribute}
     * @param value7
     *            the seventh attribute value
     * @return a {@code Map} containing the specified attributes
     */
    public static <A, B, C, D, E, F, G> Map<SecurityAttribute<?>, Object> mapOf(
            SecurityAttribute<A> attribute1, A value1, SecurityAttribute<B> attribute2, B value2,
            SecurityAttribute<C> attribute3, C value3, SecurityAttribute<D> attribute4, D value4,
            SecurityAttribute<E> attribute5, E value5, SecurityAttribute<F> attribute6, F value6,
            SecurityAttribute<G> attribute7, G value7) {
        return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4,
                value4, attribute5, value5, attribute6, value6, attribute7, value7);
    }

    /**
     * Type-safely creates a {@code Map} of {@code SecurityAttribute}s.
     *
     * @param <A>
     *            the first {@code SecurityAttribute} type
     * @param <B>
     *            the second {@code SecurityAttribute} type
     * @param <C>
     *            the third {@code SecurityAttribute} type
     * @param <D>
     *            the fourth {@code SecurityAttribute} type
     * @param <E>
     *            the fifth {@code SecurityAttribute} type
     * @param <F>
     *            the sixth {@code SecurityAttribute} type
     * @param <G>
     *            the seventh {@code SecurityAttribute} type
     * @param <H>
     *            the eighth {@code SecurityAttribute} type
     * @param attribute1
     *            the first {@code SecurityAttribute}
     * @param value1
     *            the first attribute value
     * @param attribute2
     *            the second {@code SecurityAttribute}
     * @param value2
     *            the second attribute value
     * @param attribute3
     *            the third {@code SecurityAttribute}
     * @param value3
     *            the third attribute value
     * @param attribute4
     *            the fourth {@code SecurityAttribute}
     * @param value4
     *            the fourth attribute value
     * @param attribute5
     *            the fifth {@code SecurityAttribute}
     * @param value5
     *            the fifth attribute value
     * @param attribute6
     *            the sixth {@code SecurityAttribute}
     * @param value6
     *            the sixth attribute value
     * @param attribute7
     *            the seventh {@code SecurityAttribute}
     * @param value7
     *            the seventh attribute value
     * @param attribute8
     *            the eighth {@code SecurityAttribute}
     * @param value8
     *            the eighth attribute value
     * @return a {@code Map} containing the specified attributes
     */
    public static <A, B, C, D, E, F, G, H> Map<SecurityAttribute<?>, Object> mapOf(
            SecurityAttribute<A> attribute1, A value1, SecurityAttribute<B> attribute2, B value2,
            SecurityAttribute<C> attribute3, C value3, SecurityAttribute<D> attribute4, D value4,
            SecurityAttribute<E> attribute5, E value5, SecurityAttribute<F> attribute6, F value6,
            SecurityAttribute<G> attribute7, G value7, SecurityAttribute<H> attribute8, H value8) {
        return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4,
                value4, attribute5, value5, attribute6, value6, attribute7, value7, attribute8,
                value8);
    }

    /**
     * Type-safely creates a {@code Map} of {@code SecurityAttribute}s.
     *
     * @param <A>
     *            the first {@code SecurityAttribute} type
     * @param <B>
     *            the second {@code SecurityAttribute} type
     * @param <C>
     *            the third {@code SecurityAttribute} type
     * @param <D>
     *            the fourth {@code SecurityAttribute} type
     * @param <E>
     *            the fifth {@code SecurityAttribute} type
     * @param <F>
     *            the sixth {@code SecurityAttribute} type
     * @param <G>
     *            the seventh {@code SecurityAttribute} type
     * @param <H>
     *            the eighth {@code SecurityAttribute} type
     * @param <I>
     *            the ninth {@code SecurityAttribute} type
     * @param attribute1
     *            the first {@code SecurityAttribute}
     * @param value1
     *            the first attribute value
     * @param attribute2
     *            the second {@code SecurityAttribute}
     * @param value2
     *            the second attribute value
     * @param attribute3
     *            the third {@code SecurityAttribute}
     * @param value3
     *            the third attribute value
     * @param attribute4
     *            the fourth {@code SecurityAttribute}
     * @param value4
     *            the fourth attribute value
     * @param attribute5
     *            the fifth {@code SecurityAttribute}
     * @param value5
     *            the fifth attribute value
     * @param attribute6
     *            the sixth {@code SecurityAttribute}
     * @param value6
     *            the sixth attribute value
     * @param attribute7
     *            the seventh {@code SecurityAttribute}
     * @param value7
     *            the seventh attribute value
     * @param attribute8
     *            the eighth {@code SecurityAttribute}
     * @param value8
     *            the eighth attribute value
     * @param attribute9
     *            the ninth {@code SecurityAttribute}
     * @param value9
     *            the ninth attribute value
     * @return a {@code Map} containing the specified attributes
     */
    public static <A, B, C, D, E, F, G, H, I> Map<SecurityAttribute<?>, Object> mapOf(
            SecurityAttribute<A> attribute1, A value1, SecurityAttribute<B> attribute2, B value2,
            SecurityAttribute<C> attribute3, C value3, SecurityAttribute<D> attribute4, D value4,
            SecurityAttribute<E> attribute5, E value5, SecurityAttribute<F> attribute6, F value6,
            SecurityAttribute<G> attribute7, G value7, SecurityAttribute<H> attribute8, H value8,
            SecurityAttribute<I> attribute9, I value9) {
        return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4,
                value4, attribute5, value5, attribute6, value6, attribute7, value7, attribute8,
                value8, attribute9, value9);
    }

    /**
     * Type-safely creates a {@code Map} of {@code SecurityAttribute}s.
     *
     * @param <A>
     *            the first {@code SecurityAttribute} type
     * @param <B>
     *            the second {@code SecurityAttribute} type
     * @param <C>
     *            the third {@code SecurityAttribute} type
     * @param <D>
     *            the fourth {@code SecurityAttribute} type
     * @param <E>
     *            the fifth {@code SecurityAttribute} type
     * @param <F>
     *            the sixth {@code SecurityAttribute} type
     * @param <G>
     *            the seventh {@code SecurityAttribute} type
     * @param <H>
     *            the eighth {@code SecurityAttribute} type
     * @param <I>
     *            the ninth {@code SecurityAttribute} type
     * @param <J>
     *            the tenth {@code SecurityAttribute} type
     * @param attribute1
     *            the first {@code SecurityAttribute}
     * @param value1
     *            the first attribute value
     * @param attribute2
     *            the second {@code SecurityAttribute}
     * @param value2
     *            the second attribute value
     * @param attribute3
     *            the third {@code SecurityAttribute}
     * @param value3
     *            the third attribute value
     * @param attribute4
     *            the fourth {@code SecurityAttribute}
     * @param value4
     *            the fourth attribute value
     * @param attribute5
     *            the fifth {@code SecurityAttribute}
     * @param value5
     *            the fifth attribute value
     * @param attribute6
     *            the sixth {@code SecurityAttribute}
     * @param value6
     *            the sixth attribute value
     * @param attribute7
     *            the seventh {@code SecurityAttribute}
     * @param value7
     *            the seventh attribute value
     * @param attribute8
     *            the eighth {@code SecurityAttribute}
     * @param value8
     *            the eighth attribute value
     * @param attribute9
     *            the ninth {@code SecurityAttribute}
     * @param value9
     *            the ninth attribute value
     * @param attribute10
     *            the tenth {@code SecurityAttribute}
     * @param value10
     *            the tenth attribute value
     * @return a {@code Map} containing the specified attributes
     */
    public static <A, B, C, D, E, F, G, H, I, J> Map<SecurityAttribute<?>, Object> mapOf(
            SecurityAttribute<A> attribute1, A value1, SecurityAttribute<B> attribute2, B value2,
            SecurityAttribute<C> attribute3, C value3, SecurityAttribute<D> attribute4, D value4,
            SecurityAttribute<E> attribute5, E value5, SecurityAttribute<F> attribute6, F value6,
            SecurityAttribute<G> attribute7, G value7, SecurityAttribute<H> attribute8, H value8,
            SecurityAttribute<I> attribute9, I value9, SecurityAttribute<J> attribute10,
            J value10) {
        return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4,
                value4, attribute5, value5, attribute6, value6, attribute7, value7, attribute8,
                value8, attribute9, value9, attribute10, value10);
    }

    /**
     * Obtains the {@code SecurityAttribute} corresponding to the given index, if it exists. For
     * most purposes, the name should be used, as indices may change over time.
     *
     * @param index
     *            the index of the {@code SecurityAttribute} to obtain
     * @return the {@code SecurityAttribute} corresponding to the given index, or {@code null} if it
     *         does not exist
     */
    public static SecurityAttribute<?> of(int index) {
        return attributesByIndex.get(index);
    }

    /**
     * Obtains the {@code SecurityAttribute} corresponding to the given name, if it exists.
     *
     * @param name
     *            the name of the {@code SecurityAttribute} to obtain
     * @return the {@code SecurityAttribute} corresponding to the given name, or {@code null} if it
     *         does not exist
     */
    @JsonCreator
    public static SecurityAttribute<?> of(String name) {
        return attributesByName.get(name);
    }

    /**
     * Obtains (or creates) a {@code SecurityAttribute} with the given name and value type.
     *
     * @param <T>
     *            the value type of the {@code SecurityAttribute}
     * @param name
     *            the unique name of the {@code SecurityAttribute}
     * @param index
     *            the index of the {@code SecurityAttribute} in an attributes array
     * @param type
     *            the {@code Class} of the value type
     * @param valueProvider
     *            a {@code ValueProvider} capable of interpreting {@code SecurityAttribute} values,
     *            or {@code null} if not applicable
     * @return an existing {@code SecurityAttribute} if already defined, otherwise a new
     *         {@code SecurityAttribute}
     */
    public static <T> SecurityAttribute<T> of(String name, int index, Class<T> type,
            ValueProvider<T> valueProvider) {
        @SuppressWarnings("unchecked")
        SecurityAttribute<T> attribute =
                (SecurityAttribute<T>)attributesByName.computeIfAbsent(name,
                        n -> new SecurityAttribute<>(name, index, type, valueProvider));
        attributesByIndex.put(index, attribute);

        return attribute;
    }

    private final String name;
    private final int index;
    private final Class<T> type;

    private final ValueProvider<T> valueProvider;

    /**
     * Creates a new {@code SecurityAttribute} with the given name and index. Restricted to enforce
     * use of the {@code of()} factory method.
     *
     * @param name
     *            the unique name of the {@code SecurityAttribute}
     * @param index
     *            the index of the {@code SecurityAttribute} in an attributes array
     * @param type
     *            the {@code Class} of the value type
     * @param valueProvider
     *            a {@code ValueProvider} capable of interpreting {@code SecurityAttribute} values,
     *            or {@code null} if not applicable
     */
    private SecurityAttribute(String name, int index, Class<T> type,
            ValueProvider<T> valueProvider) {
        this.name = name;
        this.index = index;
        this.type = type;
        this.valueProvider = valueProvider;
    }

    @Override
    public int compareTo(SecurityAttribute<?> o) {
        // sort by attribute name for diagnostic-friendly behavior (e.g. toString() output)
        return name.compareTo(o.name);
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
     * Obtains the index at which this {@code SecurityAttribute} may be found in an attributes
     * array.
     *
     * @return the array index for this {@code SecurityAttribute}
     */
    public int getIndex() {
        return index;
    }

    /**
     * Obtains the name of this {@code SecurityAttribute}.
     *
     * @return the attribute name
     */
    public String getName() {
        return name;
    }

    /**
     * Obtains the type implemented by values of this {@code SecurityAttribute}.
     *
     * @return the {@code SecurityAttribute} value type
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Obtains a {@code ValueProvider} capable of interpreting values of this
     * {@code SecurityAttribute} type.
     *
     * @return a {@code ValueProvider} or {@code null} if not applicable
     */
    public ValueProvider<T> getValueProvider() {
        return valueProvider;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @JsonValue
    public String toJsonString() {
        return name;
    }

    @Override
    public String toString() {
        return "SecurityAttribute[\"" + name + "\"]";
    }
}
