package org.slaq.slaqworx.panoptes.evaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.GroupAggregator;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleResult;

/**
 * {@code RuleEvaluator} is a {@code Callable} that evaluates a single {@code Rule} against a set of
 * {@code Position}s (and optionally a set of benchmark {@code Position}s). The results are grouped
 * by the {@code EvaluationGroupClassifier} defined by the {@code Rule}; there is always at least
 * one group (unless the input set of {@code Position}s is empty).
 * <p>
 * Given that a {@code RuleEvaluator} requires a {@code EvaluationContext} at construction time, the
 * evaluator can only be used for that context. {@code RuleEvaluator} creation is meant to be
 * inexpensive, however, so a new one can be created for each evaluation session.
 *
 * @author jeremy
 */
public class RuleEvaluator implements Callable<EvaluationResult> {
    /**
     * {@code PositionSupplierCollector} is a {@code Collector} that operates on a {@code Stream} of
     * {@code Position}s to collect into a new {@code PositionSupplier}, using a {@code Collection}
     * as an accumulator.
     *
     * @author jeremy
     */
    private class PositionSupplierCollector implements
            Collector<PositionEvaluationContext, Collection<Position>, PositionSupplier> {
        private final PortfolioKey portfolioKey;
        private final Double portfolioMarketValue;

        /**
         * Creates a new {@code PositionSupplierCollector}.
         *
         * @param portfolioKey
         *            the (possibly {@code null} key of the associated {@code Portfolio}
         * @param portfolioMarketValue
         *            the (possibly {@code null} portfolio market value to specify on the created
         *            {@code PositionSupplier}
         */
        public PositionSupplierCollector(PortfolioKey portfolioKey, Double portfolioMarketValue) {
            this.portfolioKey = portfolioKey;
            this.portfolioMarketValue = portfolioMarketValue;
        }

        @Override
        public BiConsumer<Collection<Position>, PositionEvaluationContext> accumulator() {
            // add the Position to the accumulator collection
            return (c, ctx) -> c.add(ctx.getPosition());
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.UNORDERED);
        }

        @Override
        public BinaryOperator<Collection<Position>> combiner() {
            return (c1, c2) -> {
                // merge the two collections
                c1.addAll(c2);
                return c1;
            };
        }

        @Override
        public Function<Collection<Position>, PositionSupplier> finisher() {
            // create a new PositionSet of the accumulated Positions
            return c -> new PositionSet(c, portfolioKey, portfolioMarketValue);
        }

        @Override
        public Supplier<Collection<Position>> supplier() {
            return ArrayList::new;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(RuleEvaluator.class);

    private final Rule rule;
    private final PositionSupplier portfolioPositions;
    private final PositionSupplier proposedPositions;
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
        this(rule, portfolioPositions, null, benchmarkPositions, evaluationContext);
    }

    /**
     * Creates a new {@code RuleEvaluator} to evaluate the given {@code Rule} against the given
     * {@code Position}s.
     *
     * @param rule
     *            the {@code Rule} to be evaluated
     * @param portfolioPositions
     *            the {@code Position}s against which to evaluate the {@code Rule}
     * @param proposedPositions
     *            the (possibly {@code null}) proposed {@code Position}s (e.g. by a proposed
     *            {@code Trade} to be combined with the {@code Portfolio} {@code Position}s in a
     *            separate evaluation
     * @param benchmarkPositions
     *            the (possibly {@code null} benchmark {@code Position}s against which to evaluate
     * @param evaluationContext
     *            the context in which the {@code Rule} is to be evaluated
     */
    public RuleEvaluator(Rule rule, PositionSupplier portfolioPositions,
            PositionSupplier proposedPositions, PositionSupplier benchmarkPositions,
            EvaluationContext evaluationContext) {
        this.rule = rule;
        this.portfolioPositions = portfolioPositions;
        this.proposedPositions = proposedPositions;
        this.benchmarkPositions = benchmarkPositions;
        this.evaluationContext = evaluationContext;
    }

    @Override
    public EvaluationResult call() {
        LOG.debug("evaluating Rule {} (\"{}\") on {} Positions for Portfolio {}", rule.getKey(),
                rule.getDescription(), portfolioPositions.size(),
                portfolioPositions.getPortfolioKey());

        // group the Positions of the Portfolio into classifications according to the Rule's
        // GroupClassifier
        Map<EvaluationGroup, PositionSupplier> classifiedPortfolioPositions = classify(
                portfolioPositions, portfolioPositions.getTotalMarketValue(evaluationContext));

        // do the same for the proposed Positions, if specified
        Map<EvaluationGroup, PositionSupplier> classifiedProposedPositions;
        if (proposedPositions == null) {
            classifiedProposedPositions = null;
        } else {
            PositionSupplier concatPositions =
                    PositionSupplier.concat(portfolioPositions, proposedPositions);
            classifiedProposedPositions = classify(concatPositions,
                    concatPositions.getTotalMarketValue(evaluationContext));
        }

        // do the same for the benchmark, if specified
        Map<EvaluationGroup, PositionSupplier> classifiedBenchmarkPositions;
        if (benchmarkPositions == null) {
            classifiedBenchmarkPositions = null;
        } else {
            classifiedBenchmarkPositions = classify(benchmarkPositions,
                    benchmarkPositions.getTotalMarketValue(evaluationContext));
        }

        // Execute the Rule's GroupAggregators (if any) to create additional EvaluationGroups. For
        // example, a Rule may aggregate the Positions holding the top five issuers in the Portfolio
        // into a new group.
        for (GroupAggregator a : rule.getGroupAggregators()) {
            classifiedPortfolioPositions =
                    a.aggregate(classifiedPortfolioPositions, evaluationContext);
            if (classifiedProposedPositions != null) {
                classifiedProposedPositions =
                        a.aggregate(classifiedProposedPositions, evaluationContext);
            }
            if (classifiedBenchmarkPositions != null) {
                classifiedBenchmarkPositions =
                        a.aggregate(classifiedBenchmarkPositions, evaluationContext);
            }
        }

        // for each group of Positions, evaluate the Rule against the group, for the Portfolio,
        // proposed (if specified) and the Benchmark (if specified)
        Map<EvaluationGroup, RuleResult> ruleResults =
                evaluate(classifiedPortfolioPositions, classifiedBenchmarkPositions);

        Map<EvaluationGroup, RuleResult> proposedResults;
        if (classifiedProposedPositions == null) {
            proposedResults = null;
        } else {
            proposedResults = evaluate(classifiedProposedPositions, classifiedBenchmarkPositions);
        }

        return new EvaluationResult(rule.getKey(), ruleResults, proposedResults);
    }

    /**
     * Classifies the given {@code Position}s according to the {@code Rule}'s classifier.
     *
     * @param positions
     *            the {@code Position}s to be classified
     * @param portfolioMarketValue
     *            the (possibly {@code null} portfolio market value to use
     * @return a {@code Map} associating each distinct classification group to the {@code Position}s
     *         comprising the group
     */
    protected Map<EvaluationGroup, PositionSupplier> classify(PositionSupplier positions,
            Double portfolioMarketValue) {
        Predicate<PositionEvaluationContext> positionFilter = rule.getPositionFilter();
        if (positionFilter == null) {
            positionFilter = (p -> true);
        }

        return positions.getPositionsWithContext(evaluationContext).filter(positionFilter)
                .collect(Collectors.groupingBy(p -> rule.getGroupClassifier().classify(p),
                        new PositionSupplierCollector(positions.getPortfolioKey(),
                                portfolioMarketValue)));
    }

    /**
     * Evaluates the given {@code Position}s, optionally against the given benchmark
     * {@code} Positions (if specified).
     *
     * @param evaluatedPositions
     *            the {@code Position}s to be evaluated
     * @param classifiedBenchmarkPositions
     *            the (possibly {@code null} benchmark {@code Position}s to be evaluated against
     * @return the {@code Rule} evaluation results grouped by {@code EvaluationGroup}
     */
    protected Map<EvaluationGroup, RuleResult> evaluate(
            Map<EvaluationGroup, PositionSupplier> evaluatedPositions,
            Map<EvaluationGroup, PositionSupplier> classifiedBenchmarkPositions) {
        return evaluatedPositions.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> {
                    EvaluationGroup group = e.getKey();
                    PositionSupplier portfolioPositions = e.getValue();
                    PositionSupplier benchmarkPositions =
                            (classifiedBenchmarkPositions == null ? null
                                    : classifiedBenchmarkPositions.get(group));

                    // TODO revisit whether reusing benchmark calculations is worthwhile
                    RuleResult singleResult = rule.evaluate(portfolioPositions, benchmarkPositions,
                            evaluationContext);

                    return singleResult;
                }));
    }
}
