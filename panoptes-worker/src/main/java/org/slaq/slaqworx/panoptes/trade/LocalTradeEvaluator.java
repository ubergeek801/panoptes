package org.slaq.slaqworx.panoptes.trade;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Named;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.tuple.Pair;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TradeEvaluator} which performs processing on the local node.
 *
 * @author jeremy
 */
@Prototype
@Named("local")
public class LocalTradeEvaluator implements TradeEvaluator {
  protected static final double ROOM_TOLERANCE = 500;
  protected static final double MIN_ALLOCATION = 1000;
  private static final Logger LOG = LoggerFactory.getLogger(TradeEvaluator.class);
  private static final double LOG_2 = Math.log(2);

  private final PortfolioEvaluator evaluator;
  private final PortfolioProvider portfolioProvider;
  private final SecurityProvider securityProvider;

  /**
   * Creates a new {@link LocalTradeEvaluator}.
   *
   * @param evaluator
   *     the {@link PortfolioEvaluator} to use to perform {@link Portfolio}-level evaluations
   * @param assetCache
   *     the {@link AssetCache} to use to resolve {@link Portfolio} and {@link Security} references
   */
  public LocalTradeEvaluator(@Named("local") PortfolioEvaluator evaluator, AssetCache assetCache) {
    this(evaluator, assetCache, assetCache);
  }

  /**
   * Creates a new {@link LocalTradeEvaluator}.
   *
   * @param evaluator
   *     the {@link PortfolioEvaluator} to use to perform {@link Portfolio}-level evaluations
   * @param portfolioProvider
   *     the {@link PortfolioProvider} to use to obtain {@link Portfolio} information
   * @param securityProvider
   *     the {@link SecurityProvider} to use to obtain {@link Security} information
   */
  public LocalTradeEvaluator(@Named("local") PortfolioEvaluator evaluator,
      PortfolioProvider portfolioProvider, SecurityProvider securityProvider) {
    this.evaluator = evaluator;
    this.portfolioProvider = portfolioProvider;
    this.securityProvider = securityProvider;
  }

  @Override
  public CompletableFuture<TradeEvaluationResult> evaluate(Trade trade,
      EvaluationContext evaluationContext) throws ExecutionException, InterruptedException {
    LOG.info("evaluating trade {} with {} allocations", trade.getKey(), trade.getAllocationCount());
    // evaluate the impact on each Portfolio
    return CompletableFuture.completedFuture(AssetCache.getLocalExecutor().submit(
        () -> trade.getTransactions().entrySet().parallelStream().map(portfolioTransactionEntry -> {
          PortfolioKey portfolioKey = portfolioTransactionEntry.getKey();
          Transaction transaction = portfolioTransactionEntry.getValue();

          try {
            Map<RuleKey, EvaluationResult> ruleResults =
                evaluator.evaluate(portfolioKey, transaction, evaluationContext.copy()).get();

            return Pair.of(portfolioKey, ruleResults);
          } catch (Exception e) {
            // FIXME throw a better exception
            throw new RuntimeException("could not evaluate trade", e);
          }
        }).collect(() -> new TradeEvaluationResult(trade.getKey()),
            TradeEvaluationResult::addImpacts, TradeEvaluationResult::merge)).get());
  }

  @Override
  public CompletableFuture<Double> evaluateRoom(PortfolioKey portfolioKey, SecurityKey securityKey,
      double targetValue) throws ExecutionException, InterruptedException {
    // first try the minimum allocation to quickly eliminate Portfolios with no room at all

    EvaluationContext evaluationContext = new EvaluationContext(securityProvider, portfolioProvider,
        EvaluationMode.SHORT_CIRCUIT_EVALUATION);

    double minCompliantValue = MIN_ALLOCATION;
    double trialValue = minCompliantValue;
    TradeEvaluationResult evaluationResult =
        testRoom(portfolioKey, securityKey, trialValue, evaluationContext).get();
    if (!evaluationResult.isCompliant()) {
      // even the minimum allocation failed; give up now
      return CompletableFuture.completedFuture(0d);
    }

    // we can allocate at least the minimum; try now for the max (target) value and iterate from
    // there

    trialValue = targetValue;
    double minNoncompliantValue = trialValue;
    int maxRoomIterations = (int) Math.ceil(Math.log(targetValue / ROOM_TOLERANCE) / LOG_2) + 1;
    for (int i = 0; i < maxRoomIterations; i++) {
      evaluationResult = testRoom(portfolioKey, securityKey, trialValue, evaluationContext).get();
      if (evaluationResult.isCompliant()) {
        if (minCompliantValue < trialValue) {
          // we have a new low-water mark for what is compliant
          minCompliantValue = trialValue;

          // now try an amount halfway between the current amount and the lowest amount
          // known to be noncompliant
          trialValue = (trialValue + minNoncompliantValue) / 2;
          // if the new trial value is sufficiently close to the old one, then call it
          // good
          if (Math.abs(trialValue - minCompliantValue) < ROOM_TOLERANCE) {
            return CompletableFuture.completedFuture(minCompliantValue);
          }
        }
      } else {
        minNoncompliantValue = trialValue;
        trialValue = (minCompliantValue + trialValue) / 2;
      }
    }

    // number of iterations has expired; this is our final answer
    return CompletableFuture.completedFuture(minCompliantValue);
  }

  /**
   * Tests for the requested amount of room in the given {@link Security} for the given {@link
   * Portfolio}.
   *
   * @param portfolioKey
   *     the {@link PortfolioKey} identifying the {@link Portfolio} in which to find room
   * @param securityKey
   *     the {@link SecurityKey} identifying the {@link Security} name for which to find room
   * @param targetValue
   *     the desired investment amount, as USD market value
   * @param evaluationContext
   *     the {@link EvaluationContext} under which to perform the evaluation
   *
   * @return a {@link CompletableFuture} {@link TradeEvaluationResult} indicating the result of the
   *     evaluation
   *
   * @throws ExecutionException
   *     if the calculation could not be processed
   * @throws InterruptedException
   *     if the {@link Thread} was interrupted during processing
   */
  protected CompletableFuture<TradeEvaluationResult> testRoom(PortfolioKey portfolioKey,
      SecurityKey securityKey, double targetValue, EvaluationContext evaluationContext)
      throws ExecutionException, InterruptedException {
    TaxLot trialAllocation = new TaxLot(targetValue, securityKey);
    Transaction trialTransaction = new Transaction(portfolioKey, List.of(trialAllocation));
    LocalDate tradeDate = LocalDate.now();
    Trade trialTrade = new Trade(tradeDate, tradeDate, Map.of(portfolioKey, trialTransaction));

    return evaluate(trialTrade, evaluationContext);
  }
}
