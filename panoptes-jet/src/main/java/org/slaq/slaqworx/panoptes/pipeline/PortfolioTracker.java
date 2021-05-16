package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.map.IMap;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RulesProvider;

/**
 * A utility for determining whether a tracked portfolio is ready for evaluation (that is, all of
 * its held securities have been encountered), and for performing the rule evaluations when ready.
 *
 * @author jeremy
 */
public class PortfolioTracker implements Serializable, RulesProvider {
  @Serial
  private static final long serialVersionUID = 1L;

  private final EvaluationSource evaluationSource;
  private Portfolio portfolio;
  private Set<SecurityKey> unsatisfiedSecurities;
  private Set<SecurityKey> heldSecurities;

  /**
   * Creates a new {@link PortfolioTracker}.
   *
   * @param evaluationSource
   *     the type of portfolio (portfolio or benchmark) being tracked
   */
  protected PortfolioTracker(EvaluationSource evaluationSource) {
    this.evaluationSource = evaluationSource;
  }

  /**
   * Applies the given security to the tracked portfolio, evaluating related rules if appropriate.
   *
   * @param security
   *     a key identifying the security currently being encountered
   * @param rulesProvider
   *     a {@link RulesProvider} providing rules to be evaluated
   *
   * @return a {@link PortfolioEvaluationInput} providing input to the next stage, or {@code null}
   *     if the portfolio is not ready
   */
  public PortfolioEvaluationInput applySecurity(SecurityKey security, RulesProvider rulesProvider) {
    if (portfolio == null) {
      // nothing we can do yet
      return null;
    }

    return processPortfolio(portfolio, security, rulesProvider);
  }

  /**
   * Obtains the portfolio being tracked in the current process state.
   *
   * @return a {@link Portfolio}
   */
  public Portfolio getPortfolio() {
    return portfolio;
  }

  @Override
  public Stream<Rule> getRules() {
    return (portfolio == null ? Stream.empty() : portfolio.getRules());
  }

  /**
   * Registers the given portfolio for tracking in the current process state.
   *
   * @param portfolio
   *     the {@link Portfolio} to be tracked
   */
  public void trackPortfolio(Portfolio portfolio) {
    this.portfolio = portfolio;
    // determine which securities have yet to be encountered
    heldSecurities = portfolio.getPositions().map(Position::getSecurityKey)
        .collect(Collectors.toCollection(HashSet::new));
    unsatisfiedSecurities = new HashSet<>(heldSecurities);
    IMap<SecurityKey, Security> securityMap = PanoptesApp.getAssetCache().getSecurityCache();
    unsatisfiedSecurities.removeIf(securityMap::containsKey);
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
   * @param portfolio
   *     the portfolio being processed; if {@code null}, then nothing will be done
   * @param currentSecurity
   *     a key identifying the security being encountered, or {@code null} if a portfolio is being
   *     encountered
   * @param rulesProvider
   *     a {@link RulesProvider} providing the rules to be evaluated; if {@code null} or empty, then
   *     nothing will be done
   *
   * @return a {@link PortfolioEvaluationInput} providing input to the next stage, or {@code null}
   *     if the portfolio is not ready
   */
  protected PortfolioEvaluationInput processPortfolio(Portfolio portfolio,
      SecurityKey currentSecurity, RulesProvider rulesProvider) {
    if (portfolio == null) {
      return null;
    }

    // if there are no rules to be evaluated, then don't bother
    if (rulesProvider == null || rulesProvider.getRules().count() == 0) {
      return null;
    }

    // update the unsatisfied securities
    if (currentSecurity != null && !isPortfolioComplete()) {
      unsatisfiedSecurities.remove(currentSecurity);
    }
    if (!isPortfolioComplete()) {
      // still have unsatisfied securities, so portfolio isn't ready
      return null;
    }

    // in a security encounter, if the security isn't held by the tracked portfolio, there is
    // nothing to do
    if (currentSecurity != null && !heldSecurities.contains(currentSecurity)) {
      return null;
    }

    // portfolio is ready for evaluation; proceed
    return new PortfolioEvaluationInput(evaluationSource, portfolio, rulesProvider);
  }
}
