package org.slaq.slaqworx.panoptes.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
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
     * @return a Map associating each evaluated Rule with its (grouped) result
     */
    public Map<Rule, Map<EvaluationGroup, Boolean>> evaluate(Stream<Rule> rules,
            PositionSupplier portfolioPositions, PositionSupplier benchmarkPositions) {
        // multiple levels of mapping going on: the first level iterates (in parallel) over Rules
        // and evaluates each one
        Map<Rule, Map<EvaluationGroup, Boolean>> allResults =
                rules.parallel().collect(Collectors.toMap(r -> r, rule -> {
                    LOG.info("evaluating rule {} (\"{}\") on {} positions for portfolio {}",
                            rule.getId(), rule.getDescription(), portfolioPositions.size(),
                            portfolioPositions);

                    // group the Positions of the portfolio into classifications according to the
                    // Rule's GroupClassifier
                    Map<EvaluationGroup, Collection<Position>> classifiedPortfolioPositions =
                            portfolioPositions.getPositions()
                                    .collect(Collectors.groupingBy(
                                            p -> rule.getGroupClassifier().classify(p),
                                            Collectors.toCollection(ArrayList::new)));

                    // do the same for the benchmark, if specified
                    Map<EvaluationGroup, Collection<Position>> classifiedBenchmarkPositions;
                    if (benchmarkPositions == null) {
                        classifiedBenchmarkPositions = null;
                    } else {
                        classifiedBenchmarkPositions = benchmarkPositions.getPositions()
                                .collect(Collectors.groupingBy(
                                        p -> rule.getGroupClassifier().classify(p),
                                        Collectors.toCollection(ArrayList::new)));
                    }

                    // Execute the Rule's GroupAggregators (if any) to create additional
                    // EvaluationGroups. For example, a Rule may include the Positions holding the
                    // top five issuers in the Portfolio.
                    rule.getGroupAggregators().forEach(a -> {
                        classifiedPortfolioPositions
                                .putAll(a.aggregate(classifiedPortfolioPositions));
                        if (classifiedBenchmarkPositions != null) {
                            classifiedBenchmarkPositions
                                    .putAll(a.aggregate(classifiedBenchmarkPositions));
                        }
                    });

                    // for each group of Positions, evaluate the Rule against the group, for both
                    // the Portfolio and the Benchmark (if specified)
                    Map<EvaluationGroup, Boolean> ruleResults = classifiedPortfolioPositions
                            .entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> {
                                EvaluationGroup group = e.getKey();
                                Collection<Position> ppos = e.getValue();
                                // create PositionSets for the grouped Positions, being careful to
                                // relate to the original Portfolios as some Rules will require it
                                PositionSet bpos;
                                if (classifiedBenchmarkPositions == null) {
                                    // no benchmark is provided
                                    bpos = null;
                                } else {
                                    Collection<Position> bposSet =
                                            classifiedBenchmarkPositions.get(group);
                                    if (bposSet == null) {
                                        // a benchmark was provided, but has no Positions in the
                                        // group
                                        bpos = null;
                                    } else {
                                        bpos = new PositionSet(
                                                classifiedBenchmarkPositions.get(group),
                                                benchmarkPositions.getPortfolio());
                                    }
                                }

                                boolean singleResult = rule.evaluate(
                                        new PositionSet(ppos, portfolioPositions.getPortfolio()),
                                        bpos);

                                // wasn't that easy?
                                return singleResult;
                            }));

                    return ruleResults;
                }));

        return allResults;
    }
}
