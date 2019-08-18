package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * A {@code ConfigurableRule} is a {@code Rule} that can be configured, typically via
 * deserialization from a persistent representation using JSON configuration parameters and/or
 * Groovy filter expressions.
 *
 * @author jeremy
 */
public abstract class ConfigurableRule extends Rule implements JsonConfigurable {
    /**
     * Creates a new {@code ConfigurableRule} with the given key and description.
     *
     * @param key
     *            the unique key to assign to the {@code Rule}, or {@code null} to generate one
     * @param description
     *            the description of the {@code Rule}
     */
    protected ConfigurableRule(RuleKey key, String description) {
        super(key, description, null);
    }

    /**
     * Creates a new {@code ConfigurableRule} with the given key, description and evaluation group
     * classifier.
     *
     * @param key
     *            the unique key to assign to the {@code Rule}, or null to generate one
     * @param description
     *            the description of the {@code Rule}
     * @param groupClassifier
     *            the (possibly {@code null}) {@code EvaluationGroupClassifier} to use, which may
     *            also implement {@code GroupAggregator}
     */
    protected ConfigurableRule(RuleKey key, String description,
            EvaluationGroupClassifier groupClassifier) {
        super(key, description, groupClassifier);
    }

    /**
     * Creates a new {@code ConfigurableRule} with a generated key and the given description.
     *
     * @param description
     *            the description of the {@code Rule}
     */
    protected ConfigurableRule(String description) {
        super(description);
    }

    /**
     * Obtains this {@code Rule}'s {@code Position} filter, if any, as a Groovy expression. The
     * filter would have been specified at create time through a JSON configuration.
     *
     * @return the {@code Position} filter as a Groovy expression, or {@code null} if no filter is
     *         specified
     */
    public String getGroovyFilter() {
        return null;
    }
}
