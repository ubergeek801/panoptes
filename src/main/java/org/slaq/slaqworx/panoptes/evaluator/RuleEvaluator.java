package org.slaq.slaqworx.panoptes.evaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleResult;

/**
 * {@code RuleEvaluator} is a {@code Callable} that evaluates a single {@code Rule} against a set of
 * {@code Position}s (and optionally a set of benchmark {@code Position}s). The results are grouped
 * by the {@code EvaluationGroupClassifier} defined by the {@code Rule}; there is always at least
 * one group (unless the input set of {@code Position}s is empty).
 *
 * @author jeremy
 */
public class RuleEvaluator implements Callable<EvaluationResult> {
    private static final Logger LOG = LoggerFactory.getLogger(RuleEvaluator.class);

    private final Rule rule;
    private final PositionSupplier portfolioPositions;
    private final PositionSupplier benchmarkPositions;
    private final EvaluationContext evaluationContext;

    /**
     * Creates a new {@code RuleEvaluator} to evaluate the given {@code Rule} against the given
     * {@code Position}s.
     *
     * @param rule
     *            the {@code Rule} to be evaluated
     * @param portfolioPositions
     *            the {@code Position}s against which to evaluate the {@code Rule}
     * @param benchmarkPositions
     *            the (possibly {@code null} benchmark {@code Position}s against which to evaluate
     * @param evaluationContext
     *            the context in which the {@code Rule} is to be evaluated
     */
    public RuleEvaluator(Rule rule, PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
        this.rule = rule;
        this.portfolioPositions = portfolioPositions;
        this.benchmarkPositions = benchmarkPositions;
        this.evaluationContext = evaluationContext;
    }

    @Override
    public EvaluationResult call() {
        LOG.debug("evaluating rule {} (\"{}\") on {} positions for portfolio {}", rule.getKey(),
                rule.getDescription(), portfolioPositions.size(),
                portfolioPositions.getPortfolio());

        // group the Positions of the Portfolio into classifications according to the Rule's
        // GroupClassifier
        Map<EvaluationGroup<?>, Collection<Position>> classifiedPortfolioPositions =
                portfolioPositions.getPositions()
                        .collect(Collectors.groupingBy(p -> rule.getGroupClassifier().classify(p),
                                Collectors.toCollection(ArrayList::new)));

        // do the same for the benchmark, if specified
        Map<EvaluationGroup<?>, Collection<Position>> classifiedBenchmarkPositions;
        if (benchmarkPositions == null) {
            classifiedBenchmarkPositions = null;
        } else {
            classifiedBenchmarkPositions = benchmarkPositions.getPositions()
                    .collect(Collectors.groupingBy(p -> rule.getGroupClassifier().classify(p),
                            Collectors.toCollection(ArrayList::new)));
        }

        // Execute the Rule's GroupAggregators (if any) to create additional EvaluationGroups. For
        // example, a Rule may aggregate the Positions holding the top five issuers in the Portfolio
        // into a new group.
        rule.getGroupAggregators().forEach(a -> {
            classifiedPortfolioPositions.putAll(a.aggregate(classifiedPortfolioPositions));
            if (classifiedBenchmarkPositions != null) {
                classifiedBenchmarkPositions.putAll(a.aggregate(classifiedBenchmarkPositions));
            }
        });

        // for each group of Positions, evaluate the Rule against the group, for both the Portfolio
        // and the Benchmark (if specified)
        Map<EvaluationGroup<?>, RuleResult> ruleResults = classifiedPortfolioPositions.entrySet()
                .stream().collect(Collectors.toMap(e -> e.getKey(), e -> {
                    EvaluationGroup<?> group = e.getKey();
                    Collection<Position> ppos = e.getValue();
                    // create PositionSets for the grouped Positions, being careful to relate to the
                    // original Portfolios, as some Rules will require them
                    PositionSet bpos;
                    if (benchmarkPositions == null || classifiedBenchmarkPositions == null) {
                        // no benchmark is provided
                        bpos = null;
                    } else {
                        Collection<Position> bposSet = classifiedBenchmarkPositions.get(group);
                        if (bposSet == null) {
                            // a benchmark was provided, but has no Positions in the group
                            bpos = null;
                        } else {
                            bpos = new PositionSet(classifiedBenchmarkPositions.get(group),
                                    benchmarkPositions.getPortfolio());
                        }
                    }

                    RuleResult singleResult =
                            rule.evaluate(new PositionSet(ppos, portfolioPositions.getPortfolio()),
                                    bpos, evaluationContext);

                    // wasn't that easy?
                    return singleResult;
                }));

        return new EvaluationResult(rule.getKey(), ruleResults);
    }
}
