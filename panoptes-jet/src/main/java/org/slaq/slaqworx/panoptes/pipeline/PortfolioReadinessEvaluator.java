package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.function.TriFunction;
import java.io.Serial;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.event.PortfolioCommandEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.TransactionEvent;

/**
 * A transformation which collects security and portfolio position data and outputs {@link
 * PortfolioKey}s when a portfolio is ready for evaluation. This logic applies to both "standard"
 * portfolios and benchmarks.
 *
 * @author jeremy
 */
public class PortfolioReadinessEvaluator implements SupplierEx<PortfolioTracker> {
  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Provides a {@link TriFunction} to handle {@link PortfolioEvent}s, which are keyed/partitioned.
   *
   * @return the {@link PortfolioEvent} handling function
   */
  public TriFunction<PortfolioTracker, PortfolioKey, PortfolioEvent, Traverser<PortfolioKey>> portfolioEventHandler() {
    return (t, k, e) -> handlePortfolioEvent(t, e);
  }

  /**
   * Provides a {@link TriFunction} to handle {@link Security} events, which are broadcast.
   *
   * @return the {@link Security} event handling function
   */
  public TriFunction<PortfolioTracker, PortfolioKey, Security, Traverser<PortfolioKey>> securityHandler() {
    return (t, k, s) -> handleSecurityEvent(t, s.getKey());
  }

  /**
   * Creates a new {@link PortfolioReadinessEvaluator}.
   */
  public PortfolioReadinessEvaluator() {
    // nothing to do
  }

  @Override
  @Nonnull
  public PortfolioTracker getEx() {
    return new PortfolioTracker();
  }

  /**
   * Handles a portfolio event.
   *
   * @param portfolioTracker
   *     a {@link PortfolioTracker} that manages the portfolio readiness state
   * @param portfolioEvent
   *     an event containing portfolio constituent data
   *
   * @return a {@link Traverser} which emits {@link PortfolioKey}s ready to be evaluated
   */
  @Nonnull
  protected Traverser<PortfolioKey> handlePortfolioEvent(@Nonnull PortfolioTracker portfolioTracker,
      @Nonnull PortfolioEvent portfolioEvent) {
    boolean isPortfolioProcessable;
    PortfolioKey portfolioKey;
    if (portfolioEvent instanceof PortfolioCommandEvent) {
      portfolioKey = portfolioTracker.getPortfolioKey();
      // process only if the command refers to the keyed portfolio specifically
      isPortfolioProcessable =
          (portfolioKey != null && portfolioKey.equals(portfolioEvent.getPortfolioKey()));
    } else if (portfolioEvent instanceof PortfolioDataEvent portfolioDataEvent) {
      portfolioKey = portfolioEvent.getPortfolioKey();
      portfolioTracker.trackPortfolio(portfolioDataEvent.getPortfolio());
      isPortfolioProcessable = true;
    } else if (portfolioEvent instanceof TransactionEvent) {
      // FIXME implement; right now just process the portfolio
      portfolioKey = portfolioTracker.getPortfolioKey();
      isPortfolioProcessable = true;
    } else {
      // this shouldn't be possible since only the above types of PortfolioEvents exist
      throw new IllegalArgumentException(
          "don't know how to process PortfolioEvent of type " + portfolioEvent.getClass());
    }

    boolean isReady =
        (isPortfolioProcessable && portfolioTracker.evaluateReadiness(portfolioKey, null));
    if (!isReady) {
      return Traversers.empty();
    }

    return Traversers.singleton(portfolioTracker.getPortfolioKey());
  }

  /**
   * Handles a security event.
   *
   * @param portfolioTracker
   *     a {@link PortfolioTracker} that manages the portfolio readiness state
   * @param securityKey
   *     a key identifying the {@link Security} update information obtained from the event
   *
   * @return a {@link Traverser} which emits {@link PortfolioKey}s ready to be evaluated
   */
  @Nonnull
  protected Traverser<PortfolioKey> handleSecurityEvent(PortfolioTracker portfolioTracker,
      @Nonnull SecurityKey securityKey) {
    PortfolioTracker.encounterSecurity(securityKey);

    // since securities are broadcast, they can arrive before a portfolio is encountered
    if (portfolioTracker == null) {
      return Traversers.empty();
    }

    boolean isReady = portfolioTracker.applySecurity(securityKey);
    if (!isReady) {
      return Traversers.empty();
    }

    return Traversers.singleton(portfolioTracker.getPortfolioKey());
  }
}
