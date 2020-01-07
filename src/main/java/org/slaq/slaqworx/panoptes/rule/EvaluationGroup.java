package org.slaq.slaqworx.panoptes.rule;

/**
 * {@code EvaluationGroup} is a value type used as a key when classifying rule evaluation results.
 *
 * @author jeremy
 */
public class EvaluationGroup {
    public static final String DEFAULT_EVALUATION_GROUP_ID = "portfolio";

    private static final EvaluationGroup DEFAULT_GROUP =
            new EvaluationGroup(DEFAULT_EVALUATION_GROUP_ID, null);

    /**
     * Obtains the default ({@code Portfolio}-level) {@code EvaluationGroup}.
     *
     * @return the default {@code EvaluationGroup}
     */
    public static EvaluationGroup defaultGroup() {
        return DEFAULT_GROUP;
    }

    private final String id;
    private final String aggregationKey;

    /**
     * Creates a new {@code EvaluationGroup} with the given ID and aggregation key.
     *
     * @param id
     *            the unique ID of the {@code EvaluationGroup}
     * @param aggregationKey
     *            the aggregation key used to define this {@code valuationGroup}, or {@code null}
     *            for the default
     */
    public EvaluationGroup(String id, String aggregationKey) {
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
        EvaluationGroup other = (EvaluationGroup)obj;
        return id.equals(other.id);
    }

    /**
     * Obtains the aggregation key on which this {@code EvaluationGroup} is classified, or
     * {@code null} if this is the default (unclassified) group.
     *
     * @return this {@code EvalulationGroup}'s aggregation key
     */
    public String getAggregationKey() {
        return aggregationKey;
    }

    /**
     * Obtains the ID of this {@code EvaluationGroup}.
     *
     * @return the {@code EvaluationGroup} ID
     */
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
