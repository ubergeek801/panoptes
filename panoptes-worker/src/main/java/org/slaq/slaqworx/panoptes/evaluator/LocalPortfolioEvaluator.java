package org.slaq.slaqworx.panoptes.evaluator;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Named;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link PortfolioEvaluator} which performs processing on the local node. {@link Rule}s are
 * evaluated sequentially (although in no particular order); any desired processing concurrency is
 * expected to be provided at a higher layer of abstraction.
 *
 * @author jeremy
 */
@Prototype
@Named("local")
public class LocalPortfolioEvaluator implements PortfolioEvaluator {
  private static final Logger LOG = LoggerFactory.getLogger(LocalPortfolioEvaluator.class);
  private final PortfolioProvider portfolioProvider;

  /**
   * Creates a new {@link LocalPortfolioEvaluator}.
   *
   * @param portfolioProvider
   *     the {@link PortfolioProvider} to use to resolve {@link Portfolio} references
   */
  public LocalPortfolioEvaluator(PortfolioProvider portfolioProvider) {
    this.portfolioProvider = portfolioProvider;
  }

  @Override
  public CompletableFuture<Map<RuleKey, EvaluationResult>> evaluate(PortfolioKey portfolioKey,
      EvaluationContext evaluationContext) {
    return evaluate(portfolioKey, null, evaluationContext);
  }

  @Override
  public CompletableFuture<Map<RuleKey, EvaluationResult>> evaluate(PortfolioKey portfolioKey,
      Transaction transaction, EvaluationContext evaluationContext) {
    Portfolio portfolio = portfolioProvider.getPortfolio(portfolioKey);

    return CompletableFuture
        .completedFuture(evaluate(portfolio.getRules(), portfolio, transaction, evaluationContext));
  }

  /**
   * Evaluates the given {@link Rule}s against the given {@link Portfolio}.
   *
   * @param rules
   *     the {@link Rule}s to be evaluated
   * @param portfolio
   *     the {@link Portfolio} against which to evaluate the {@link Rule}s
   * @param transaction
   *     a (possibly {@code null} {@link Transaction} to optionally evaluate with the {@link
   *     Portfolio}
   * @param evaluationContext
   *     the {@link EvaluationContext} under which to evaluate
   *
   * @return a {@link Map} associating each evaluated {@link Rule} with its result
   */
  protected Map<RuleKey, EvaluationResult> evaluate(Stream<Rule> rules, Portfolio portfolio,
      Transaction transaction, EvaluationContext evaluationContext) {
    LOG.info("locally evaluating Portfolio {} (\"{}\")", portfolio.getKey(), portfolio.getName());
    long startTime = System.currentTimeMillis();

    ShortCircuiter shortCircuiter = new ShortCircuiter(portfolio.getKey(), transaction,
        evaluationContext.getEvaluationMode() == EvaluationMode.SHORT_CIRCUIT_EVALUATION);

    // evaluate the Rules
    Map<RuleKey, EvaluationResult> results = rules.takeWhile(shortCircuiter).map(r -> {
      EvaluationResult baseResult =
          new RuleEvaluator(r, portfolio, transaction, evaluationContext).call();
      Portfolio benchmark = portfolio.getBenchmark(portfolioProvider);
      EvaluationResult benchmarkResult;
      if (benchmark != null && r.isBenchmarkSupported()) {
        // attempt to get from cache first
        benchmarkResult = evaluationContext.getBenchmarkResult(r.getKey());
        if (benchmarkResult == null) {
          benchmarkResult = new RuleEvaluator(r, benchmark, transaction, evaluationContext).call();
          evaluationContext.cacheBenchmarkValue(r.getKey(), benchmarkResult);
        }
      } else {
        benchmarkResult = null;
      }

      return new BenchmarkComparator().compare(baseResult, benchmarkResult, r);
    }).peek(shortCircuiter)
        .collect(Collectors.toMap(EvaluationResult::getRuleKey, result -> result));

    LOG.info("evaluated {} Rules ({}) over {} Positions for Portfolio {} in {} ms", results.size(),
        (shortCircuiter.isShortCircuited() ? "short-circuited" : "full"), portfolio.size(),
        portfolio.getKey(), System.currentTimeMillis() - startTime);

    return results;
  }

  /**
   * A {@link Predicate} and {@link Consumer} intended for use with on a {@link Stream} of {@link
   * Rule}s. The {@link Stream} is "short-circuited" by {@code takeWhile()} after a failed result is
   * encountered by {@code peek()}.
   * <p>
   * Note that a {@link Predicate} used by {@code takeWhile()} is expected to be stateless; we bend
   * that definition somewhat by maintaining state (specifically, whether a failed result has
   * already been seen). However, this is consistent with the semantics of short-circuit evaluation,
   * which allow any number of results to be returned when an evaluation is short-circuited, as long
   * as at least one of them indicates a failure.
   *
   * @author jeremy
   */
  private static class ShortCircuiter implements Consumer<EvaluationResult>, Predicate<Rule> {
    private final PortfolioKey portfolioKey;
    private final Transaction transaction;
    private final boolean isShortCircuiting;

    private EvaluationResult failedResult;

    /**
     * Creates a new {@link ShortCircuiter}.
     *
     * @param portfolioKey
     *     a {@link PortfolioKey} identifying the {@link Portfolio} being evaluated
     * @param transaction
     *     a (possibly {@code null} {@link Transaction} being evaluated with the {@link Portfolio}
     * @param isShortCircuiting
     *     {@code true} if short-circuiting is to be activated, {@code false} to allow all
     *     evaluations to pass through
     */
    public ShortCircuiter(PortfolioKey portfolioKey, Transaction transaction,
        boolean isShortCircuiting) {
      this.portfolioKey = portfolioKey;
      this.transaction = transaction;
      this.isShortCircuiting = isShortCircuiting;
    }

    @Override
    public void accept(EvaluationResult result) {
      if (!isShortCircuiting) {
        // nothing to do
        return;
      }

      if (failedResult != null) {
        // no need to inspect any more results
        return;
      }

      boolean isPassed;
      if (transaction == null) {
        // stop when a Rule fails
        isPassed = result.isPassed();
      } else {
        // stop when a non-compliant impact is encountered
        TradeEvaluationResult tradeResult = new TradeEvaluationResult(transaction.getTradeKey());
        tradeResult.addImpacts(portfolioKey, Map.of(result.getRuleKey(), result));
        isPassed = tradeResult.isCompliant();
      }

      if (!isPassed) {
        failedResult = result;
      }
    }

    /**
     * Indicates whether a short-circuit was triggered.
     *
     * @return {@code true} if at least one evaluation failed and triggered a short-circuit, {@code
     *     false} otherwise
     */
    public boolean isShortCircuited() {
      return (failedResult != null);
    }

    @Override
    public boolean test(Rule t) {
      // if a failed result has been encountered already, remaining Rules can be filtered
      return (failedResult == null);
    }
  }
}
