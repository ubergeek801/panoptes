package org.slaq.slaqworx.panoptes.rule;

import java.util.concurrent.ConcurrentHashMap;

/**
 * EvaluationContext provides contextual information that can be shared across multiple evaluations.
 * Currently it is used experimentally to cache benchmark evaluation results between the "current"
 * and "proposed" phases of a trade evaluation.
 *
 * @author jeremy
 */
public class EvaluationContext {
    private final ConcurrentHashMap<Rule, Double> previousBenchmarkValues =
            new ConcurrentHashMap<>(1000);

    public EvaluationContext() {
        // nothing to do
    }

    public Double getPreviousBenchmarkValue(Rule rule) {
        return previousBenchmarkValues.get(rule);
    }

    public void setPreviousBenchmarkValue(Rule rule, double previousBenchmarkValue) {
        previousBenchmarkValues.put(rule, previousBenchmarkValue);
    }
}
