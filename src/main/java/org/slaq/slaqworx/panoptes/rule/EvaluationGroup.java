package org.slaq.slaqworx.panoptes.rule;

import java.io.Serializable;

/**
 * EvaluationGroup is a value type used as a key when classifying rule evaluation results.
 *
 * @author jeremy
 * @param <K>
 *            the aggregation key type, e.g. SecurityAttribute
 */
public class EvaluationGroup<K extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_EVALUATION_GROUP_ID = "portfolio";

    private static final EvaluationGroup<?> DEFAULT_GROUP =
            new EvaluationGroup<>(DEFAULT_EVALUATION_GROUP_ID, null);

    /**
     * Obtains the default (Portfolio-level) EvaluationGroup.
     *
     * @return the default EvaluationGroup
     */
    public static EvaluationGroup<?> defaultGroup() {
        return DEFAULT_GROUP;
    }

    private final String id;
    private final K aggregationKey;

    /**
     * Creates a new EvaluationGroup with the given ID and aggregation key.
     *
     * @param id
     *            the unique ID of the EvaluationGroup
     * @param aggregationKey
     *            the aggregation key used to define this EvaluationGroup, or null for the default
     */
    public EvaluationGroup(String id, K aggregationKey) {
        this.id = id;
        this.aggregationKey = aggregationKey;
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
        EvaluationGroup<?> other = (EvaluationGroup<?>)obj;
        return id.equals(other.id);
    }

    /**
     * Obtains the aggregation key on which this EvaluationGroup is classified, or null if this is
     * the default (unclassified) group.
     *
     * @return this EvalulationGroup's aggregation key
     */
    public K getAggregationKey() {
        return aggregationKey;
    }

    /**
     * Obtains the ID of this EvaluationGroup.
     *
     * @return the EvaluationGroup ID
     */
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
