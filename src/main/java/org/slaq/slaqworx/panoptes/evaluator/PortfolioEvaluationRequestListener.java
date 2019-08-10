package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.UUID;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
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
 * results to the {@code Portfolio} evaluation result map. A local intermediary queue is used to
 * decouple the Hazelcast event processing threads from the {@code PortfolioEvaluator} threads, in
 * an attempt to keep the processing pipeline filled.
 *
 * @author jeremy
 */
public class PortfolioEvaluationRequestListener
        implements EntryAddedListener<UUID, PortfolioEvaluationRequest> {
    private final PortfolioCache portfolioCache;
    private final IMap<UUID, PortfolioEvaluationRequest> evaluationRequestMap;
    private final IQueue<PortfolioEvaluationRequest> evaluationRequestQueue;
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
        evaluationRequestQueue = portfolioCache.getPortfolioEvaluationQueue();
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
            // put the results on the result map
            while (!Thread.interrupted()) {
                PortfolioEvaluationRequest message;
                try {
                    message = evaluationRequestQueue.take();
                } catch (InterruptedException e) {
                    // shut it down
                    return;
                }
                Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results =
                        evaluatePortfolio(message.getPortfolioKey());
                evaluationResultMap.setAsync(message.getRequestId(), results);
            }
        }, "PortfolioEvaluationRequestProcessor");
        requestProcessor.setDaemon(true);
        requestProcessor.start();
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
