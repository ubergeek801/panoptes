package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import org.slaq.slaqworx.panoptes.rule.BenchmarkComparable;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * A service which compares intermediate results from a base portfolio and a benchmark portfolio to
 * produce a final result.
 *
 * @author jeremy
 */
public class BenchmarkComparator {
    /**
     * Creates a new {@code BenchmarkComparator}.
     */
    public BenchmarkComparator() {
        // nothing to do
    }

    public EvaluationResult compare(EvaluationResult baseResult, EvaluationResult benchmarkResult,
            BenchmarkComparable rule) {
        if (benchmarkResult == null) {
            // presume no benchmark used; just use the base result
            return baseResult;
        }

        if (!rule.isBenchmarkSupported()) {
            // no benchmark comparison possible; just use the base result
            return baseResult;
        }

        Double lowerLimit = rule.getLowerLimit();
        Double upperLimit = rule.getUpperLimit();

        Map<EvaluationGroup,
                ValueResult> baseResults = baseResult.getResults().entrySet().stream()
                        .map(e -> Pair.of(e.getKey(),
                                compare(e.getValue(), benchmarkResult.getResults().get(e.getKey()),
                                        lowerLimit, upperLimit)))
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        Map<EvaluationGroup, ValueResult> proposedResults;
        if (baseResult.getProposedResults() != null) {
            proposedResults = baseResult.getProposedResults().entrySet().stream()
                    .map(e -> Pair.of(e.getKey(),
                            compare(e.getValue(), benchmarkResult.getResults().get(e.getKey()),
                                    lowerLimit, upperLimit)))
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        } else {
            proposedResults = null;
        }

        return new EvaluationResult(baseResult.getRuleKey(), baseResults, proposedResults);
    }

    protected ValueResult compare(ValueResult baseResult, ValueResult benchmarkResult,
            Double lowerLimit, Double upperLimit) {
        Double benchmarkValue = (benchmarkResult == null ? null : benchmarkResult.getValue());
        // rescale the value to the benchmark; this may result in NaN, which means that the
        // Position's portfolio concentration is infinitely greater than the benchmark
        double scaledValue =
                (benchmarkValue == null ? Double.NaN : baseResult.getValue() / benchmarkValue);

        if (lowerLimit != null && (!Double.isNaN(scaledValue) && scaledValue < lowerLimit)) {
            return new ValueResult(Threshold.BELOW, scaledValue, benchmarkValue);
        }

        if (upperLimit != null && (Double.isNaN(scaledValue) || scaledValue > upperLimit)) {
            return new ValueResult(Threshold.ABOVE, scaledValue, benchmarkValue);
        }

        return new ValueResult(Threshold.WITHIN, scaledValue, benchmarkValue);
    }
}
