package org.slaq.slaqworx.panoptes.rule;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * An {@link EvaluationGroupClassifier} which classifies {@link Position}s based on the value of a
 * specified {@link SecurityAttribute}.
 *
 * @author jeremy
 */
public class SecurityAttributeGroupClassifier
    implements EvaluationGroupClassifier, JsonConfigurable {
  @Nonnull
  private final SecurityAttribute<?> securityAttribute;

  /**
   * Creates a new {@link SecurityAttributeGroupClassifier} which classifies {@link Position}s based
   * on the specified {@link SecurityAttribute}.
   *
   * @param securityAttribute
   *     the {@link SecurityAttribute} on which to classify {@link Position}s
   */
  public SecurityAttributeGroupClassifier(@Nonnull SecurityAttribute<?> securityAttribute) {
    this.securityAttribute = securityAttribute;
  }

  /**
   * Creates a new {@link SecurityAttributeGroupClassifier} which classifies {@link Position}s based
   * on the {@link SecurityAttribute} specified in the JSON configuration.
   *
   * @param jsonConfiguration
   *     a JSON configuration specifying the {@link SecurityAttribute} on which to classify {@link
   *     Position}s
   *
   * @return a {@link SecurityAttributeGroupClassifier} with the specified configuration
   */
  @Nonnull
  public static SecurityAttributeGroupClassifier fromJson(@Nonnull String jsonConfiguration) {
    Configuration configuration;
    try {
      configuration =
          JsonConfigurable.defaultObjectMapper().readValue(jsonConfiguration, Configuration.class);
    } catch (Exception e) {
      // TODO throw a better exception
      throw new RuntimeException("could not parse JSON configuration " + jsonConfiguration, e);
    }

    return new SecurityAttributeGroupClassifier(SecurityAttribute.of(configuration.attribute));
  }

  @Override
  @Nonnull
  public EvaluationGroup classify(
      @Nonnull Supplier<PositionEvaluationContext> positionContextSupplier) {
    PositionEvaluationContext positionContext = positionContextSupplier.get();

    Object attributeValue = positionContext.getPosition()
        .getAttributeValue(securityAttribute, false, positionContext.getEvaluationContext());
    // TODO maybe implement special handling for null attribute values
    return new EvaluationGroup(String.valueOf(attributeValue), securityAttribute.getName());
  }

  @Override
  @Nonnull
  public String getJsonConfiguration() {
    Configuration configuration = new Configuration(getSecurityAttribute().getName());

    try {
      return JsonConfigurable.defaultObjectMapper().writeValueAsString(configuration);
    } catch (JsonProcessingException e) {
      // TODO throw a better exception
      throw new RuntimeException("could not serialize JSON configuration", e);
    }
  }

  /**
   * Obtains the {@link SecurityAttribute} on which this classifier aggregates.
   *
   * @return the {@link SecurityAttribute}
   */
  @Nonnull
  public SecurityAttribute<?> getSecurityAttribute() {
    return securityAttribute;
  }

  /**
   * Mirrors the structure of the JSON configuration.
   */
  static record Configuration(@Nonnull String attribute) {
    // trivial
  }
}
