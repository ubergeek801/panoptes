package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.function.TriFunction;
import java.io.Serial;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.event.PortfolioCommandEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.SecurityUpdateEvent;
import org.slaq.slaqworx.panoptes.event.TransactionEvent;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg.EvaluationSource;

/**
 * A process function which collects security and portfolio position data and outputs {@link
 * PortfolioEvaluationInput}s when a portfolio is ready for evaluation.
 *
 * @author jeremy
 */
public class PortfolioReadinessEvaluator implements SupplierEx<PortfolioTracker>,
    TriFunction<PortfolioTracker, PortfolioKey, PortfolioEvent,
        Traverser<PortfolioEvaluationInput>> {
  @Serial
  private static final long serialVersionUID = 1L;

  private transient PortfolioTracker portfolioTracker;

  /**
   * Creates a new {@link PortfolioReadinessEvaluator}.
   */
  public PortfolioReadinessEvaluator() {
    // nothing to do
  }

  @Override
  public Traverser<PortfolioEvaluationInput> applyEx(PortfolioTracker processState,
      PortfolioKey eventKey, PortfolioEvent event) {
    portfolioTracker = processState;

    PortfolioEvaluationInput evaluationInput;
    if (event instanceof SecurityUpdateEvent) {
      SecurityKey securityKey = ((SecurityUpdateEvent) event).getSecurityKey();
      evaluationInput = handleSecurityEvent(securityKey);
    } else {
      evaluationInput = handlePortfolioEvent(event);
    }

    return (evaluationInput != null ? Traversers.singleton(evaluationInput) : Traversers.empty());
  }

  @Override
  public PortfolioTracker getEx() {
    return new PortfolioTracker(EvaluationSource.PORTFOLIO);
  }

  /**
   * Handles a portfolio event.
   *
   * @param portfolioEvent
   *     an event containing portfolio constituent data
   *
   * @return a {@link PortfolioEvaluationInput} providing input to the next stage, or {@code null}
   *     if the portfolio is not ready
   */
  protected PortfolioEvaluationInput handlePortfolioEvent(PortfolioEvent portfolioEvent) {
    boolean isPortfolioProcessable;
    Portfolio portfolio;
    if (portfolioEvent instanceof PortfolioCommandEvent) {
      portfolio = portfolioTracker.getPortfolio();
      // process only if the command refers to the keyed portfolio specifically
      isPortfolioProcessable = (portfolio != null &&
          portfolio.getPortfolioKey().equals(portfolioEvent.getPortfolioKey()));
    } else if (portfolioEvent instanceof PortfolioDataEvent) {
      portfolio = ((PortfolioDataEvent) portfolioEvent).getPortfolio();
      // we shouldn't be seeing benchmarks, but ignore them if we do
      if (portfolio.isAbstract()) {
        isPortfolioProcessable = false;
      } else {
        portfolioTracker.trackPortfolio(portfolio);
        isPortfolioProcessable = true;
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
      return portfolioTracker.processPortfolio(portfolio, null, portfolio);
    }

    // can't process yet
    return null;
  }

  /**
   * Handles a security event.
   *
   * @param securityKey
   *     a key identifying the {@link Security} update information obtained from the event
   *
   * @return a {@link PortfolioEvaluationInput} providing input to the next stage, or {@code null}
   *     if the portfolio is not ready
   */
  protected PortfolioEvaluationInput handleSecurityEvent(SecurityKey securityKey) {
    return portfolioTracker.applySecurity(securityKey, portfolioTracker);
  }
}
