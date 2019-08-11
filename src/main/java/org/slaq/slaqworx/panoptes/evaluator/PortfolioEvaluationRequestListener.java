package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.data.PortfolioCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code PortfolioEvaluationRequestListener} is an {@code EntryAddedListener} that listens for new
 * local entries in the {@code Portfolio} evaluation request map, consumes them and publishes
 * results to the {@code Portfolio} evaluation result map. Local intermediary queues are used to
 * decouple the Hazelcast event processing threads from the {@code PortfolioEvaluator} threads, in
 * an attempt to keep the processing pipeline filled.
 *
 * @author jeremy
 */
public class PortfolioEvaluationRequestListener
        implements EntryAddedListener<UUID, PortfolioEvaluationRequest> {
    private static final Logger LOG =
            LoggerFactory.getLogger(PortfolioEvaluationRequestListener.class);

    private final PortfolioCache portfolioCache;
    private final IMap<UUID, PortfolioEvaluationRequest> evaluationRequestMap;
    private final LinkedBlockingQueue<PortfolioEvaluationRequest> evaluationRequestQueue =
            new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Pair<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>>> evaluationResultQueue =
            new LinkedBlockingQueue<>();
    private final IMap<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> evaluationResultMap;

    /**
     * Creates a new {@code RuleEvaluationRequestListener} which uses the given
     * {@code PortfolioCache} to resolve cache resources.
     *
     * @param portfolioCache
     *            the {@code PortfolioCache} to use
     */
    public PortfolioEvaluationRequestListener(PortfolioCache portfolioCache) {
        this.portfolioCache = portfolioCache;
        evaluationRequestMap = portfolioCache.getPortfolioEvaluationRequestMap();
        evaluationResultMap = portfolioCache.getPortfolioEvaluationResultMap();
        evaluationRequestMap.addLocalEntryListener(this);
    }

    @Override
    public void entryAdded(EntryEvent<UUID, PortfolioEvaluationRequest> event) {
        UUID messageId = event.getKey();
        PortfolioEvaluationRequest message = event.getValue();

        // add the message to the local queue and remove it from the request map
        evaluationRequestQueue.add(message);
        evaluationRequestMap.removeAsync(messageId);
    }

    /**
     * Starts this {@code PortfolioEvaluationRequestListener}'s queue-processing thread.
     */
    public void start() {
        Thread requestProcessor = new Thread(() -> {
            // continuously take a request from the local queue, call the Portfolio evaluator, and
            // put the results on the result queue
            boolean isRemoteMessage = false;
            while (!Thread.interrupted()) {
                PortfolioEvaluationRequest message;
                try {
                    message = evaluationRequestQueue.poll(isRemoteMessage ? 10 : 1000,
                            TimeUnit.MILLISECONDS);
                    if (message == null) {
                        try {
                            UUID messageKey = evaluationRequestMap.keySet().iterator().next();
                            message = evaluationRequestMap.remove(messageKey);
                            if (message == null) {
                                isRemoteMessage = false;
                                continue;
                            }
                            LOG.info("no local message available; got remote message {}",
                                    message.getRequestId());
                            isRemoteMessage = true;
                        } catch (NoSuchElementException e) {
                            isRemoteMessage = false;
                            continue;
                        }
                    }
                } catch (InterruptedException e) {
                    // shut it down
                    return;
                }
                Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results =
                        evaluatePortfolio(message.getPortfolioKey());
                evaluationResultQueue.add(new ImmutablePair<>(message.getRequestId(), results));
            }
        }, "PortfolioEvaluationRequestProcessor");
        requestProcessor.setDaemon(true);
        requestProcessor.start();

        Thread resultProcessor = new Thread(() -> {
            // continuously take a results from the local queue and publish to the result map
            while (!Thread.interrupted()) {
                Pair<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> result;
                try {
                    result = evaluationResultQueue.take();
                } catch (InterruptedException e) {
                    // shut it down
                    return;
                }
                evaluationResultMap.set(result.getLeft(), result.getRight());
            }
        }, "PortfolioEvaluationResultProcessor");
        resultProcessor.setDaemon(true);
        resultProcessor.start();
    }

    /**
     * Evaluates the specified {@code Portfolio}.
     *
     * @param portfolioKey
     *            the key of the {@code Portfolio} to be evaluated
     * @return the {@code Portfolio} evaluation results
     */
    protected Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>
            evaluatePortfolio(PortfolioKey portfolioKey) {
        try {
            return new LocalPortfolioEvaluator().evaluate(portfolioCache.getPortfolio(portfolioKey),
                    new EvaluationContext(portfolioCache, portfolioCache, portfolioCache));
        } catch (Exception e) {
            // TODO throw a real exception
            throw new RuntimeException("could not process PortfolioEvaluationRequest", e);
        }
    }
}
