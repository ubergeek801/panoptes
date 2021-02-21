package org.slaq.slaqworx.panoptes.pipeline;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A utility for determining whether a tracked portfolio is ready for evaluation (that is, all of
 * its held securities have been encountered), and for performing the rule evaluations when ready.
 *
 * @author jeremy
 */
public class PortfolioTracker implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(PortfolioTracker.class);

    private static final ConcurrentHashMap<PortfolioKey, Boolean> portfolioCompleteState =
            new ConcurrentHashMap<>();

    private EvaluationSource evaluationSource;
    private Portfolio portfolio;

    /**
     * Creates a new {@code PortfolioTracker}.
     *
     * @param evaluationSource
     *            the type of portfolio (portfolio or benchmark) being tracked
     */
    protected PortfolioTracker(EvaluationSource evaluationSource) {
        this.evaluationSource = evaluationSource;
    }

    /**
     * Applies the given security to the tracked portfolio, evaluating related rules if appropriate.
     *
     * @param security
     *            the security currently being encountered
     * @param ruleProvider
     *            a {@code Function} which provides rules to be evaluated for a given
     *            {@code Portfolio}
     * @param results
     *            a {@code Collection} to which rule evaluation results, if any, are output
     */
    public void applySecurity(Security security, Function<Portfolio, Iterable<Rule>> ruleProvider,
            Collection<RuleEvaluationResult> results) {
        IMap<SecurityKey, Security> securityMap = PanoptesApp.getAssetCache().getSecurityCache();

        processPortfolio(results, portfolio, security, securityMap, ruleProvider.apply(portfolio));
    }

    /**
     * Obtains the portfolio being tracked in the current process state.
     *
     * @return a {@code Portfolio}
     */
    public Portfolio getPortfolio() {
        return portfolio;
    }

    /**
     * Registers the given portfolio for tracking in the current process state.
     *
     * @param portfolio
     *            the {@code Portfolio} to be tracked
     * @param heldSecuritiesMap
     *            a {@code MultiMap} mapping the key of a security to the keys of portfolios that
     *            hold it
     */
    public void trackPortfolio(Portfolio portfolio,
            MultiMap<SecurityKey, PortfolioKey> heldSecuritiesMap) {
        this.portfolio = portfolio;

        // FIXME possibly lock here
        portfolio.getPositions()
                .forEach(p -> heldSecuritiesMap.put(p.getSecurityKey(), portfolio.getKey()));
    }

    /**
     * Performs a portfolio evaluation and publishes the result.
     *
     * @param results
     *            the {@code Collection} to which to output compliance results
     * @param portfolio
     *            the portfolio being processed
     * @param securityMap
     *            an {@code IMap} containing known security information
     * @param rules
     *            the rules to be evaluated
     */
    protected void evaluatePortfolio(Collection<RuleEvaluationResult> results, Portfolio portfolio,
            IMap<SecurityKey, Security> securityMap, Iterable<Rule> rules) {
        // this is questionable but there shouldn't be any other portfolios queried
        PortfolioProvider portfolioProvider = (k -> portfolio);
        SecurityProvider securityProvider = (k, context) -> securityMap.get(k);

        LOG.info("processing rules for {} {} (\"{}\")", evaluationSource, portfolio.getKey(),
                portfolio.getName());
        int[] numRules = new int[1];
        rules.forEach(rule -> {
            // FIXME get/generate eventId
            long eventId = System.currentTimeMillis();

            EvaluationResult evaluationResult =
                    new org.slaq.slaqworx.panoptes.evaluator.RuleEvaluator(rule, portfolio,
                            new EvaluationContext(securityProvider, portfolioProvider)).call();
            // enrich the result with some other essential information
            RuleEvaluationResult ruleEvaluationResult = new RuleEvaluationResult(eventId,
                    portfolio.getKey(), portfolio.getBenchmarkKey(), evaluationSource,
                    rule.isBenchmarkSupported(), rule.getLowerLimit(), rule.getUpperLimit(),
                    evaluationResult);
            results.add(ruleEvaluationResult);
            numRules[0]++;
        });
        LOG.info("processed {} rules for {} {} (\"{}\")", numRules[0], evaluationSource,
                portfolio.getKey(), portfolio.getName());
    }

    /**
     * Determines whether the given portfolio is "complete" (all security information has been
     * provided) and performs a compliance evaluation if so.
     *
     * @param results
     *            the {@code Collection} to which to output compliance results
     * @param portfolio
     *            the portfolio being processed; if {@code null}, then nothing will be done
     * @param currentSecurity
     *            the security being encountered, or {@code null} if a portfolio is being
     *            encountered
     * @param securityMap
     *            an {@code IMap} containing known security information
     * @param rules
     *            the rules to be evaluated; if {@code null} or empty, then nothing will be done
     */
    protected void processPortfolio(Collection<RuleEvaluationResult> results, Portfolio portfolio,
            Security currentSecurity, IMap<SecurityKey, Security> securityMap,
            Iterable<Rule> rules) {
        if (portfolio == null) {
            return;
        }

        // if there are no rules to be evaluated, then don't bother
        if (!rules.iterator().hasNext()) {
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
                if (!securityMap.containsKey(position.getSecurityKey())) {
                    isComplete = false;
                    break;
                }
                if (currentSecurity != null
                        && position.getSecurityKey().equals(currentSecurity.getKey())) {
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
        evaluatePortfolio(results, portfolio, securityMap, rules);
    }
}
