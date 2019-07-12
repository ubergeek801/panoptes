package org.slaq.slaqworx.panoptes.rule;

import java.util.concurrent.ConcurrentHashMap;

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
