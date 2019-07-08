package org.slaq.slaqworx.panoptes.asset;

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
public class SecurityAttribute<T> {
	private static final Map<String, SecurityAttribute<?>> attributes = new HashMap<>();

	public static SecurityAttribute<String> isin = SecurityAttribute.of("isin", String.class);
	public static SecurityAttribute<String> description =
			SecurityAttribute.of("description", String.class);
	public static SecurityAttribute<String> country = SecurityAttribute.of("country", String.class);
	public static SecurityAttribute<String> region = SecurityAttribute.of("region", String.class);
	public static SecurityAttribute<String> sector = SecurityAttribute.of("sector", String.class);
	public static SecurityAttribute<String> currency =
			SecurityAttribute.of("currency", String.class);
	public static SecurityAttribute<BigDecimal> coupon =
			SecurityAttribute.of("coupon", BigDecimal.class);
	public static SecurityAttribute<LocalDate> maturityDate =
			SecurityAttribute.of("maturityDate", LocalDate.class);
	public static SecurityAttribute<String> ratingSymbol =
			SecurityAttribute.of("ratingSymbol", String.class);
	public static SecurityAttribute<Double> ratingValue =
			SecurityAttribute.of("ratingValue", Double.class);
	public static SecurityAttribute<BigDecimal> yield =
			SecurityAttribute.of("yield", BigDecimal.class);
	public static SecurityAttribute<Double> duration =
			SecurityAttribute.of("duration", Double.class);

	private final String name;

	private SecurityAttribute(String name) {
		this.name = name;
	}

	public static <T> SecurityAttribute<T> of(String name, Class<T> clazz) {
		@SuppressWarnings("unchecked")
		SecurityAttribute<T> attribute = (SecurityAttribute<T>)attributes.computeIfAbsent(name,
				n -> new SecurityAttribute<Object>(name));
		return attribute;
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
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
}
