package org.slaq.slaqworx.panoptes.rule;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * SecurityAttributeGroupClassifier is an EvaluationGroupClassifier which classifies Positions based
 * on the value of a specified SecurityAttribute.
 *
 * @author jeremy
 */
public class SecurityAttributeGroupClassifier
        implements EvaluationGroupClassifier, JsonConfigurable {
    static class Configuration {
        public String attribute;
    }

    /**
     * Creates a new SecurityAttributeGroupClassifier which classifies Positions based on the
     * SecurityAttribute specified in the JSON configuration.
     *
     * @param jsonConfiguration
     *            a JSON configuration specifying the SecurityAttribute on which to classify
     *            Positions
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
     * Creates a new SecurityAttributeGroupClassifier which classifies Positions based on the
     * specified SecurityAttribute.
     *
     * @param securityAttribute
     *            the SecurityAttribute on which to classify Positions
     */
    public SecurityAttributeGroupClassifier(SecurityAttribute<?> securityAttribute) {
        this.securityAttribute = securityAttribute;
    }

    @Override
    public EvaluationGroup<SecurityAttribute<?>> classify(SecurityProvider securityProvider,
            Position position) {
        return new EvaluationGroup<>(String.valueOf(
                position.getSecurity(securityProvider).getAttributeValue(securityAttribute)),
                securityAttribute);
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
     * Obtains the SecurityAttribute on which this classifier aggregates.
     *
     * @return the SecurityAttribute
     */
    public SecurityAttribute<?> getSecurityAttribute() {
        return securityAttribute;
    }
}
