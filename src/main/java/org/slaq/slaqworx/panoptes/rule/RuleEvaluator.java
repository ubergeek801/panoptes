package org.slaq.slaqworx.panoptes.rule;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;

/**
 * RuleEvaluator is responsible for the process of evaluating a set of Rules against some Portfolio
 * and possibly some related benchmark.
 *
 * @author jeremy
 */
public class RuleEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(RuleEvaluator.class);

    /**
     * Creates a new RuleEvaluator.
     */
    public RuleEvaluator() {
        // nothing to do
    }

    /**
     * Evaluates the given Portfolio using its associated Rules and benchmark (if any).
     *
     * @param portfolio
     *            the Portfolio to be evaluated
     * @return a Map associating each evaluated Rule with its result
     */
    public Map<Rule, Map<EvaluationGroup, Boolean>> evaluate(Portfolio portfolio) {
        return evaluate(portfolio.getRules(), portfolio, portfolio.getBenchmark());
    }

    /**
     * Evaluates the given Portfolio using its associated Rules but overriding its associated
     * benchmark (if any) with the specified benchmark.
     *
     * @param portfolio
     *            the Portfolio to be evaluated
     * @param benchmark
     *            the (possibly null) benchmark to use in place of the Portfolio's associated
     *            benchmark
     * @return a Map associating each evaluated Rule with its result
     */
    public Map<Rule, Map<EvaluationGroup, Boolean>> evaluate(Portfolio portfolio,
            Portfolio benchmark) {
        return evaluate(portfolio.getRules(), portfolio, benchmark);
    }

    /**
     * Evaluates the given Portfolio against the given Rules (instead of the Portfolio's associated
     * Rules), using the Portfolio's associated benchmark (if any).
     *
     * @param rules
     *            the Rules to evaluate against the given Portfolio
     * @param portfolio
     *            the Portfolio to be evaluated
     * @return a Map associating each evaluated Rule with its result
     */
    public Map<Rule, Map<EvaluationGroup, Boolean>> evaluate(Stream<Rule> rules,
            Portfolio portfolio) {
        return evaluate(rules, portfolio, portfolio.getBenchmark());
    }

    /**
     * Evaluates the given Positions against the given Rules and (optionally) benchmark Positions.
     * Each Rule's results are grouped by its specified EvaluationGroup.
     *
     * @param rules
     *            the Rules to evaluate against the given portfolio
     * @param portfolioPositions
     *            the portfolio Positions to be evaluated
     * @param benchmarkPositions
     *            the (possibly null) benchmark Positions to be evaluated against
     * @return a Map associating each evaluated Rule with its result
     */
    public Map<Rule, Map<EvaluationGroup, Boolean>> evaluate(Stream<Rule> rules,
            PositionSupplier portfolioPositions, PositionSupplier benchmarkPositions) {
        Map<Rule, Map<EvaluationGroup, Boolean>> allResults =
                rules.parallel().collect(Collectors.toMap(r -> r, rule -> {
                    LOG.info("evaluating rule {} (\"{}\") on {} positions for portfolio {}",
                            rule.getId(), rule.getDescription(), portfolioPositions.size(),
                            portfolioPositions);

                    Map<EvaluationGroup, Set<Position>> classifiedPortfolioPositions =
                            portfolioPositions.getPositions()
                                    .collect(Collectors.groupingBy(
                                            p -> rule.getGroupClassifier().classify(p),
                                            Collectors.toSet()));

                    Map<EvaluationGroup, Set<Position>> classifiedBenchmarkPositions;
                    if (benchmarkPositions == null) {
                        classifiedBenchmarkPositions = null;
                    } else {
                        classifiedBenchmarkPositions = benchmarkPositions.getPositions()
                                .collect(Collectors.groupingBy(
                                        p -> rule.getGroupClassifier().classify(p),
                                        Collectors.toSet()));
                    }

                    Map<EvaluationGroup, Boolean> ruleResults = classifiedPortfolioPositions
                            .entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> {
                                EvaluationGroup group = e.getKey();
                                Set<Position> ppos = e.getValue();
                                // create a PositionSet for the grouped Positions, being careful to
                                // relate
                                // to the original Portfolio as some Rules will require it
                                PositionSet bpos = (classifiedBenchmarkPositions == null ? null
                                        : new PositionSet(classifiedBenchmarkPositions.get(group),
                                                benchmarkPositions.getPortfolio()));
                                boolean result = rule.evaluate(
                                        new PositionSet(ppos, portfolioPositions.getPortfolio()),
                                        bpos);

                                return result;
                            }));

                    return ruleResults;
                }));

        return allResults;
    }
}
