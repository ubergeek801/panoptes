package org.slaq.slaqworx.panoptes.evaluator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

public class RuleEvaluationResultListener
        implements EntryListener<UUID, Pair<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> {
    private final HashMap<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> allResults =
            new HashMap<>(400);

    protected RuleEvaluationResultListener() {
        // nothing to do
    }

    @Override
    public void entryAdded(
            EntryEvent<UUID, Pair<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> event) {
        addResults(event.getValue());
    }

    @Override
    public void entryEvicted(
            EntryEvent<UUID, Pair<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> event) {
        // not interested
    }

    @Override
    public void entryRemoved(
            EntryEvent<UUID, Pair<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> event) {
        // not interested
    }

    @Override
    public void entryUpdated(
            EntryEvent<UUID, Pair<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> event) {
        addResults(event.getValue());
    }

    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> getResults() {
        return allResults;
    }

    /**
     * Join on this listener, blocking until the expected number of results are available.
     *
     * @param numExpectedResults
     *            the number of expected results to wait for
     * @throws {@code
     *             InterruptedException} if the {@code Thread} is interrupted while awaiting results
     */
    public void join(int numExpectedResults) throws InterruptedException {
        while (true) {
            synchronized (allResults) {
                if (allResults.size() >= numExpectedResults) {
                    return;
                }

                allResults.wait(1000);
            }
        }
    }

    @Override
    public void mapCleared(MapEvent event) {
        // not interested
    }

    @Override
    public void mapEvicted(MapEvent event) {
        // not interested
    }

    protected void addResults(Pair<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results) {
        synchronized (allResults) {
            allResults.put(results.getLeft(), results.getRight());
            allResults.notifyAll();
        }
    }
}
