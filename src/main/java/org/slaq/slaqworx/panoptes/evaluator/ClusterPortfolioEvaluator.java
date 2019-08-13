package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import javax.inject.Singleton;

import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.commons.lang3.tuple.Pair;
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
@Singleton
public class ClusterPortfolioEvaluator implements PortfolioEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(ClusterPortfolioEvaluator.class);

    private final PortfolioCache portfolioCache;
    private final ClientSession portfolioEvaluationRequestQueueSession;
    private final ClientProducer portfolioEvaluationRequestQueueProducer;

    /**
     * Creates a new {@code ClusterPortfolioEvaluator} using the given {@code PortfolioCache} for
     * distributed {@code Portfolio} evaluation.
     *
     * @param portfolioCache
     *            the {@code PortfolioCache} to use to obtain distributed resources
     */
    protected ClusterPortfolioEvaluator(PortfolioCache portfolioCache,
            Pair<ClientSession, ClientProducer> portfolioEvaluationRequestQueueProducer) {
        this.portfolioCache = portfolioCache;
        portfolioEvaluationRequestQueueSession = portfolioEvaluationRequestQueueProducer.getLeft();
        this.portfolioEvaluationRequestQueueProducer =
                portfolioEvaluationRequestQueueProducer.getRight();
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
            ClientMessage message =
                    portfolioEvaluationRequestQueueSession.createMessage(Message.TEXT_TYPE, false);
            message.getBodyBuffer()
                    .writeString(requestId.toString() + ":" + portfolio.getKey().toString());
            try {
                portfolioEvaluationRequestQueueProducer.send(message);
            } catch (Exception e) {
                // TODO throw a real exception
                throw new RuntimeException("could not send message", e);
            }

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
