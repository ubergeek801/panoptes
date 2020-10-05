package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleMsg;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * {@code RuleSerializer} (de)serializes the state of a {@code Rule} (actually a
 * {@code ConfigurableRule} using Protobuf.
 * <p>
 * Note that in order for deserialization to work, the {@code Rule} class must define the static
 * method:
 *
 * <pre>
 *
 * public static SampleRule fromJson(String jsonConfiguration, RuleKey key, String description,
 *         String groovyFilter, EvaluationGroupClassifier groupClassifier)
 * </pre>
 *
 * @author jeremy
 */
public class RuleSerializer implements ProtobufSerializer<ConfigurableRule> {
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
                    + " for rule " + id + " (" + description + ")", e);
        }

        return rule;
    }

    /**
     * Converts a {@code ConfigurableRule} into a new {@code RuleMsg}.
     *
     * @param rule
     *            the {@code ConfigurableRule} to be converted
     * @return a {@code RuleMsg}
     */
    public static RuleMsg convert(ConfigurableRule rule) {
        IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
        keyBuilder.setId(rule.getKey().getId());

        // TODO this code is similar to that in RuleMapStore; try to consolidate
        RuleMsg.Builder ruleBuilder = RuleMsg.newBuilder();
        ruleBuilder.setKey(keyBuilder);
        ruleBuilder.setDescription(rule.getDescription());
        ruleBuilder.setType(rule.getClass().getName());
        if (rule.getJsonConfiguration() != null) {
            ruleBuilder.setConfiguration(rule.getJsonConfiguration());
        }
        if (rule.getGroovyFilter() != null) {
            ruleBuilder.setFilter(rule.getGroovyFilter());
        }
        EvaluationGroupClassifier classifier = rule.getGroupClassifier();
        if (classifier != null) {
            ruleBuilder.setClassifierType(classifier.getClass().getName());
            if (classifier instanceof JsonConfigurable) {
                String jsonConfiguration = ((JsonConfigurable)classifier).getJsonConfiguration();
                if (jsonConfiguration != null) {
                    ruleBuilder.setClassifierConfiguration(jsonConfiguration);
                }
            }
        }

        return ruleBuilder.build();
    }

    /**
     * Converts a {@code RuleMsg} into a new {@code ConfigurableRule}.
     *
     * @param ruleMsg
     *            the {@code RuleMsg} to be converted
     * @return a {@code ConfigurableRule}
     */
    public static ConfigurableRule convert(RuleMsg ruleMsg) {
        return constructRule(ruleMsg.getKey().getId(), ruleMsg.getDescription(), ruleMsg.getType(),
                ruleMsg.getConfiguration(), ruleMsg.getFilter(), ruleMsg.getClassifierType(),
                ruleMsg.getClassifierConfiguration());
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
    protected static <T> Class<T> resolveClass(String className, String function, String ruleId,
            String ruleDescription) {
        if (StringUtils.isEmpty(className)) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>)Class.forName(className);

            return clazz;
        } catch (ClassNotFoundException e) {
            // TODO throw a better exception
            throw new RuntimeException("could not find " + function + " class " + className
                    + " for rule " + ruleId + "(" + ruleDescription + ")");
        }
    }

    /**
     * Creates a new {@code RuleSerializer}.
     */
    public RuleSerializer() {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.RULE.ordinal();
    }

    @Override
    public ConfigurableRule read(byte[] buffer) throws IOException {
        RuleMsg ruleMsg = RuleMsg.parseFrom(buffer);

        return convert(ruleMsg);
    }

    @Override
    public byte[] write(ConfigurableRule rule) throws IOException {
        RuleMsg ruleMsg = convert(rule);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ruleMsg.writeTo(out);
        return out.toByteArray();
    }
}
