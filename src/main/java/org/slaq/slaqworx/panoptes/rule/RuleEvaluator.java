package org.slaq.slaqworx.panoptes.rule;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * RuleEvaluator is responsible for the process of evaluating a set of Rules against some Portfolio.
 *
 * @author jeremy
 */
public class RuleEvaluator {
    /**
     * Creates a new RuleEvaluator.
     */
    public RuleEvaluator() {
        // nothing to do
    }

    /**
     * Evaluates the given Portfolio using its associated Rules and benchmark (if any).
     *
     * @param portfolio the Portfolio to be evaluated
     * @return a Map associating each evaluated Rule with its result
     */
    public Map<Rule, Boolean> evaluate(Portfolio portfolio) {
        return evaluate(portfolio.getRules(), portfolio, portfolio.getBenchmark());
    }

    /**
     * Evaluates the given Portfolio using its associated Rules but overriding its associated
     * benchmark (if any) with the specified benchmark.
     *
     * @param portfolio the Portfolio to be evaluated
     * @param benchmark the (possibly null) benchmark to use in place of the Portfolio's associated
     *                  benchmark
     * @return a Map associating each evaluated Rule with its result
     */
    public Map<Rule, Boolean> evaluate(Portfolio portfolio, Portfolio benchmark) {
        return evaluate(portfolio.getRules(), portfolio, benchmark);
    }

    /**
     * Evaluates the given Portfolio against the given Rules (instead of the Portfolio's associated
     * Rules), using the Portfolio's associated benchmark (if any).
     *
     * @param rules     the Rules to evaluate against the given Portfolio
     * @param portfolio the Portfolio to be evaluated
     * @return a Map associating each evaluated Rule with its result
     */
    public Map<Rule, Boolean> evaluate(Stream<Rule> rules, Portfolio portfolio) {
        return evaluate(rules, portfolio, portfolio.getBenchmark());
    }

    /**
     * Evaluates the given Portfolio against the given Rules (instead of the Portfolio's associated
     * Rules), and overriding the Portfolio's associated benchmark (if any) with the specified
     * benchmark.
     *
     * @param rules     the Rules to evaluate against the given Portfolio
     * @param portfolio the Portfolio to be evaluated
     * @param benchmark the (possibly null) benchmark to use in place of the Portfolio's associated
     *                  benchmark
     * @return a Map associating each evaluated Rule with its result
     */
    public Map<Rule, Boolean> evaluate(Stream<Rule> rules, Portfolio portfolio,
            Portfolio benchmark) {
        return rules.parallel()
                .collect(Collectors.toMap(r -> r, r -> r.evaluate(portfolio, benchmark)));
    }
}
