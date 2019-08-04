package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * MaterializedRule is a framework for implementation of the Rule interface. A MaterializedPosition
 * may be durable (e.g. sourced from a database/cache) or ephemeral (e.g. supplied by a simulation
 * mechanism or even a unit test).
 *
 * @author jeremy
 */
public abstract class MaterializedRule extends AbstractRule implements JsonConfigurable {
    /**
     * Creates a new MaterializedRule with the given key and description.
     *
     * @param key
     *            the unique key to assign to the Rule, or null to generate one
     * @param description
     *            the description of the Rule
     */
    protected MaterializedRule(RuleKey key, String description) {
        super(key, description, null);
    }

    /**
     * Creates a new MaterializedRule with the given key, description and evaluation group
     * classifier.
     *
     * @param key
     *            the unique key to assign to the Rule, or null to generate one
     * @param description
     *            the description of the Rule
     * @param groupClassifier
     *            the (possibly null) EvaluationGroupClassifier to use, which may also implement
     *            GroupAggregator
     */
    protected MaterializedRule(RuleKey key, String description,
            EvaluationGroupClassifier groupClassifier) {
        super(key, description, groupClassifier);
    }

    /**
     * Creates a new MaterializedRule with a generated key and the given description.
     *
     * @param description
     *            the description of the Rule
     */
    protected MaterializedRule(String description) {
        super(description);
    }

    /**
     * Obtains this Rule's Position filter, if any, as a Groovy expression. The filter would have
     * been specified at create time through a JSON configuration.
     *
     * @return the Position filter as a Groovy expression, or null if no filter is specified
     */
    public String getGroovyFilter() {
        return null;
    }
}
