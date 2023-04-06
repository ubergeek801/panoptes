package org.slaq.slaqworx.panoptes.pipeline;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.apache.commons.collections.IteratorUtils;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility for determining whether a tracked portfolio is ready for evaluation (that is, all of
 * its held securities have been encountered), and for performing the rule evaluations when ready.
 *
 * @author jeremy
 */
public class PortfolioTracker implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(PortfolioTracker.class);

  private static final ValueStateDescriptor<Portfolio> PORTFOLIO_STATE_DESCRIPTOR =
      new ValueStateDescriptor<>("portfolio", Portfolio.class);

  private static final ConcurrentHashMap<PortfolioKey, Boolean> portfolioCompleteState =
      new ConcurrentHashMap<>();

  private final EvaluationSource evaluationSource;
  private final transient ValueState<Portfolio> portfolioState;

  /**
   * Creates a new {@link PortfolioTracker} using the given {@link RuntimeContext} to create process
   * state.
   *
   * @param context the {@link RuntimeContext} in which to create process state
   * @param evaluationSource the type of source portfolio (portfolio or benchmark) being tracked
   */
  protected PortfolioTracker(RuntimeContext context, EvaluationSource evaluationSource) {
    portfolioState = context.getState(PORTFOLIO_STATE_DESCRIPTOR);
    this.evaluationSource = evaluationSource;
  }

  /**
   * Applies the given security to the tracked portfolio, evaluating related rules if appropriate.
   *
   * @param context the process context related to the security event
   * @param security the security currently being encountered
   * @param ruleProvider a {@link Function} which provides rules to be evaluated for a given {@link
   *     Portfolio}
   * @param out a {@link Collector} to which rule evaluation results, if any, are output
   * @throws Exception if a state-related operation fails
   */
  public void applySecurity(
      KeyedBroadcastProcessFunction<PortfolioKey, PortfolioEvent, Security, RuleEvaluationResult>
              .Context
          context,
      Security security,
      Function<Portfolio, Iterable<Rule>> ruleProvider,
      Collector<RuleEvaluationResult> out)
      throws Exception {
    BroadcastState<SecurityKey, Security> securityState =
        context.getBroadcastState(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR);
    securityState.put(security.getKey(), security);

    context.applyToKeyedState(
        PORTFOLIO_STATE_DESCRIPTOR,
        (portfolioKey, state) -> {
          Portfolio portfolio = state.value();
          processPortfolio(out, portfolio, security, securityState, ruleProvider.apply(portfolio));
        });
  }

  /**
   * Obtains the portfolio being tracked in the current process state.
   *
   * @return a {@link Portfolio}
   * @throws IOException if the state operation fails
   */
  public Portfolio getPortfolio() throws IOException {
    return portfolioState.value();
  }

  /**
   * Register the given portfolio for tracking in the current process state.
   *
   * @param portfolio the {@link Portfolio} to be tracked
   * @throws IOException if the state operation fails
   */
  public void trackPortfolio(Portfolio portfolio) throws IOException {
    portfolioState.update(portfolio);
  }

  /**
   * Performs a portfolio evaluation and publishes the result.
   *
   * @param out the {@link Collector} to which to output compliance results
   * @param portfolio the portfolio being processed
   * @param securityState the security information currently held in broadcast state
   * @param rules the rules to be evaluated
   */
  protected void evaluatePortfolio(
      Collector<RuleEvaluationResult> out,
      Portfolio portfolio,
      ReadOnlyBroadcastState<SecurityKey, Security> securityState,
      Collection<Rule> rules) {
    // this is questionable but there shouldn't be any other portfolios queried
    PortfolioProvider portfolioProvider = (k -> portfolio);
    SecurityProvider securityProvider =
        (k, context) -> {
          try {
            return securityState.get(k);
          } catch (Exception e) {
            // FIXME throw a real exception
            throw new RuntimeException("could not get security " + k, e);
          }
        };

    LOG.info(
        "processing {} rules for {} {} (\"{}\")",
        rules.size(),
        evaluationSource,
        portfolio.getKey(),
        portfolio.getName());
    rules.forEach(
        rule -> {
          // FIXME get/generate eventId
          long eventId = System.currentTimeMillis();

          EvaluationResult evaluationResult =
              new org.slaq.slaqworx.panoptes.evaluator.RuleEvaluator(
                      rule,
                      portfolio,
                      new EvaluationContext(null, securityProvider, portfolioProvider))
                  .call();
          // enrich the result with some other essential information
          RuleEvaluationResult ruleEvaluationResult =
              new RuleEvaluationResult(
                  eventId,
                  portfolio.getKey(),
                  portfolio.getBenchmarkKey(),
                  evaluationSource,
                  rule.isBenchmarkSupported(),
                  rule.lowerLimit(),
                  rule.upperLimit(),
                  evaluationResult);
          out.collect(ruleEvaluationResult);
        });
    LOG.info(
        "processed {} rules for {} {} (\"{}\")",
        rules.size(),
        evaluationSource,
        portfolio.getKey(),
        portfolio.getName());
  }

  /**
   * Determines whether the given portfolio is "complete" (all security information has been
   * provided) and performs a compliance evaluation if so.
   *
   * @param out the {@link Collector} to which to output compliance results
   * @param portfolio the portfolio being processed; if {@code null}, then nothing will be done
   * @param currentSecurity the security being encountered, or {@code null} if a portfolio is being
   *     encountered
   * @param securityState the security information currently held in broadcast state
   * @param rules the rules to be evaluated; if {@code null} or empty, then nothing will be done
   * @throws Exception if an error occurs during processing
   */
  protected void processPortfolio(
      Collector<RuleEvaluationResult> out,
      Portfolio portfolio,
      Security currentSecurity,
      ReadOnlyBroadcastState<SecurityKey, Security> securityState,
      Iterable<Rule> rules)
      throws Exception {
    if (portfolio == null) {
      return;
    }

    // if there are no rules to be evaluated, then don't bother
    List<Rule> ruleCollection = IteratorUtils.toList(rules.iterator());
    if (ruleCollection.isEmpty()) {
      return;
    }

    boolean needSecurityHeld = (currentSecurity != null);
    boolean needPortfolioComplete =
        (!Boolean.TRUE.equals(portfolioCompleteState.get(portfolio.getKey())));

    if (needSecurityHeld || needPortfolioComplete) {
      // determine whether we have all held securities for the portfolio, and whether the
      // current security is in the portfolio
      boolean isComplete = true;
      boolean isCurrentSecurityHeld = (currentSecurity == null);
      Iterator<? extends Position> positionIter = portfolio.getPositions().iterator();
      while (positionIter.hasNext()) {
        Position position = positionIter.next();
        if (!securityState.contains(position.getSecurityKey())) {
          isComplete = false;
          break;
        }
        if (currentSecurity != null && position.getSecurityKey().equals(currentSecurity.getKey())) {
          isCurrentSecurityHeld = true;
        }
      }

      if (isComplete) {
        portfolioCompleteState.put(portfolio.getKey(), true);
      }

      if (!isComplete || !isCurrentSecurityHeld) {
        // we are either not ready or not affected
        return;
      }
    }

    // portfolio is ready for evaluation; proceed
    evaluatePortfolio(out, portfolio, securityState, ruleCollection);
  }
}
