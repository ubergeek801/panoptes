package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import com.hazelcast.core.IMap;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.data.PortfolioCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code PortfolioEvaluationRequestListener} is an {@code EntryAddedListener} that listens for new
 * local entries in the {@code Portfolio} evaluation request map, consumes them and publishes
 * results to the {@code Portfolio} evaluation result map.
 *
 * @author jeremy
 */
@Service
public class PortfolioEvaluationRequestListener {
    private final PortfolioCache portfolioCache;

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
    protected PortfolioEvaluationRequestListener(PortfolioCache portfolioCache) {
        this.portfolioCache = portfolioCache;
        evaluationResultMap = portfolioCache.getPortfolioEvaluationResultMap();

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

    @JmsListener(destination = "portfolioEvaluationRequestQueue", concurrency = "1")
    public void receiveMessage(final Message message) throws JMSException {
        if (message instanceof ObjectMessage) {
            ObjectMessage textMessage = (ObjectMessage)message;
            PortfolioEvaluationRequest request =
                    (PortfolioEvaluationRequest)textMessage.getObject();

            Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results =
                    evaluatePortfolio(request.getPortfolioKey());
            evaluationResultQueue.add(new ImmutablePair<>(request.getRequestId(), results));
        }
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
