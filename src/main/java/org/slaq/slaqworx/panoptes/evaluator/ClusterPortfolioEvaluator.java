package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.data.PortfolioCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code ClusterPortfolioEvaluator} is a {@code PortfolioEvaluator} which delegates processing to
 * the cluster.
 *
 * @author jeremy
 */
public class ClusterPortfolioEvaluator implements PortfolioEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(ClusterPortfolioEvaluator.class);

    private final PortfolioCache portfolioCache;

    /**
     * Creates a new {@code ClusterPortfolioEvaluator} using the given {@code PortfolioCache} for
     * distributed {@code Portfolio} evaluation.
     *
     * @param portfolioCache
     *            the {@code PortfolioCache} to use to obtain distributed resources
     */
    public ClusterPortfolioEvaluator(PortfolioCache portfolioCache) {
        this.portfolioCache = portfolioCache;
    }

    @Override
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> evaluate(Portfolio portfolio,
            EvaluationContext evaluationContext) throws InterruptedException {
        // TODO try not to duplicate processing prologue/epilogue
        long numRules = portfolio.getRules().count();
        if (numRules == 0) {
            LOG.warn("not evaluating Portfolio {} with no Rules", portfolio.getName());
            return Collections.emptyMap();
        }

        long startTime = System.currentTimeMillis();
        LOG.info("delegating request to evaluate Portfolio {}", portfolio.getName(), numRules);
        UUID requestId = UUID.randomUUID();
        PortfolioEvaluationResultListener resultListener =
                new PortfolioEvaluationResultListener(requestId, portfolio.getKey());
        String listenerRegistration = portfolioCache.getPortfolioEvaluationResultMap()
                .addEntryListener(resultListener, requestId, true);
        try {
            // publish a message to the evaluation queue
            PortfolioEvaluationRequest request =
                    new PortfolioEvaluationRequest(requestId, portfolio.getKey());
            portfolioCache.getPortfolioEvaluationRequestMap().setAsync(requestId, request);

            // wait for the result
            resultListener.join();

            return resultListener.getResults();
        } finally {
            // clean up the listener and result map
            portfolioCache.getPortfolioEvaluationResultMap()
                    .removeEntryListener(listenerRegistration);
            portfolioCache.getPortfolioEvaluationResultMap().delete(resultListener.getRequestId());

            LOG.info("received evaluation for Portfolio {} in {} ms", portfolio.getName(),
                    System.currentTimeMillis() - startTime);
        }
    }
}
