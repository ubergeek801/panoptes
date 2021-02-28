package org.slaq.slaqworx.panoptes.evaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.GroupAggregator;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Callable} that evaluates a single {@link Rule} against a set of {@link Position}s (and
 * optionally a set of benchmark {@link Position}s). The results are grouped by the {@link
 * EvaluationGroupClassifier} defined by the {@link Rule}; there is always at least one group
 * (unless the input set of {@link Position}s is empty).
 * <p>
 * Given that a {@link RuleEvaluator} requires a {@link EvaluationContext} at construction time, the
 * evaluator can only be used for that context. {@link RuleEvaluator} creation is meant to be
 * inexpensive, however, so a new one can be created for each evaluation session.
 *
 * @author jeremy
 */
public class RuleEvaluator implements Callable<EvaluationResult> {
  private static final Logger LOG = LoggerFactory.getLogger(RuleEvaluator.class);

  private final Rule rule;
  private final PositionSupplier portfolioPositions;
  private final PositionSupplier proposedPositions;
  private final EvaluationContext evaluationContext;

  /**
   * Creates a new {@link RuleEvaluator} to evaluate the given {@link Rule} against the given {@link
   * Position}s.
   *
   * @param rule
   *     the {@link Rule} to be evaluated
   * @param portfolioPositions
   *     the {@link Position}s against which to evaluate the {@link Rule}
   * @param evaluationContext
   *     the context in which the {@link Rule} is to be evaluated
   */
  public RuleEvaluator(Rule rule, PositionSupplier portfolioPositions,
      EvaluationContext evaluationContext) {
    this(rule, portfolioPositions, null, evaluationContext);
  }

  /**
   * Creates a new {@link RuleEvaluator} to evaluate the given {@link Rule} against the given {@link
   * Position}s.
   *
   * @param rule
   *     the {@link Rule} to be evaluated
   * @param portfolioPositions
   *     the {@link Position}s against which to evaluate the {@link Rule}
   * @param proposedPositions
   *     the (possibly {@code null}) proposed {@link Position}s (e.g. by a proposed {@link Trade} to
   *     be combined with the {@link Portfolio} {@link Position}s in a separate evaluation
   * @param evaluationContext
   *     the context in which the {@link Rule} is to be evaluated
   */
  public RuleEvaluator(Rule rule, PositionSupplier portfolioPositions,
      PositionSupplier proposedPositions, EvaluationContext evaluationContext) {
    this.rule = rule;
    this.portfolioPositions = portfolioPositions;
    this.proposedPositions = proposedPositions;
    this.evaluationContext = evaluationContext;
    // a new EvaluationContext should be provided to each new RuleEvaluator, but just in case...
    evaluationContext.clear();
  }

  @Override
  public EvaluationResult call() {
    LOG.debug("evaluating Rule {} (\"{}\") on {} Positions for Portfolio {}", rule.getKey(),
        rule.getDescription(), portfolioPositions.size(), portfolioPositions.getPortfolioKey());

    // group the Positions of the Portfolio into classifications according to the Rule's
    // GroupClassifier
    Map<EvaluationGroup, PositionSupplier> classifiedPortfolioPositions =
        classify(portfolioPositions, evaluationContext.getMarketValue(portfolioPositions));

    // do the same for the proposed Positions, if specified
    Map<EvaluationGroup, PositionSupplier> classifiedProposedPositions;
    if (proposedPositions == null) {
      classifiedProposedPositions = null;
    } else {
      PositionSupplier concatPositions =
          PositionSupplier.concat(portfolioPositions, proposedPositions);
      classifiedProposedPositions =
          classify(concatPositions, evaluationContext.getMarketValue(concatPositions));
    }

    // Execute the Rule's GroupAggregators (if any) to create additional EvaluationGroups. For
    // example, a Rule may aggregate the Positions holding the top five issuers in the Portfolio
    // into a new group.
    for (GroupAggregator a : rule.getGroupAggregators()) {
      classifiedPortfolioPositions = a.aggregate(classifiedPortfolioPositions, evaluationContext);
      if (classifiedProposedPositions != null) {
        classifiedProposedPositions = a.aggregate(classifiedProposedPositions, evaluationContext);
      }
    }

    // for each group of Positions, evaluate the Rule against the group, for the Portfolio and
    // (if specified) for the Portfolio + proposed positions
    Map<EvaluationGroup, ValueResult> ruleResults = evaluate(classifiedPortfolioPositions);

    Map<EvaluationGroup, ValueResult> proposedResults;
    if (classifiedProposedPositions == null) {
      proposedResults = null;
    } else {
      proposedResults = evaluate(classifiedProposedPositions);
    }

    return new EvaluationResult(rule.getKey(), ruleResults, proposedResults);
  }

  /**
   * Classifies the given {@link Position}s according to the {@link Rule}'s classifier.
   *
   * @param positions
   *     the {@link Position}s to be classified
   * @param portfolioMarketValue
   *     the (possibly {@code null} portfolio market value to use
   *
   * @return a {@link Map} associating each distinct classification group to the {@link Position}s
   *     comprising the group
   */
  protected Map<EvaluationGroup, PositionSupplier> classify(PositionSupplier positions,
      Double portfolioMarketValue) {
    Predicate<PositionEvaluationContext> positionFilter = rule.getPositionFilter();
    if (positionFilter == null) {
      positionFilter = (p -> true);
    }

    return positions.getPositionsWithContext(evaluationContext).filter(positionFilter).collect(
        Collectors.groupingBy(p -> rule.getGroupClassifier().classify(p),
            new PositionSupplierCollector(positions.getPortfolioKey(), portfolioMarketValue)));
  }

  /**
   * Evaluates the given {@link Position}s.
   *
   * @param evaluatedPositions
   *     the {@link Position}s to be evaluated
   *
   * @return the {@link Rule} evaluation results grouped by {@link EvaluationGroup}
   */
  protected Map<EvaluationGroup, ValueResult> evaluate(
      Map<EvaluationGroup, PositionSupplier> evaluatedPositions) {
    return evaluatedPositions.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> {
      EvaluationGroup evaluationGroup = e.getKey();
      PositionSupplier portfolioPositions = e.getValue();

      ValueResult singleResult =
          rule.evaluate(portfolioPositions, evaluationGroup, evaluationContext);

      return singleResult;
    }));
  }

  /**
   * A {@link Collector} that operates on a {@link Stream} of {@link Position}s to collect into a
   * new {@link PositionSupplier}, using a {@link Collection} as an accumulator.
   *
   * @author jeremy
   */
  private class PositionSupplierCollector
      implements Collector<PositionEvaluationContext, Collection<Position>, PositionSupplier> {
    private final PortfolioKey portfolioKey;
    private final Double portfolioMarketValue;

    /**
     * Creates a new {@link PositionSupplierCollector}.
     *
     * @param portfolioKey
     *     the (possibly {@code null} key of the associated {@link Portfolio}
     * @param portfolioMarketValue
     *     the (possibly {@code null} portfolio market value to specify on the created {@link
     *     PositionSupplier}
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
      return c -> new PositionSet<>(c, portfolioKey, portfolioMarketValue);
    }

    @Override
    public Supplier<Collection<Position>> supplier() {
      return ArrayList::new;
    }
  }
}
