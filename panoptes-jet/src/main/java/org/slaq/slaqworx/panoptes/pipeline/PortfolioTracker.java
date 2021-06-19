package org.slaq.slaqworx.panoptes.pipeline;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * A utility for determining whether a tracked portfolio is ready for evaluation (that is, all of
 * its held securities have been encountered).
 *
 * @author jeremy
 */
public class PortfolioTracker implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private static final HashSet<SecurityKey> encounteredSecurities = new HashSet<>();

  private PortfolioKey portfolioKey;
  private Set<SecurityKey> unsatisfiedSecurities;
  private Set<SecurityKey> heldSecurities;

  /**
   * Creates a new {@link PortfolioTracker}.
   */
  protected PortfolioTracker() {
    // nothing to do
  }

  public static void encounterSecurity(@Nonnull SecurityKey security) {
    synchronized (encounteredSecurities) {
      encounteredSecurities.add(security);
    }
  }

  /**
   * Applies the given security to the tracked portfolio, evaluating related rules if appropriate.
   *
   * @param security
   *     a key identifying the security currently being encountered
   *
   * @return {@code true} if evaluation should proceed to the next stage, {@code false} otherwise
   */
  public boolean applySecurity(@Nonnull SecurityKey security) {
    encounterSecurity(security);

    if (portfolioKey == null) {
      // nothing else we can do yet
      return false;
    }

    return evaluateReadiness(portfolioKey, security);
  }

  /**
   * Obtains the key identifying the portfolio being tracked in the current process state.
   *
   * @return a {@link PortfolioKey}, or {@code null} if one has not been associated yet
   */
  public PortfolioKey getPortfolioKey() {
    return portfolioKey;
  }

  /**
   * Registers the given portfolio for tracking in the current process state.
   *
   * @param portfolio
   *     the {@link Portfolio} to be tracked
   */
  public void trackPortfolio(@Nonnull Portfolio portfolio) {
    this.portfolioKey = portfolio.getPortfolioKey();
    // determine which securities have yet to be encountered
    heldSecurities = portfolio.getPositions().map(Position::getSecurityKey)
        .collect(Collectors.toCollection(HashSet::new));
    unsatisfiedSecurities = new HashSet<>(heldSecurities);
    synchronized (encounteredSecurities) {
      unsatisfiedSecurities.removeAll(encounteredSecurities);
    }
  }

  /**
   * Indicates whether the tracked portfolio is "complete," that is, all of its held securities have
   * been encountered.
   *
   * @return {@code true} if the tracked portfolio is complete, {@code false} otherwise
   */
  protected boolean isPortfolioComplete() {
    return (unsatisfiedSecurities != null && unsatisfiedSecurities.isEmpty());
  }

  /**
   * Determines whether the given portfolio is "complete" (all security information has been
   * provided) and whether the currently encountered security (if any) is held, and performs a
   * compliance evaluation if so.
   *
   * @param portfolioKey
   *     the key identifying the portfolio being processed; if {@code null}, then nothing will be
   *     done
   * @param currentSecurity
   *     a key identifying the security being encountered, or {@code null} if a portfolio is being
   *     encountered
   *
   * @return {@code true} if evaluation should proceed to the next stage, {@code false} otherwise
   */
  protected boolean evaluateReadiness(PortfolioKey portfolioKey, SecurityKey currentSecurity) {
    if (portfolioKey == null) {
      return false;
    }

    // update the unsatisfied securities
    if (currentSecurity != null && !isPortfolioComplete()) {
      unsatisfiedSecurities.remove(currentSecurity);
    }
    if (!isPortfolioComplete()) {
      // still have unsatisfied securities, so portfolio isn't ready
      return false;
    }

    // in a security encounter, if the security isn't held by the tracked portfolio, there is
    // nothing to do
    if (currentSecurity != null && !heldSecurities.contains(currentSecurity)) {
      return false;
    }

    // portfolio is ready for evaluation; proceed
    return true;
  }
}
