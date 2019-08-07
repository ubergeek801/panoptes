package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.MaterializedRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * <code>RuleSerializer</code> (de)serializes the state of a <code>Rule</code> (actually a
 * <code>MaterializedRule</code> using Protobuf.
 *
 * @author jeremy
 */
public class RuleSerializer implements ByteArraySerializer<MaterializedRule> {
    public static MaterializedRule constructRule(String id, String description, String ruleTypeName,
            String configuration, String groovyFilter, String classifierTypeName,
            String classifierConfiguration) {
        Class<EvaluationGroupClassifier> classifierType =
                resolveClass(classifierTypeName, "classifier", id, description);
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
                throw new RuntimeException("could not instantiate classifier class " + ruleTypeName
                        + " for rule " + id + "(" + description + ")", e);
            }
        }

        Class<MaterializedRule> ruleType = resolveClass(ruleTypeName, "rule", id, description);
        MaterializedRule rule;
        try {
            Method fromJsonMethod = ruleType.getMethod("fromJson", String.class, RuleKey.class,
                    String.class, String.class, EvaluationGroupClassifier.class);
            rule = (MaterializedRule)fromJsonMethod.invoke(null, configuration, new RuleKey(id),
                    description, groovyFilter, classifier);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not instantiate rule class " + ruleTypeName
                    + " for rule " + id + "(" + description + ")", e);
        }

        return rule;
    }

    /**
     * Resolves the Class with the given name.
     *
     * @param <T>
     *            the expected type of the returned Class
     * @param className
     *            the name of the Class to resolve
     * @param function
     *            the function that the class serves (for logging purposes)
     * @param ruleId
     *            the rule ID for which the Class is being instantiated (for logging purposes)
     * @param ruleDescription
     *            the rule description for which the Class is being instantiated (for logging
     *            purposes)
     * @return the requested Class, or null if the given class name was null
     */
    protected static <T> Class<T> resolveClass(String className, String function, String ruleId,
            String ruleDescription) {
        if (className == null) {
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

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.RULE.ordinal();
    }

    @Override
    public MaterializedRule read(byte[] buffer) throws IOException {
        RuleMsg ruleMsg = RuleMsg.parseFrom(buffer);

        return constructRule(ruleMsg.getKey().getId(), ruleMsg.getDescription(), ruleMsg.getType(),
                ruleMsg.getConfiguration(), ruleMsg.getFilter(), ruleMsg.getClassifierType(),
                ruleMsg.getClassifierConfiguration());
    }

    @Override
    public byte[] write(MaterializedRule rule) throws IOException {
        IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
        keyBuilder.setId(rule.getKey().getId());
        IdKeyMsg key = keyBuilder.build();

        // TODO this code is similar to that in RuleMapStore; try to consolidate
        RuleMsg.Builder ruleBuilder = RuleMsg.newBuilder();
        ruleBuilder.setKey(key);
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

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ruleBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
