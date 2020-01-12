package org.slaq.slaqworx.panoptes.rule;

import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * {@code EligibilityListRule} is an {@EligibilityRule} that determines eligibility of a
 * {@Security} based on whether certain of its attributes appears in a specified blacklist or
 * whitelist.
 *
 * @author jeremy
 */
public class EligibilityListRule extends EligibilityRule implements ConfigurableRule {
    /**
     * {@code Configuration} encapsulates the properties of a {@code EligibilityListRule} which are
     * configurable via e.g. JSON.
     */
    static class Configuration {
        public EligibilityListType listType;
        public String securityAttribute;
        public Set<String> eligibilityList;
    }

    enum EligibilityListType {
        BLACKLIST, WHITELIST
    }

    /**
     * Creates a new {@code EligibilityListRule} with the given JSON configuration, key,
     * description, filter and classifier.
     *
     * @param jsonConfiguration
     *            the JSON configuration specifying lower and upper limits
     * @param key
     *            the unique key of this {@code Rule}, or {@code null} to generate one
     * @param description
     *            the {@code Rule} description
     * @param groovyFilter
     *            a (possibly {@code null}) Groovy expression to be used as a {@code Position}
     *            filter
     * @param groupClassifier
     *            the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may
     *            also implement {@code GroupAggregator}
     */
    public static EligibilityListRule fromJson(String jsonConfiguration, RuleKey key,
            String description, String groovyFilter, EvaluationGroupClassifier groupClassifier) {
        Configuration configuration;
        try {
            configuration = JsonConfigurable.defaultObjectMapper().readValue(jsonConfiguration,
                    Configuration.class);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not parse JSON configuration " + jsonConfiguration,
                    e);
        }

        SecurityAttribute<?> securityAttribute =
                SecurityAttribute.of(configuration.securityAttribute);
        // FIXME handle nonexistent attribute
        if (!(String.class.isAssignableFrom(securityAttribute.getClass()))) {
            // FIXME handle non-String attribute
        }
        @SuppressWarnings("unchecked")
        SecurityAttribute<String> eligibilityAttribute =
                (SecurityAttribute<String>)SecurityAttribute.of(configuration.securityAttribute);

        return new EligibilityListRule(key, description, configuration.listType,
                eligibilityAttribute, configuration.eligibilityList);
    }

    private final EligibilityListType eligibilityListType;
    private final SecurityAttribute<?> eligibilityAttribute;
    private final Set<String> eligibilityList;

    /**
     * Creates a new {@code EligibilityListRule} with the given parameters.
     *
     * @param key
     *            the unique key of this rule, or {@code null} to generate one
     * @param description
     *            the rule description
     * @param eligibilityListType
     *            the type of list which specifies {@code Security} eligibility
     * @param securityAttribute
     *            the {@code Security} attribute upon which eligibility is based
     * @param eligibilityList
     *            a list of {@code Security} attribute values which is interpreted according to the
     *            list type
     */
    public EligibilityListRule(RuleKey key, String description,
            EligibilityListType eligibilityListType, SecurityAttribute<String> securityAttribute,
            Set<String> eligibilityList) {
        super(key, description);

        this.eligibilityListType = eligibilityListType;
        eligibilityAttribute = securityAttribute;
        this.eligibilityList = eligibilityList;
    }

    @Override
    public String getJsonConfiguration() {
        // FIXME implement

        try {
            return JsonConfigurable.defaultObjectMapper().writeValueAsString(null);
        } catch (JsonProcessingException e) {
            // TODO throw a better exception
            throw new RuntimeException("could not serialize JSON configuration", e);
        }
    }

    @Override
    public String getParameterDescription() {
        return eligibilityListType + " of " + eligibilityAttribute + ": ["
                + String.join(",", eligibilityList) + "]";
    }

    @Override
    public boolean isEligible(Security security) {
        if (EligibilityListType.BLACKLIST.equals(eligibilityListType)) {
            // membership in a BLACKLIST indicates ineligibility
            return !eligibilityList.contains(security.getAttributeValue(eligibilityAttribute));
        }

        // membership in a WHITELIST indicates eligibility
        return eligibilityList.contains(security.getAttributeValue(eligibilityAttribute));
    }
}
