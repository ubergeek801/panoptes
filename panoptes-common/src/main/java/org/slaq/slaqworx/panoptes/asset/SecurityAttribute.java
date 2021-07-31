package org.slaq.slaqworx.panoptes.asset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.rule.ValueProvider;

/**
 * Identifies a particular attribute of a {@link Security}. Implements {@link Serializable} for
 * convenience of implementing cluster-friendly {@link Security} filters.
 *
 * @param <T>
 *     the type which values of this attribute implement
 *
 * @author jeremy
 */
public class SecurityAttribute<T> implements Comparable<SecurityAttribute<?>>, Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

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

  @Nonnull
  private final String name;
  private final int index;
  @Nonnull
  private final Class<T> type;
  private final ValueProvider<T> valueProvider;

  /**
   * Creates a new {@link SecurityAttribute} with the given name and index. Restricted to enforce
   * use of the {@code #of()} factory methods.
   *
   * @param name
   *     the unique name of the {@link SecurityAttribute}
   * @param index
   *     the index of the {@link SecurityAttribute} in an attributes array
   * @param type
   *     the {@link Class} of the value type
   * @param valueProvider
   *     a {@link ValueProvider} capable of interpreting {@link SecurityAttribute} values, or {@code
   *     null} if not applicable
   */
  private SecurityAttribute(@Nonnull String name, int index, @Nonnull Class<T> type,
      ValueProvider<T> valueProvider) {
    this.name = name;
    this.index = index;
    this.type = type;
    this.valueProvider = valueProvider;
  }

  /**
   * Type-safely creates a {@link Map} of a single {@link SecurityAttribute}.
   *
   * @param <A>
   *     the {@link SecurityAttribute} type
   * @param attribute
   *     the {@link SecurityAttribute}
   * @param value
   *     the attribute value
   *
   * @return a {@link Map} containing the specified attribute
   */
  @Nonnull
  public static <A> Map<SecurityAttribute<?>, Object> mapOf(@Nonnull SecurityAttribute<A> attribute,
      @Nonnull A value) {
    return Map.of(attribute, value);
  }

  /**
   * Type-safely creates a {@link Map} of {@link SecurityAttribute}s.
   *
   * @param <A>
   *     the first {@link SecurityAttribute} type
   * @param <B>
   *     the second {@link SecurityAttribute} type
   * @param attribute1
   *     the first {@link SecurityAttribute}
   * @param value1
   *     the first attribute value
   * @param attribute2
   *     the second {@link SecurityAttribute}
   * @param value2
   *     the second attribute value
   *
   * @return a {@link Map} containing the specified attributes
   */
  @Nonnull
  public static <A, B> Map<SecurityAttribute<?>, Object> mapOf(
      @Nonnull SecurityAttribute<A> attribute1, @Nonnull A value1,
      @Nonnull SecurityAttribute<B> attribute2, @Nonnull B value2) {
    return Map.of(attribute1, value1, attribute2, value2);
  }

  /**
   * Type-safely creates a {@link Map} of {@link SecurityAttribute}s.
   *
   * @param <A>
   *     the first {@link SecurityAttribute} type
   * @param <B>
   *     the second {@link SecurityAttribute} type
   * @param <C>
   *     the third {@link SecurityAttribute} type
   * @param attribute1
   *     the first {@link SecurityAttribute}
   * @param value1
   *     the first attribute value
   * @param attribute2
   *     the second {@link SecurityAttribute}
   * @param value2
   *     the second attribute value
   * @param attribute3
   *     the third {@link SecurityAttribute}
   * @param value3
   *     the third attribute value
   *
   * @return a {@link Map} containing the specified attributes
   */
  @Nonnull
  public static <A, B, C> Map<SecurityAttribute<?>, Object> mapOf(
      @Nonnull SecurityAttribute<A> attribute1, @Nonnull A value1,
      @Nonnull SecurityAttribute<B> attribute2, @Nonnull B value2,
      @Nonnull SecurityAttribute<C> attribute3, @Nonnull C value3) {
    return Map.of(attribute1, value1, attribute2, value2, attribute3, value3);
  }

  /**
   * Type-safely creates a {@link Map} of {@link SecurityAttribute}s.
   *
   * @param <A>
   *     the first {@link SecurityAttribute} type
   * @param <B>
   *     the second {@link SecurityAttribute} type
   * @param <C>
   *     the third {@link SecurityAttribute} type
   * @param <D>
   *     the fourth {@link SecurityAttribute} type
   * @param attribute1
   *     the first {@link SecurityAttribute}
   * @param value1
   *     the first attribute value
   * @param attribute2
   *     the second {@link SecurityAttribute}
   * @param value2
   *     the second attribute value
   * @param attribute3
   *     the third {@link SecurityAttribute}
   * @param value3
   *     the third attribute value
   * @param attribute4
   *     the fourth {@link SecurityAttribute}
   * @param value4
   *     the fourth attribute value
   *
   * @return a {@link Map} containing the specified attributes
   */
  @Nonnull
  public static <A, B, C, D> Map<SecurityAttribute<?>, Object> mapOf(
      @Nonnull SecurityAttribute<A> attribute1, @Nonnull A value1,
      @Nonnull SecurityAttribute<B> attribute2, @Nonnull B value2,
      @Nonnull SecurityAttribute<C> attribute3, @Nonnull C value3,
      @Nonnull SecurityAttribute<D> attribute4, @Nonnull D value4) {
    return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4, value4);
  }

  /**
   * Type-safely creates a {@link Map} of {@link SecurityAttribute}s.
   *
   * @param <A>
   *     the first {@link SecurityAttribute} type
   * @param <B>
   *     the second {@link SecurityAttribute} type
   * @param <C>
   *     the third {@link SecurityAttribute} type
   * @param <D>
   *     the fourth {@link SecurityAttribute} type
   * @param <E>
   *     the fifth {@link SecurityAttribute} type
   * @param attribute1
   *     the first {@link SecurityAttribute}
   * @param value1
   *     the first attribute value
   * @param attribute2
   *     the second {@link SecurityAttribute}
   * @param value2
   *     the second attribute value
   * @param attribute3
   *     the third {@link SecurityAttribute}
   * @param value3
   *     the third attribute value
   * @param attribute4
   *     the fourth {@link SecurityAttribute}
   * @param value4
   *     the fourth attribute value
   * @param attribute5
   *     the fifth {@link SecurityAttribute}
   * @param value5
   *     the fifth attribute value
   *
   * @return a {@link Map} containing the specified attributes
   */
  @Nonnull
  public static <A, B, C, D, E> Map<SecurityAttribute<?>, Object> mapOf(
      @Nonnull SecurityAttribute<A> attribute1, @Nonnull A value1,
      @Nonnull SecurityAttribute<B> attribute2, @Nonnull B value2,
      @Nonnull SecurityAttribute<C> attribute3, @Nonnull C value3,
      @Nonnull SecurityAttribute<D> attribute4, @Nonnull D value4,
      @Nonnull SecurityAttribute<E> attribute5, @Nonnull E value5) {
    return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4, value4,
        attribute5, value5);
  }

  /**
   * Type-safely creates a {@link Map} of {@link SecurityAttribute}s.
   *
   * @param <A>
   *     the first {@link SecurityAttribute} type
   * @param <B>
   *     the second {@link SecurityAttribute} type
   * @param <C>
   *     the third {@link SecurityAttribute} type
   * @param <D>
   *     the fourth {@link SecurityAttribute} type
   * @param <E>
   *     the fifth {@link SecurityAttribute} type
   * @param <F>
   *     the sixth {@link SecurityAttribute} type
   * @param attribute1
   *     the first {@link SecurityAttribute}
   * @param value1
   *     the first attribute value
   * @param attribute2
   *     the second {@link SecurityAttribute}
   * @param value2
   *     the second attribute value
   * @param attribute3
   *     the third {@link SecurityAttribute}
   * @param value3
   *     the third attribute value
   * @param attribute4
   *     the fourth {@link SecurityAttribute}
   * @param value4
   *     the fourth attribute value
   * @param attribute5
   *     the fifth {@link SecurityAttribute}
   * @param value5
   *     the fifth attribute value
   * @param attribute6
   *     the sixth {@link SecurityAttribute}
   * @param value6
   *     the sixth attribute value
   *
   * @return a {@link Map} containing the specified attributes
   */
  @Nonnull
  public static <A, B, C, D, E, F> Map<SecurityAttribute<?>, Object> mapOf(
      @Nonnull SecurityAttribute<A> attribute1, @Nonnull A value1,
      @Nonnull SecurityAttribute<B> attribute2, @Nonnull B value2,
      @Nonnull SecurityAttribute<C> attribute3, @Nonnull C value3,
      @Nonnull SecurityAttribute<D> attribute4, @Nonnull D value4,
      @Nonnull SecurityAttribute<E> attribute5, @Nonnull E value5,
      @Nonnull SecurityAttribute<F> attribute6, @Nonnull F value6) {
    return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4, value4,
        attribute5, value5, attribute6, value6);
  }

  /**
   * Type-safely creates a {@link Map} of {@link SecurityAttribute}s.
   *
   * @param <A>
   *     the first {@link SecurityAttribute} type
   * @param <B>
   *     the second {@link SecurityAttribute} type
   * @param <C>
   *     the third {@link SecurityAttribute} type
   * @param <D>
   *     the fourth {@link SecurityAttribute} type
   * @param <E>
   *     the fifth {@link SecurityAttribute} type
   * @param <F>
   *     the sixth {@link SecurityAttribute} type
   * @param <G>
   *     the seventh {@link SecurityAttribute} type
   * @param attribute1
   *     the first {@link SecurityAttribute}
   * @param value1
   *     the first attribute value
   * @param attribute2
   *     the second {@link SecurityAttribute}
   * @param value2
   *     the second attribute value
   * @param attribute3
   *     the third {@link SecurityAttribute}
   * @param value3
   *     the third attribute value
   * @param attribute4
   *     the fourth {@link SecurityAttribute}
   * @param value4
   *     the fourth attribute value
   * @param attribute5
   *     the fifth {@link SecurityAttribute}
   * @param value5
   *     the fifth attribute value
   * @param attribute6
   *     the sixth {@link SecurityAttribute}
   * @param value6
   *     the sixth attribute value
   * @param attribute7
   *     the seventh {@link SecurityAttribute}
   * @param value7
   *     the seventh attribute value
   *
   * @return a {@link Map} containing the specified attributes
   */
  @Nonnull
  public static <A, B, C, D, E, F, G> Map<SecurityAttribute<?>, Object> mapOf(
      @Nonnull SecurityAttribute<A> attribute1, @Nonnull A value1,
      @Nonnull SecurityAttribute<B> attribute2, @Nonnull B value2,
      @Nonnull SecurityAttribute<C> attribute3, @Nonnull C value3,
      @Nonnull SecurityAttribute<D> attribute4, @Nonnull D value4,
      @Nonnull SecurityAttribute<E> attribute5, @Nonnull E value5,
      @Nonnull SecurityAttribute<F> attribute6, @Nonnull F value6,
      @Nonnull SecurityAttribute<G> attribute7, @Nonnull G value7) {
    return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4, value4,
        attribute5, value5, attribute6, value6, attribute7, value7);
  }

  /**
   * Type-safely creates a {@link Map} of {@link SecurityAttribute}s.
   *
   * @param <A>
   *     the first {@link SecurityAttribute} type
   * @param <B>
   *     the second {@link SecurityAttribute} type
   * @param <C>
   *     the third {@link SecurityAttribute} type
   * @param <D>
   *     the fourth {@link SecurityAttribute} type
   * @param <E>
   *     the fifth {@link SecurityAttribute} type
   * @param <F>
   *     the sixth {@link SecurityAttribute} type
   * @param <G>
   *     the seventh {@link SecurityAttribute} type
   * @param <H>
   *     the eighth {@link SecurityAttribute} type
   * @param attribute1
   *     the first {@link SecurityAttribute}
   * @param value1
   *     the first attribute value
   * @param attribute2
   *     the second {@link SecurityAttribute}
   * @param value2
   *     the second attribute value
   * @param attribute3
   *     the third {@link SecurityAttribute}
   * @param value3
   *     the third attribute value
   * @param attribute4
   *     the fourth {@link SecurityAttribute}
   * @param value4
   *     the fourth attribute value
   * @param attribute5
   *     the fifth {@link SecurityAttribute}
   * @param value5
   *     the fifth attribute value
   * @param attribute6
   *     the sixth {@link SecurityAttribute}
   * @param value6
   *     the sixth attribute value
   * @param attribute7
   *     the seventh {@link SecurityAttribute}
   * @param value7
   *     the seventh attribute value
   * @param attribute8
   *     the eighth {@link SecurityAttribute}
   * @param value8
   *     the eighth attribute value
   *
   * @return a {@link Map} containing the specified attributes
   */
  @Nonnull
  public static <A, B, C, D, E, F, G, H> Map<SecurityAttribute<?>, Object> mapOf(
      @Nonnull SecurityAttribute<A> attribute1, @Nonnull A value1,
      @Nonnull SecurityAttribute<B> attribute2, @Nonnull B value2,
      @Nonnull SecurityAttribute<C> attribute3, @Nonnull C value3,
      @Nonnull SecurityAttribute<D> attribute4, @Nonnull D value4,
      @Nonnull SecurityAttribute<E> attribute5, @Nonnull E value5,
      @Nonnull SecurityAttribute<F> attribute6, @Nonnull F value6,
      @Nonnull SecurityAttribute<G> attribute7, @Nonnull G value7,
      @Nonnull SecurityAttribute<H> attribute8, @Nonnull H value8) {
    return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4, value4,
        attribute5, value5, attribute6, value6, attribute7, value7, attribute8, value8);
  }

  /**
   * Type-safely creates a {@link Map} of {@link SecurityAttribute}s.
   *
   * @param <A>
   *     the first {@link SecurityAttribute} type
   * @param <B>
   *     the second {@link SecurityAttribute} type
   * @param <C>
   *     the third {@link SecurityAttribute} type
   * @param <D>
   *     the fourth {@link SecurityAttribute} type
   * @param <E>
   *     the fifth {@link SecurityAttribute} type
   * @param <F>
   *     the sixth {@link SecurityAttribute} type
   * @param <G>
   *     the seventh {@link SecurityAttribute} type
   * @param <H>
   *     the eighth {@link SecurityAttribute} type
   * @param <I>
   *     the ninth {@link SecurityAttribute} type
   * @param attribute1
   *     the first {@link SecurityAttribute}
   * @param value1
   *     the first attribute value
   * @param attribute2
   *     the second {@link SecurityAttribute}
   * @param value2
   *     the second attribute value
   * @param attribute3
   *     the third {@link SecurityAttribute}
   * @param value3
   *     the third attribute value
   * @param attribute4
   *     the fourth {@link SecurityAttribute}
   * @param value4
   *     the fourth attribute value
   * @param attribute5
   *     the fifth {@link SecurityAttribute}
   * @param value5
   *     the fifth attribute value
   * @param attribute6
   *     the sixth {@link SecurityAttribute}
   * @param value6
   *     the sixth attribute value
   * @param attribute7
   *     the seventh {@link SecurityAttribute}
   * @param value7
   *     the seventh attribute value
   * @param attribute8
   *     the eighth {@link SecurityAttribute}
   * @param value8
   *     the eighth attribute value
   * @param attribute9
   *     the ninth {@link SecurityAttribute}
   * @param value9
   *     the ninth attribute value
   *
   * @return a {@link Map} containing the specified attributes
   */
  @Nonnull
  public static <A, B, C, D, E, F, G, H, I> Map<SecurityAttribute<?>, Object> mapOf(
      @Nonnull SecurityAttribute<A> attribute1, @Nonnull A value1,
      @Nonnull SecurityAttribute<B> attribute2, @Nonnull B value2,
      @Nonnull SecurityAttribute<C> attribute3, @Nonnull C value3,
      @Nonnull SecurityAttribute<D> attribute4, @Nonnull D value4,
      @Nonnull SecurityAttribute<E> attribute5, @Nonnull E value5,
      @Nonnull SecurityAttribute<F> attribute6, @Nonnull F value6,
      @Nonnull SecurityAttribute<G> attribute7, @Nonnull G value7,
      @Nonnull SecurityAttribute<H> attribute8, @Nonnull H value8,
      @Nonnull SecurityAttribute<I> attribute9, @Nonnull I value9) {
    return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4, value4,
        attribute5, value5, attribute6, value6, attribute7, value7, attribute8, value8, attribute9,
        value9);
  }

  /**
   * Type-safely creates a {@link Map} of {@link SecurityAttribute}s.
   *
   * @param <A>
   *     the first {@link SecurityAttribute} type
   * @param <B>
   *     the second {@link SecurityAttribute} type
   * @param <C>
   *     the third {@link SecurityAttribute} type
   * @param <D>
   *     the fourth {@link SecurityAttribute} type
   * @param <E>
   *     the fifth {@link SecurityAttribute} type
   * @param <F>
   *     the sixth {@link SecurityAttribute} type
   * @param <G>
   *     the seventh {@link SecurityAttribute} type
   * @param <H>
   *     the eighth {@link SecurityAttribute} type
   * @param <I>
   *     the ninth {@link SecurityAttribute} type
   * @param <J>
   *     the tenth {@link SecurityAttribute} type
   * @param attribute1
   *     the first {@link SecurityAttribute}
   * @param value1
   *     the first attribute value
   * @param attribute2
   *     the second {@link SecurityAttribute}
   * @param value2
   *     the second attribute value
   * @param attribute3
   *     the third {@link SecurityAttribute}
   * @param value3
   *     the third attribute value
   * @param attribute4
   *     the fourth {@link SecurityAttribute}
   * @param value4
   *     the fourth attribute value
   * @param attribute5
   *     the fifth {@link SecurityAttribute}
   * @param value5
   *     the fifth attribute value
   * @param attribute6
   *     the sixth {@link SecurityAttribute}
   * @param value6
   *     the sixth attribute value
   * @param attribute7
   *     the seventh {@link SecurityAttribute}
   * @param value7
   *     the seventh attribute value
   * @param attribute8
   *     the eighth {@link SecurityAttribute}
   * @param value8
   *     the eighth attribute value
   * @param attribute9
   *     the ninth {@link SecurityAttribute}
   * @param value9
   *     the ninth attribute value
   * @param attribute10
   *     the tenth {@link SecurityAttribute}
   * @param value10
   *     the tenth attribute value
   *
   * @return a {@link Map} containing the specified attributes
   */
  @Nonnull
  public static <A, B, C, D, E, F, G, H, I, J> Map<SecurityAttribute<?>, Object> mapOf(
      @Nonnull SecurityAttribute<A> attribute1, @Nonnull A value1,
      @Nonnull SecurityAttribute<B> attribute2, @Nonnull B value2,
      @Nonnull SecurityAttribute<C> attribute3, @Nonnull C value3,
      @Nonnull SecurityAttribute<D> attribute4, @Nonnull D value4,
      @Nonnull SecurityAttribute<E> attribute5, @Nonnull E value5,
      @Nonnull SecurityAttribute<F> attribute6, @Nonnull F value6,
      @Nonnull SecurityAttribute<G> attribute7, @Nonnull G value7,
      @Nonnull SecurityAttribute<H> attribute8, @Nonnull H value8,
      @Nonnull SecurityAttribute<I> attribute9, @Nonnull I value9,
      @Nonnull SecurityAttribute<J> attribute10, @Nonnull J value10) {
    return Map.of(attribute1, value1, attribute2, value2, attribute3, value3, attribute4, value4,
        attribute5, value5, attribute6, value6, attribute7, value7, attribute8, value8, attribute9,
        value9, attribute10, value10);
  }

  /**
   * Obtains the {@link SecurityAttribute} corresponding to the given index, if it exists. For most
   * purposes, the name should be used, as indices may change over time.
   *
   * @param index
   *     the index of the {@link SecurityAttribute} to obtain
   *
   * @return the {@link SecurityAttribute} corresponding to the given index, or {@code null} if it
   *     does not exist
   */
  public static SecurityAttribute<?> of(int index) {
    return attributesByIndex.get(index);
  }

  /**
   * Obtains the {@link SecurityAttribute} corresponding to the given name, if it exists.
   *
   * @param name
   *     the name of the {@link SecurityAttribute} to obtain
   *
   * @return the {@link SecurityAttribute} corresponding to the given name, or {@code null} if it
   *     does not exist
   */
  @JsonCreator
  public static SecurityAttribute<?> of(@Nonnull String name) {
    return attributesByName.get(name);
  }

  /**
   * Obtains (or creates) a {@link SecurityAttribute} with the given name and value type.
   *
   * @param <T>
   *     the value type of the {@link SecurityAttribute}
   * @param name
   *     the unique name of the {@link SecurityAttribute}
   * @param index
   *     the index of the {@link SecurityAttribute} in an attributes array
   * @param type
   *     the {@link Class} of the value type
   * @param valueProvider
   *     a {@link ValueProvider} capable of interpreting {@link SecurityAttribute} values, or {@code
   *     null} if not applicable
   *
   * @return an existing {@link SecurityAttribute} if already defined, otherwise a new {@link
   *     SecurityAttribute}
   */
  @Nonnull
  public static <T> SecurityAttribute<T> of(@Nonnull String name, int index, @Nonnull Class<T> type,
      ValueProvider<T> valueProvider) {
    @SuppressWarnings("unchecked") SecurityAttribute<T> attribute =
        (SecurityAttribute<T>) attributesByName.computeIfAbsent(name,
            n -> new SecurityAttribute<>(name, index, type, valueProvider));
    attributesByIndex.put(index, attribute);

    return attribute;
  }

  @Override
  public int compareTo(@Nonnull SecurityAttribute<?> o) {
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
    SecurityAttribute<?> other = (SecurityAttribute<?>) obj;

    return name.equals(other.name);
  }

  /**
   * Obtains the index at which this {@link SecurityAttribute} may be found in an attributes array.
   *
   * @return the array index for this {@link SecurityAttribute}
   */
  public int getIndex() {
    return index;
  }

  /**
   * Obtains the name of this {@link SecurityAttribute}.
   *
   * @return the attribute name
   */
  @Nonnull
  public String getName() {
    return name;
  }

  /**
   * Obtains the type implemented by values of this {@link SecurityAttribute}.
   *
   * @return the {@link SecurityAttribute} value type
   */
  @Nonnull
  public Class<T> getType() {
    return type;
  }

  /**
   * Obtains a {@link ValueProvider} capable of interpreting values of this {@link
   * SecurityAttribute} type.
   *
   * @return a {@link ValueProvider} or {@code null} if not applicable
   */
  public ValueProvider<T> getValueProvider() {
    return valueProvider;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @JsonValue
  @Nonnull
  public String toJsonString() {
    return name;
  }

  @Override
  @Nonnull
  public String toString() {
    return "SecurityAttribute[\"" + name + "\"]";
  }
}
