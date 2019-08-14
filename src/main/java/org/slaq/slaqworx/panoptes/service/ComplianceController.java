package org.slaq.slaqworx.panoptes.service;

import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.cache.PortfolioCache;
import org.slaq.slaqworx.panoptes.evaluator.ClusterPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * ComplianceController is a simple "Web service" that evaluates all {@code Portfolio}s and outputs
 * quasi-formatted results.
 *
 * @author jeremy
 */
@Controller("/")
public class ComplianceController implements FlowableOnSubscribe<String> {
    private static final Logger LOG = LoggerFactory.getLogger(ComplianceController.class);

    // need enough threads to keep the processing pipeline full in a limited-CPU environment
    private static final ForkJoinPool evaluatorPool = new ForkJoinPool(50);

    private final PortfolioCache portfolioCache;

    private final ClusterPortfolioEvaluator portfolioEvaluator;

    /**
     * Creates a new {@code ComplianceController} using the given resources. Restricted because
     * instances of this class should be obtained through the {@code ApplicationContext}.
     *
     * @param portfolioCache
     *            the {@code PortfolioCache} to use to obtain cached resources
     * @param portfolioEvaluator
     *            the {@code ClusterPortfolioEvaluator} to use to evaluate {@code Portfolio}s
     */
    protected ComplianceController(PortfolioCache portfolioCache,
            ClusterPortfolioEvaluator portfolioEvaluator) {
        this.portfolioCache = portfolioCache;
        this.portfolioEvaluator = portfolioEvaluator;
    }

    /**
     * Runs a compliance evaluation and makes the results available as a {@code Flowable}.
     *
     * @return a {@code Flowable} of quasi-formatted results
     */
    @Get("/compliance")
    public Flowable<String> runCompliance() {
        return Flowable.create(this, BackpressureStrategy.BUFFER);
    }

    @Override
    public void subscribe(FlowableEmitter<String> emitter) throws Exception {
        FlowableEmitter<String> serialEmitter = emitter.serialize();

        long startTime = System.currentTimeMillis();
        evaluatorPool.submit(() -> portfolioCache.getPortfolioCache().values().parallelStream()
                .forEach(portfolio -> {
                    StringBuilder output = new StringBuilder();
                    Future<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> futureResults;
                    try {
                        futureResults =
                                portfolioEvaluator.evaluate(portfolio, new EvaluationContext(
                                        portfolioCache, portfolioCache, portfolioCache));
                    } catch (InterruptedException e) {
                        serialEmitter.onNext("thread interrupted");
                        return;
                    }
                    output.append("Portfolio " + portfolio.getName() + "\n");
                    try {
                        futureResults.get().forEach((ruleKey, resultMap) -> {
                            output.append("  * Rule " + ruleKey + "\n");
                            boolean[] isFirst = new boolean[1];
                            isFirst[0] = true;
                            resultMap.forEach((group, result) -> {
                                if (isFirst[0]) {
                                    output.append("    * Group " + group.getAggregationKey() + " = "
                                            + group.getId() + "\n");
                                    isFirst[0] = false;
                                    output.append("      -> result: " + result.isPassed() + " ("
                                            + result.getValue() + ")\n");
                                }
                            });
                        });
                    } catch (Exception e) {
                        LOG.error("could not complete portfolio evaluation", e);
                        serialEmitter.onNext("could not complete evaluation");
                    }

                    serialEmitter.onNext(output.toString());
                })).join();

        serialEmitter.onNext(
                "compliance run completed in " + (System.currentTimeMillis() - startTime) + " ms");
        serialEmitter.onComplete();
    }
}
