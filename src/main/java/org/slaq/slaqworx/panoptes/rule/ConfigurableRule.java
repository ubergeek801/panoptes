package org.slaq.slaqworx.panoptes.rule;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * A {@code ConfigurableRule} is a {@code Rule} that can be configured, typically via
 * deserialization from a persistent representation using JSON configuration parameters and/or
 * Groovy filter expressions.
 *
 * @author jeremy
 */
public interface ConfigurableRule extends Rule, JsonConfigurable {
    /**
     * Constructs a {@code ConfigurableRule} from the given parameters.
     *
     * @param id
     *            the ID of the {@code Rule} to be created
     * @param description
     *            the description of the {@code Rule} to be created
     * @param ruleClassName
     *            the name of the Java class implementing the {@code Rule}
     * @param configuration
     *            additional {@code Rule} JSON configuration, or {@code null} if not applicable
     * @param groovyFilter
     *            the {@code Rule} filter expression as Groovy, or {@code null} if not applicable
     * @param classifierClassName
     *            the name of the Java class implementing the classifer, or {@code null} if not
     *            applicable
     * @param classifierConfiguration
     *            additional classifier JSON configuration, or {@code null} if not applicable
     * @return a {@code ConfigurableRule} constructed according to the given parameters
     */
    public static ConfigurableRule constructRule(String id, String description,
            String ruleClassName, String configuration, String groovyFilter,
            String classifierClassName, String classifierConfiguration) {
        Class<EvaluationGroupClassifier> classifierType =
                resolveClass(classifierClassName, "classifier", id, description);
        EvaluationGroupClassifier classifier;
        if (classifierType == null) {
            classifier = null;
        } else {
            try {
                if (JsonConfigurable.class.isAssignableFrom(classifierType)) {
                    // attempt to configure from JSON
                    Method fromJsonMethod = classifierType.getMethod("fromJson", String.class);
                    classifier = (EvaluationGroupClassifier)fromJsonMethod.invoke(null,
                            classifierConfiguration);
                } else {
                    // there had better be a default constructor
                    classifier = classifierType.getConstructor().newInstance();
                }
            } catch (Exception e) {
                // TODO throw a better exception
                throw new RuntimeException("could not instantiate classifier class " + ruleClassName
                        + " for rule " + id + "(" + description + ")", e);
            }
        }

        Class<ConfigurableRule> ruleType = resolveClass(ruleClassName, "rule", id, description);
        ConfigurableRule rule;
        try {
            Method fromJsonMethod = ruleType.getMethod("fromJson", String.class, RuleKey.class,
                    String.class, String.class, EvaluationGroupClassifier.class);
            rule = (ConfigurableRule)fromJsonMethod.invoke(null,
                    StringUtils.trimToNull(configuration), new RuleKey(id), description,
                    StringUtils.trimToNull(groovyFilter), classifier);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not instantiate rule class " + ruleClassName
                    + " for rule " + id + "(" + description + ")", e);
        }

        return rule;
    }

    /**
     * Resolves the {@code Class} with the given name.
     *
     * @param <T>
     *            the expected type of the returned {@code Class}
     * @param className
     *            the name of the {@code Class} to resolve
     * @param function
     *            the function that the class serves (for logging purposes)
     * @param ruleId
     *            the rule ID for which the {@code Class} is being instantiated (for logging
     *            purposes)
     * @param ruleDescription
     *            the rule description for which the {@code Class} is being instantiated (for
     *            logging purposes)
     * @return the requested {@code Class}, or {@code null} if the given class name was {@code null}
     */
    public static <T> Class<T> resolveClass(String className, String function, String ruleId,
            String ruleDescription) {
        if (StringUtils.isEmpty(className)) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>)Class.forName(className);

            return clazz;
        } catch (ClassNotFoundException e) {
            // TODO throw a better exception
            throw new RuntimeException("could not find " + function + " class " + className
                    + " for rule " + ruleId + "(" + ruleDescription + ")");
        }
    }

    /**
     * Obtains this {@code Rule}'s {@code Position} filter, if any, as a Groovy expression. The
     * filter would have been specified at create time through a JSON configuration.
     *
     * @return the {@code Position} filter as a Groovy expression, or {@code null} if no filter is
     *         specified
     */
    default public String getGroovyFilter() {
        return null;
    }
}
