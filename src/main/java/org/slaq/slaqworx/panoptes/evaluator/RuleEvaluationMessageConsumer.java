package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.data.PortfolioCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * {@code RuleEvaluationMessageConsumer} is a {@code Runnable} that continuously {@code take()}s
 * messages from the {@code Rule} evaluation queue and publishes results to the {@code Rule}
 * evaluation result map.
 *
 * @author jeremy
 */
public class RuleEvaluationMessageConsumer implements Runnable {
    private final PortfolioCache portfolioCache;

    /**
     * Creates a new {@code RuleEvaluationMessageConsumer} which uses the given
     * {@code PortfolioCache} to resolve cache resources.
     *
     * @param portfolioCache
     *            the {@code PortfolioCache} to use
     */
    public RuleEvaluationMessageConsumer(PortfolioCache portfolioCache) {
        this.portfolioCache = portfolioCache;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            RuleEvaluationMessage message;
            try {
                message = portfolioCache.getRuleEvaluationQueue().take();
            } catch (InterruptedException e) {
                // time to pack it in
                break;
            }

            Map<EvaluationGroup<?>, EvaluationResult> results =
                    processMessage(portfolioCache, message);
            portfolioCache.getRuleEvaluationResultMap().put(message.getEvaluationBatchId(),
                    new ImmutablePair<>(message.getRuleKey(), results));
        }
    }

    /**
     * Processes the given message.
     *
     * @param portfolioCache
     *            the {@code PortfolioCache} to use
     * @param message
     *            the {code RuleEvaluationMessage} to process
     * @return a {@code Map} of evaluation group to its result
     */
    protected Map<EvaluationGroup<?>, EvaluationResult>
            processMessage(PortfolioCache portfolioCache, RuleEvaluationMessage message) {
        Rule rule = portfolioCache.getRule(message.getRuleKey());
        Portfolio portfolio = portfolioCache.getPortfolio(message.getPortfolioKey());

        return new RuleEvaluator(rule, portfolio, portfolio.getBenchmark(portfolioCache),
                new EvaluationContext(portfolioCache, portfolioCache, portfolioCache)).call();
    }
}
