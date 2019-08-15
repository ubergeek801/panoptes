package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;

import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code ClusterPortfolioEvaluatorMessenger} orchestrates a {@code Portfolio} evaluation on the
 * cluster by submitting a {@code Portfolio} evaluation request to the queue, listening for the
 * corresponding result in the {@code Portfolio} evaluation result map, and making the result
 * available as a {@code Future}.
 *
 * @author jeremy
 */
public class ClusterPortfolioEvaluatorMessenger implements
        EntryAddedListener<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> {
    private static final Logger LOG =
            LoggerFactory.getLogger(ClusterPortfolioEvaluatorMessenger.class);

    private final Object resultMonitor = new Object();
    private Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results;

    private final IMap<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> portfolioEvaluationResultMap;
    private final PortfolioKey portfolioKey;
    private final UUID requestId;
    private final String registrationId;
    private final long startTime;

    /**
     * Creates a new {@code ClusterPortfolioEvaluatorMessenger} which submits an evaluation request
     * and listens for the results.
     *
     * @param portfolioCache
     *            the {@PortfolioCache} from which to obtain the result map
     * @param portfolioKey
     *            the key identifying the {@code Portfolio} to be evaluated
     */
    public ClusterPortfolioEvaluatorMessenger(
            IMap<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> portfolioEvaluationResultMap,
            ClientSession portfolioEvaluationRequestQueueSession,
            ClientProducer portfolioEvaluationRequestQueueProducer, PortfolioKey portfolioKey) {
        this.portfolioEvaluationResultMap = portfolioEvaluationResultMap;
        this.portfolioKey = portfolioKey;
        requestId = UUID.randomUUID();
        // publish a message to the evaluation queue
        ClientMessage message =
                portfolioEvaluationRequestQueueSession.createMessage(Message.TEXT_TYPE, false);
        message.getBodyBuffer().writeString(requestId.toString() + ":" + portfolioKey.toString());
        LOG.info("delegating request to evaluate Portfolio {}", portfolioKey);
        startTime = System.currentTimeMillis();
        try {
            portfolioEvaluationRequestQueueProducer.send(message);
        } catch (Exception e) {
            // TODO throw a real exception
            throw new RuntimeException("could not send message", e);
        }

        registrationId = portfolioEvaluationResultMap.addEntryListener(this, requestId, true);
    }

    @Override
    public void entryAdded(
            EntryEvent<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> event) {
        LOG.info("received Portfolio {} response in {} ms", portfolioKey,
                System.currentTimeMillis() - startTime);

        // clean up the listener registration and result map
        portfolioEvaluationResultMap.removeEntryListener(registrationId);
        portfolioEvaluationResultMap.delete(requestId);

        synchronized (resultMonitor) {
            results = event.getValue();
            resultMonitor.notifyAll();
        }
    }

    /**
     * Obtains a {@code Future} value of the results.
     *
     * @return the results as a {@code Future}
     */
    public Future<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> getResults() {
        return CompletableFuture.supplyAsync(() -> {
            while (true) {
                synchronized (resultMonitor) {
                    if (results != null) {
                        return results;
                    }

                    try {
                        resultMonitor.wait(500);
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
            }
        });
    }
}