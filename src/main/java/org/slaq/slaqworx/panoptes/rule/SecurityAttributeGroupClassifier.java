package org.slaq.slaqworx.panoptes.rule;

import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * {@code SecurityAttributeGroupClassifier} is an {@code EvaluationGroupClassifier} which classifies
 * {@code Position}s based on the value of a specified {@code SecurityAttribute}.
 *
 * @author jeremy
 */
public class SecurityAttributeGroupClassifier
        implements EvaluationGroupClassifier, JsonConfigurable {
    /**
     * {@code Configuration} mirrors the structure of the JSON configuration (which currently is
     * trivial).
     */
    static class Configuration {
        public String attribute;
    }

    /**
     * Creates a new {@code SecurityAttributeGroupClassifier} which classifies {@code Position}s
     * based on the {@code SecurityAttribute} specified in the JSON configuration.
     *
     * @param jsonConfiguration
     *            a JSON configuration specifying the {@code SecurityAttribute} on which to classify
     *            {@code Position}s
     */
    public static SecurityAttributeGroupClassifier fromJson(String jsonConfiguration) {
        Configuration configuration;
        try {
            configuration = JsonConfigurable.defaultObjectMapper().readValue(jsonConfiguration,
                    Configuration.class);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not parse JSON configuration " + jsonConfiguration,
                    e);
        }

        return new SecurityAttributeGroupClassifier(SecurityAttribute.of(configuration.attribute));
    }

    private final SecurityAttribute<?> securityAttribute;

    /**
     * Creates a new {@code SecurityAttributeGroupClassifier} which classifies {@code Position}s
     * based on the specified {@code SecurityAttribute}.
     *
     * @param securityAttribute
     *            the {@code SecurityAttribute} on which to classify {@code Position}s
     */
    public SecurityAttributeGroupClassifier(SecurityAttribute<?> securityAttribute) {
        this.securityAttribute = securityAttribute;
    }

    @Override
    public EvaluationGroup classify(Supplier<PositionEvaluationContext> positionContextSupplier) {
        PositionEvaluationContext positionContext = positionContextSupplier.get();

        Object attributeValue = positionContext.getPosition().getAttributeValue(securityAttribute,
                false, positionContext.getEvaluationContext());
        // TODO maybe implement special handling for null attribute values
        return new EvaluationGroup(String.valueOf(attributeValue), securityAttribute.getName());
    }

    @Override
    public String getJsonConfiguration() {
        Configuration configuration = new Configuration();
        configuration.attribute = getSecurityAttribute().getName();

        try {
            return JsonConfigurable.defaultObjectMapper().writeValueAsString(configuration);
        } catch (JsonProcessingException e) {
            // TODO throw a better exception
            throw new RuntimeException("could not serialize JSON configuration", e);
        }
    }

    /**
     * Obtains the {@code SecurityAttribute} on which this classifier aggregates.
     *
     * @return the {@code SecurityAttribute}
     */
    public SecurityAttribute<?> getSecurityAttribute() {
        return securityAttribute;
    }
}
