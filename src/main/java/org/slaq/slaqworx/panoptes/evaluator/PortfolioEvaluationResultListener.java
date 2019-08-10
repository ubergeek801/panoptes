package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.UUID;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * {@code PortfolioEvaluationResultListener} is an {@code EntryAddedListener} that listens for new
 * entries in the {@code Portfolio} evaluation result map, collects them and makes them available to
 * a caller.
 *
 * @author jeremy
 */
public class PortfolioEvaluationResultListener implements
        EntryAddedListener<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> {
    private final Object resultMonitor = new Object();
    private Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results;

    private final UUID requestId;
    private final PortfolioKey portfolioKey;

    /**
     * Creates a new {@code PortfolioEvaluationResultListener} which listens for results for the
     * given request ID.
     *
     * @param requestId
     *            the request ID for which to listen for results
     * @param portfolioKey
     *            the {@code Portfolio} key for which the results are produced
     */
    public PortfolioEvaluationResultListener(UUID requestId, PortfolioKey portfolioKey) {
        this.requestId = requestId;
        this.portfolioKey = portfolioKey;
    }

    @Override
    public void entryAdded(
            EntryEvent<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> event) {
        synchronized (resultMonitor) {
            results = event.getValue();
            resultMonitor.notifyAll();
        }
    }

    /**
     * Obtains the {@code Portfolio} key for which results are listened.
     *
     * @return the {@code PortfolioKey} associated with this listener
     */
    public PortfolioKey getPortfolioKey() {
        return portfolioKey;
    }

    /**
     * Obtains the request ID listened for by this {@code PortfolioEvaluationResultListener}.
     *
     * @return the request ID
     */
    public UUID getRequestId() {
        return requestId;
    }

    /**
     * Obtains the results once they are available.
     * <p>
     * TODO make {@code getResults()} return a {@code Future}
     *
     * @return the evaluation results if available, or {@code null} otherwise
     */
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> getResults() {
        return results;
    }

    /**
     * Joins on this listener, blocking until the results are available.
     *
     * @throws InterruptedException
     *             if the {@code Thread} is interrupted while awaiting results
     */
    public void join() throws InterruptedException {
        while (true) {
            synchronized (resultMonitor) {
                if (results != null) {
                    return;
                }

                resultMonitor.wait(1000);
            }
        }
    }
}
