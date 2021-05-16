package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.core.Edge;
import java.util.ArrayList;
import java.util.HashMap;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.dag.TypedProcessor;
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

public class ReadinessEvaluator
    extends TypedProcessor<PortfolioEvent, Security, PortfolioEvaluationInput> {
  private final HashMap<PortfolioKey, PortfolioTracker> portfolioTrackers = new HashMap<>();

  public ReadinessEvaluator() {
    // nothing to do
  }

  @Override
  protected Edge configureEdge0(Edge edge) {
    return super.configureEdge0(edge).partitioned(e -> ((PortfolioEvent) e).getKey());
  }

  @Override
  protected Edge configureEdge1(Edge edge) {
    return super.configureEdge0(edge).broadcast().distributed();
  }

  @Override
  protected @Nonnull
  ReadinessEvaluator newInstance() {
    return new ReadinessEvaluator();
  }

  @Override
  protected boolean process0(@Nonnull PortfolioEvent portfolioEvent) {
    // FIXME could be PORTFOLIO or BENCHMARK
    PortfolioTracker portfolioTracker = portfolioTrackers.computeIfAbsent(portfolioEvent.getKey(),
        k -> new PortfolioTracker(EvaluationSource.PORTFOLIO));

    PortfolioEvaluationInput evaluationInput;
    if (portfolioEvent instanceof SecurityUpdateEvent) {
      SecurityKey securityKey = ((SecurityUpdateEvent) portfolioEvent).getSecurityKey();
      evaluationInput = handleSecurityEvent(securityKey, portfolioTracker);
    } else {
      evaluationInput = handlePortfolioEvent(portfolioEvent, portfolioTracker);
    }

    Traverser<PortfolioEvaluationInput> output =
        (evaluationInput != null ? Traversers.singleton(evaluationInput) : Traversers.empty());

    return emitFromTraverser(output);
  }

  @Override
  protected boolean process1(@Nonnull Security security) {
    // this may not really belong here but we'll leave it for now
    PanoptesApp.getAssetCache().getSecurityCache().set(security.getKey(), security);

    ArrayList<PortfolioEvaluationInput> allOutput = new ArrayList<>();

    portfolioTrackers.forEach((key, tracker) -> {
      PortfolioEvaluationInput output = handleSecurityEvent(security.getKey(), tracker);
      if (output != null) {
        allOutput.add(output);
      }
    });

    return emitFromTraverser(Traversers.traverseIterable(allOutput));
  }

  /**
   * Handles a portfolio event.
   *
   * @param portfolioEvent
   *     an event containing portfolio constituent data
   * @param portfolioTracker
   *     the {@link PortfolioTracker} that tracks the current portfolio
   *
   * @return a {@link PortfolioEvaluationInput} providing input to the next stage, or {@code null}
   *     if the portfolio is not ready
   */
  protected PortfolioEvaluationInput handlePortfolioEvent(PortfolioEvent portfolioEvent,
      PortfolioTracker portfolioTracker) {
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
   * @param portfolioTracker
   *     the {@link PortfolioTracker} that tracks the current portfolio
   *
   * @return a {@link PortfolioEvaluationInput} providing input to the next stage, or {@code null}
   *     if the portfolio is not ready
   */
  protected PortfolioEvaluationInput handleSecurityEvent(SecurityKey securityKey,
      PortfolioTracker portfolioTracker) {
    return portfolioTracker.applySecurity(securityKey, portfolioTracker);
  }
}
