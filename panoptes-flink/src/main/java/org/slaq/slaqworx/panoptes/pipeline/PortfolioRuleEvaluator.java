package org.slaq.slaqworx.panoptes.pipeline;

import java.io.Serial;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.event.PortfolioCommandEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.event.TransactionEvent;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;

/**
 * A process function which collects security and portfolio position data and evaluates portfolio
 * compliance using the portfolio-supplied rules.
 *
 * @author jeremy
 */
public class PortfolioRuleEvaluator
    extends KeyedBroadcastProcessFunction<
        PortfolioKey, PortfolioEvent, Security, RuleEvaluationResult> {
  @Serial private static final long serialVersionUID = 1L;

  private transient PortfolioTracker portfolioTracker;

  /** Creates a new {@link PortfolioRuleEvaluator}. */
  public PortfolioRuleEvaluator() {
    // nothing to do
  }

  @Override
  public void open(Configuration config) {
    portfolioTracker = new PortfolioTracker(getRuntimeContext(), EvaluationSource.PORTFOLIO);
  }

  @Override
  public void processBroadcastElement(
      Security security,
      KeyedBroadcastProcessFunction<PortfolioKey, PortfolioEvent, Security, RuleEvaluationResult>
              .Context
          context,
      Collector<RuleEvaluationResult> out)
      throws Exception {
    portfolioTracker.applySecurity(context, security, (p -> p.getRules()::iterator), out);
  }

  @Override
  public void processElement(
      PortfolioEvent portfolioEvent,
      KeyedBroadcastProcessFunction<PortfolioKey, PortfolioEvent, Security, RuleEvaluationResult>
              .ReadOnlyContext
          context,
      Collector<RuleEvaluationResult> out)
      throws Exception {
    boolean isPortfolioProcessable;
    Portfolio portfolio;
    if (portfolioEvent instanceof PortfolioCommandEvent) {
      portfolio = portfolioTracker.getPortfolio();
      // process only if the command refers to the keyed portfolio specifically
      isPortfolioProcessable =
          (portfolio != null
              && portfolio.getPortfolioKey().equals(portfolioEvent.getPortfolioKey()));
    } else if (portfolioEvent instanceof PortfolioDataEvent) {
      portfolio = ((PortfolioDataEvent) portfolioEvent).portfolio();
      // we shouldn't be seeing benchmarks, but ignore them if we do
      if (!portfolio.isAbstract()) {
        portfolioTracker.trackPortfolio(portfolio);
        isPortfolioProcessable = true;
      } else {
        isPortfolioProcessable = false;
      }
    } else if (portfolioEvent instanceof TransactionEvent) {
      // FIXME implement; right now just process the portfolio
      portfolio = portfolioTracker.getPortfolio();
      isPortfolioProcessable = true;
    } else {
      // this shouldn't be possible since only the above types of PortfolioEvents exist
      throw new IllegalArgumentException(
          "don't know how to process PortfolioEvent of type " + portfolioEvent.getClass());
    }

    if (isPortfolioProcessable) {
      ReadOnlyBroadcastState<SecurityKey, Security> securityState =
          context.getBroadcastState(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR);
      portfolioTracker.processPortfolio(
          out, portfolio, null, securityState, portfolio.getRules()::iterator);
    }
  }
}
