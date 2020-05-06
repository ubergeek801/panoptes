package org.slaq.slaqworx.panoptes.service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.ClusterPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.util.SerializerUtil;

/**
 * A Web service for testing Panoptes.
 *
 * @author jeremy
 */
@Controller("/compliance")
public class ComplianceService {
    static class ResultState {
        int numEvaluations;
        int resultIndex;

        ResultState() {
            resultIndex = 0;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ComplianceService.class);

    private final ClusterPortfolioEvaluator evaluator;
    private final AssetCache assetCache;

    protected ComplianceService(ClusterPortfolioEvaluator clusterPortfolioEvaluator,
            AssetCache assetCache) {
        evaluator = clusterPortfolioEvaluator;
        this.assetCache = assetCache;
    }

    @Get(uri = "/all", produces = MediaType.APPLICATION_JSON_STREAM)
    public Flowable<String> evaluateCompliance() {
        Collection<PortfolioKey> portfolioKeys = assetCache.getPortfolioCache().keySet();
        int numPortfolios = portfolioKeys.size();
        ResultState state = new ResultState();
        long startTime = System.currentTimeMillis();

        LinkedBlockingQueue<
                Triple<PortfolioKey, Map<RuleKey, EvaluationResult>, Throwable>> resultQueue =
                        new LinkedBlockingQueue<>();

        Flowable<String> response = Flowable.create(emitter -> {
            while (!emitter.isCancelled()) {
                if (state.resultIndex++ == numPortfolios) {
                    long endTime = System.currentTimeMillis();
                    String message = "processed " + numPortfolios + " Portfolios using "
                            + state.numEvaluations + " Rule evaluations in " + (endTime - startTime)
                            + " ms";
                    LOG.info(message);
                    emitter.onNext(message);
                    emitter.onComplete();
                    return;
                }

                // FIXME this blocks
                Triple<PortfolioKey, Map<RuleKey, EvaluationResult>, Throwable> evaluationResult =
                        resultQueue.take();
                Map<RuleKey, EvaluationResult> ruleResults = evaluationResult.getMiddle();
                if (ruleResults != null) {
                    state.numEvaluations += ruleResults.size();
                }
                emitter.onNext(
                        SerializerUtil.defaultJsonMapper().writeValueAsString(evaluationResult));
            }
        }, BackpressureStrategy.BUFFER);

        portfolioKeys.forEach(key -> {
            CompletableFuture<Map<RuleKey, EvaluationResult>> futureResult =
                    evaluator.evaluate(key, new EvaluationContext(assetCache, assetCache));
            futureResult.handleAsync((result, exception) -> {
                try {
                    resultQueue.put(Triple.of(key, result, exception));
                } catch (InterruptedException e) {
                    // the queue is unbounded so this won't happen
                }
                return null;
            });
        });

        return response;
    }
}
