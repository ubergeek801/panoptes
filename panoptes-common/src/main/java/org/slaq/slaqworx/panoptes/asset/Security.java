package org.slaq.slaqworx.panoptes.asset;

import java.util.Map;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.NoDataException;
import org.slaq.slaqworx.panoptes.cache.EligibilityResolver;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * An investable instrument. Unlike most other asset-related entities, a {@link Security} is
 * implicitly "versioned" by hashing its attributes: the resulting hash is used as an alternate key.
 * Thus when a {@link Security} changes (due to a change in some analytic field such as yield or
 * rating), the new version will use a different hash as the alternate key.
 *
 * <p>In order to support hypothetical scenarios, the value of any {@link SecurityAttribute} may be
 * overridden in an {@link EvaluationContext}. The attribute value with overrides considered is
 * known as the <i>effective</i> attribute value, and is obtained through the {@code
 * getEffectiveAttributeValue()} methods. For situations which don't require the effective value
 * (for example, to display a table of current {@link Security} data), the {@code getAttribute()}
 * methods may be used.
 *
 * @author jeremy
 */
public class Security implements Keyed<SecurityKey>, ProtobufSerializable {
  @Nonnull private final SecurityKey key;
  @Nonnull private final SecurityAttributes attributes;

  /**
   * Creates a new {@link Security} with the given {@link SecurityAttribute} values. The key is
   * taken from the attribute containing the ISIN; this is the only attribute that is required.
   *
   * @param attributes a {@link Map} of {@link SecurityAttribute} to attribute value
   */
  public Security(@Nonnull Map<SecurityAttribute<?>, ? super Object> attributes) {
    this.attributes = new SecurityAttributes(attributes);
    String assetId = (String) attributes.get(SecurityAttribute.isin);
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
    Security other = (Security) obj;

    return attributes.equals(other.attributes);
  }

  /**
   * Obtains the {@link Security}'s attributes.
   *
   * @return a {@link SecurityAttributes} comprising this {@link Security}'s attributes
   */
  @Nonnull
  public SecurityAttributes getAttributes() {
    return attributes;
  }

  /**
   * Obtains the base value of the specified attribute index. This form of {@code
   * getAttributeValue()} is intended for the rare cases when the index is already known.
   *
   * @param attributeIndex the index corresponding to the associated {@link SecurityAttribute}
   * @return the base value of the given attribute
   * @throws NoDataException if the requested attribute has no assigned value
   */
  @Nonnull
  public Object getAttributeValue(int attributeIndex) {
    return getAttributeValue(attributeIndex, true);
  }

  /**
   * Obtains the base value of the specified attribute index. This form of {@code
   * getAttributeValue()} is intended for the rare cases when the index is already known.
   *
   * @param attributeIndex the index corresponding to the associated {@link SecurityAttribute}
   * @param isRequired {@code true} if a return value is required, {@code false} otherwise
   * @return the base value of the given attribute, or {@code null} if not assigned and {@code
   *     isRequired} is {@code false}
   * @throws NoDataException if the attribute value is not assigned and {@code isRequired} is {@code
   *     true}
   */
  public Object getAttributeValue(int attributeIndex, boolean isRequired) {
    Object value = attributes.getValue(attributeIndex);
    if (value == null && isRequired) {
      throw new NoDataException(SecurityAttribute.of(attributeIndex).getName());
    }

    return value;
  }

  /**
   * Obtains the base value of the specified attribute.
   *
   * @param <T> the expected type of the attribute value
   * @param attribute the {@link SecurityAttribute} identifying the attribute
   * @return the value of the given attribute
   * @throws NoDataException if the requested attribute has no assigned value
   */
  @Nonnull
  public <T> T getAttributeValue(@Nonnull SecurityAttribute<T> attribute) {
    return getAttributeValue(attribute, true);
  }

  /**
   * Obtains the base value of the specified attribute.
   *
   * @param <T> the expected type of the attribute value
   * @param attribute the {@code SecurityAttribute} identifying the attribute
   * @param isRequired {@code true} if a return value is required, {@code false} otherwise
   * @return the value of the given attribute, or {@code null} if not assigned and {@code
   *     isRequired} is {@code false}
   * @throws NoDataException if the attribute value is not assigned and {@code isRequired} is {@code
   *     true}
   */
  public <T> T getAttributeValue(@Nonnull SecurityAttribute<T> attribute, boolean isRequired) {
    @SuppressWarnings("unchecked")
    T value = (T) getAttributeValue(attribute.getIndex(), isRequired);

    return value;
  }

  /**
   * Obtains the effective value of the specified attribute index. This form of {@code
   * getAttributeValue()} is intended for the rare cases when the index is already known.
   *
   * @param attributeIndex the index corresponding to the associated {@link SecurityAttribute}
   * @param isRequired {@code true} if a return value is required, {@code false} otherwise
   * @param evaluationContext the {@link EvaluationContext} in which the attribute value is being
   *     retrieved
   * @return the effective value of the given attribute, or {@code null} if not assigned and {@code
   *     isRequired} is {@code false}
   * @throws NoDataException if the attribute value is not assigned and {@code isRequired} is {@code
   *     true}
   */
  public Object getEffectiveAttributeValue(
      int attributeIndex, boolean isRequired, @Nonnull EvaluationContext evaluationContext) {
    SecurityAttributes overrideAttributes = evaluationContext.getSecurityOverrides().get(key);
    if (overrideAttributes != null) {
      Object overrideValue = overrideAttributes.getValue(attributeIndex);
      if (overrideValue != null) {
        return overrideValue;
      }
    }

    return getAttributeValue(attributeIndex, isRequired);
  }

  /**
   * Obtains the effective value of the specified attribute index. This form of {@code
   * getAttributeValue()} is intended for the rare cases when the index is already known.
   *
   * @param attributeIndex the index corresponding to the associated {@link SecurityAttribute}
   * @param evaluationContext the {@link EvaluationContext} in which the attribute value is being
   *     retrieved
   * @return the effective value of the given attribute
   * @throws NoDataException if the attribute value is not assigned
   */
  @Nonnull
  public Object getEffectiveAttributeValue(
      int attributeIndex, @Nonnull EvaluationContext evaluationContext) {
    return getEffectiveAttributeValue(attributeIndex, true, evaluationContext);
  }

  /**
   * Obtains the effective value of the specified attribute.
   *
   * @param <T> the expected type of the attribute value
   * @param attribute the {@link SecurityAttribute} identifying the attribute
   * @param isRequired {@code true} if a return value is required, {@code false} otherwise
   * @param evaluationContext the {@link EvaluationContext} in which the attribute value is being
   *     retrieved
   * @return the effective value of the given attribute, or {@code null} if not assigned and {@code
   *     isRequired} is {@code false}
   * @throws NoDataException if the attribute value is not assigned and {@code isRequired} is {@code
   *     true}
   */
  public <T> T getEffectiveAttributeValue(
      @Nonnull SecurityAttribute<T> attribute,
      boolean isRequired,
      @Nonnull EvaluationContext evaluationContext) {
    @SuppressWarnings("unchecked")
    T value = (T) getEffectiveAttributeValue(attribute.getIndex(), isRequired, evaluationContext);
    return value;
  }

  /**
   * Obtains the effective value of the specified attribute.
   *
   * @param <T> the expected type of the attribute value
   * @param attribute the {@link SecurityAttribute} identifying the attribute
   * @param evaluationContext the {@link EvaluationContext} in which the attribute value is being
   *     retrieved
   * @return the effective value of the given attribute
   * @throws NoDataException if the attribute value is not assigned
   */
  @Nonnull
  public <T> T getEffectiveAttributeValue(
      @Nonnull SecurityAttribute<T> attribute, @Nonnull EvaluationContext evaluationContext) {
    return getEffectiveAttributeValue(attribute, true, evaluationContext);
  }

  @Override
  @Nonnull
  public SecurityKey getKey() {
    return key;
  }

  @Override
  public int hashCode() {
    return attributes.hashCode();
  }

  /**
   * Indicates whether this {@link Security} is a member of the specified country list.
   *
   * @param listName the name of the list for which to determine membership
   * @param evaluationContext the {@link EvaluationContext} in which to make the determination
   * @return {@code true} if this {@link Security} is a member of the specified list, {@code false}
   *     otherwise
   */
  public boolean inCountryList(
      @Nonnull String listName, @Nonnull EvaluationContext evaluationContext) {
    return new EligibilityResolver(evaluationContext.getEligibilityListProvider())
        .isCountryListMember(this, listName);
  }

  /**
   * Indicates whether this {@link Security} is a member of the specified issuer list.
   *
   * @param listName the name of the list for which to determine membership
   * @param evaluationContext the {@link EvaluationContext} in which to make the determination
   * @return {@code true} if this {@link Security} is a member of the specified list, {@code false}
   *     otherwise
   */
  public boolean inIssuerList(
      @Nonnull String listName, @Nonnull EvaluationContext evaluationContext) {
    return new EligibilityResolver(evaluationContext.getEligibilityListProvider())
        .isIssuerListMember(this, listName);
  }

  /**
   * Indicates whether this {@link Security} is a member of the specified security list.
   *
   * @param listName the name of the list for which to determine membership
   * @param evaluationContext the {@link EvaluationContext} in which to make the determination
   * @return {@code true} if this {@link Security} is a member of the specified list, {@code false}
   *     otherwise
   */
  public boolean inSecurityList(
      @Nonnull String listName, @Nonnull EvaluationContext evaluationContext) {
    return new EligibilityResolver(evaluationContext.getEligibilityListProvider())
        .isSecurityListMember(this, listName);
  }

  @Override
  @Nonnull
  public String toString() {
    return "Security[" + key + "]";
  }
}
