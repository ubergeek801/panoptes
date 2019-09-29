package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;

import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.messaging.ClientProducerSessionFactory;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * {@code ClusterEvaluatorDispatcher} orchestrates a {@code Portfolio} evaluation on the cluster by
 * submitting a {@code Portfolio} evaluation request to the queue, listening for the corresponding
 * result in the {@code Portfolio} evaluation result map, and making the result available as a
 * {@code Future}.
 *
 * @author jeremy
 */
public class ClusterEvaluatorDispatcher implements
        EntryAddedListener<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> {
    private static final Logger LOG = LoggerFactory.getLogger(ClusterEvaluatorDispatcher.class);

    private final Object resultMonitor = new Object();
    private Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results;

    private final IMap<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> portfolioEvaluationResultMap;
    private final PortfolioKey portfolioKey;
    private final UUID requestId;
    private final String registrationId;
    private final long startTime;

    /**
     * Creates a new {@code ClusterEvaluatorDispatcher} which submits an evaluation request and
     * listens for the results.
     *
     * @param portfolioEvaluationResultMap
     *            the distributed {@IMap} from which to obtain result data
     * @param requestProducerSessionFactory
     *            a {@code ClientProducerSessionFactory} providing a {@code ClientSession} and
     *            {@code ClientProducer} corresponding to the {@code Portfolio} evaluation request
     *            queue
     * @param portfolioKey
     *            the key identifying the {@code Portfolio} to be evaluated
     * @param transaction
     *            the (possibly {@code null}) {@code Transaction} from which to apply allocations to
     *            the evaluated {@code Portfolio}
     * @param overrideRuleKeys
     *            a (possibly {@code null}) {@code Stream} of {@code RuleKeys} identifying a set of
     *            {@code Rules} to execute instead of the {@code Portfolio}'s own
     */
    public ClusterEvaluatorDispatcher(
            IMap<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> portfolioEvaluationResultMap,
            ClientProducerSessionFactory requestProducerSessionFactory, PortfolioKey portfolioKey,
            Transaction transaction, Stream<RuleKey> overrideRuleKeys) {
        this.portfolioEvaluationResultMap = portfolioEvaluationResultMap;
        this.portfolioKey = portfolioKey;
        requestId = UUID.randomUUID();
        registrationId = portfolioEvaluationResultMap.addEntryListener(this, requestId, true);
        // publish a message to the evaluation queue
        ClientMessage message =
                requestProducerSessionFactory.getSession().createMessage(Message.TEXT_TYPE, false);
        StringBuilder messageBody =
                new StringBuilder(requestId.toString() + ":" + portfolioKey.toString() + ":");
        if (transaction != null) {
            messageBody.append(transaction.getTrade().getKey());
        }
        if (overrideRuleKeys != null) {
            String ruleKeyStrings = String.join(",",
                    overrideRuleKeys.map(k -> k.getId()).collect(Collectors.toSet()));
            messageBody.append(":" + ruleKeyStrings);
        }
        message.getBodyBuffer().writeString(messageBody.toString());
        LOG.info("delegating request to evaluate Portfolio {}", portfolioKey);
        startTime = System.currentTimeMillis();
        try {
            requestProducerSessionFactory.getProducer().send(message);
        } catch (Exception e) {
            // TODO throw a real exception
            throw new RuntimeException("could not send message", e);
        }
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
