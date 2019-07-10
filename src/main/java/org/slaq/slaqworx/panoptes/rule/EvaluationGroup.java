package org.slaq.slaqworx.panoptes.rule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EvaluationGroup is a value type used as a key when classifying rule evaluation results.
 *
 * @author jeremy
 */
public class EvaluationGroup {
    private static final Map<String, EvaluationGroup> groups = new ConcurrentHashMap<>();

    public static final String DEFAULT_EVALUATION_GROUP_ID = "portfolio";

    /**
     * Obtains the default (Portfolio-level) EvaluationGroup.
     *
     * @return the default EvaluationGroup
     */
    public static EvaluationGroup defaultGroup() {
        return of(DEFAULT_EVALUATION_GROUP_ID);
    }

    /**
     * Obtains (or creates) an EvaluationGroup with the given ID.
     *
     * @param id
     *            the unique ID of the EvaluationGroup
     * @return an existing EvaluationGroup if already defined, otherwise a new EvaluationGroup
     */
    public static EvaluationGroup of(String id) {
        return groups.computeIfAbsent(id, n -> new EvaluationGroup(id));
    }

    private final String id;

    /**
     * Creates a new EvaluationGroup with the given ID. Restricted to enforce use of the of()
     * factory method.
     *
     * @param id
     *            the unique ID of the EvaluationGroup
     */
    private EvaluationGroup(String id) {
        this.id = id;
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
