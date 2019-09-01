package org.slaq.slaqworx.panoptes.service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ForkJoinPool;

import com.hazelcast.query.Predicates;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.ClusterPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluator;

/**
 * ComplianceController is a simple "Web service" that evaluates all {@code Portfolio}s and outputs
 * quasi-formatted results.
 *
 * @author jeremy
 */
@Controller("/")
public class ComplianceController implements FlowableOnSubscribe<String> {
    private static final Logger LOG = LoggerFactory.getLogger(ComplianceController.class);

    // this needs to be somewhat unreasonably high to keep the processing pipeline full
    private static final ForkJoinPool evaluatorPool = new ForkJoinPool(30);

    private final AssetCache assetCache;
    private final ClusterPortfolioEvaluator portfolioEvaluator;

    /**
     * Creates a new {@code ComplianceController} using the given resources. Restricted because
     * instances of this class should be obtained through the {@code ApplicationContext}.
     *
     * @param assetCache
     *            the {@code AssetCache} to use to obtain cached resources
     * @param portfolioEvaluator
     *            the {@code ClusterPortfolioEvaluator} to use to evaluate {@code Portfolio}s
     */
    protected ComplianceController(AssetCache assetCache,
            ClusterPortfolioEvaluator portfolioEvaluator) {
        this.assetCache = assetCache;
        this.portfolioEvaluator = portfolioEvaluator;
    }

    @Get("/room/{portfolioId}")
    public String getRoom(String portfolioId, String assetId, double targetAmount)
            throws InterruptedException, ExecutionException {
        TradeEvaluator tradeEvaluator = new TradeEvaluator(assetCache, assetCache, assetCache);

        // FIXME don't assume that these exist
        Portfolio portfolio = assetCache.getPortfolio(new PortfolioKey(portfolioId, 1));
        Collection<Security> securities =
                assetCache.getSecurityCache().values(Predicates.equal("assetId", assetId));
        Security security = securities.iterator().next();

        double room = tradeEvaluator.evaluateRoom(portfolio, security, targetAmount);
        return String.valueOf(room);
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

        ExecutorCompletionService<Pair<Portfolio, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>>> completionService =
                new ExecutorCompletionService<>(evaluatorPool);

        long startTime = System.currentTimeMillis();
        Collection<Portfolio> portfolios = assetCache.getPortfolioCache().values();
        portfolios.forEach(p -> {
            completionService.submit(() -> new ImmutablePair<>(p, portfolioEvaluator
                    .evaluate(p, new EvaluationContext(assetCache, assetCache, assetCache)).get()));
        });

        for (int i = 0; i < portfolios.size(); i++) {
            Pair<Portfolio, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> portfolioResults =
                    completionService.take().get();
            Portfolio portfolio = portfolioResults.getLeft();
            Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results =
                    portfolioResults.getRight();
            StringBuilder output = new StringBuilder(
                    "Portfolio " + portfolio.getName() + " (" + portfolio.getKey().getId() + ")\n");
            results.forEach((ruleKey, resultMap) -> {
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
            serialEmitter.onNext(output.toString());
        }

        String message =
                "compliance run completed in " + (System.currentTimeMillis() - startTime) + " ms";
        serialEmitter.onNext(message);
        LOG.info(message);
        serialEmitter.onComplete();
    }
}
