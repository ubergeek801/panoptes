package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Singleton;

import com.hazelcast.core.IMap;

import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * {@code PortfolioEvaluationRequestListener} consumes messages from the {@code Portfolio}
 * evaluation request queue, delegates to a {@code LocalPortfolioEvaluator} and publishes results to
 * the {@code Portfolio} evaluation result map.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioEvaluationRequestListener {
    private static final Logger LOG =
            LoggerFactory.getLogger(PortfolioEvaluationRequestListener.class);

    private final AssetCache assetCache;
    private final ClientConsumer portfolioEvaluationRequestConsumer;

    private final LinkedBlockingQueue<Pair<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>>> evaluationResultQueue =
            new LinkedBlockingQueue<>();
    private final IMap<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> evaluationResultMap;

    /**
     * Creates a new {@code PortfolioEvaluationRequestListener} which uses the given
     * {@code AssetCache} to resolve cache resources. The listener remains idle until
     * {@code start()} is invoked.
     *
     * @param assetCache
     *            the {@code AssetCache} to use
     * @param portfolioEvaluationRequestConsumer
     *            the {@code ClientConsumer} to use to consume messages
     */
    protected PortfolioEvaluationRequestListener(AssetCache assetCache,
            ClientConsumer portfolioEvaluationRequestConsumer) {
        this.assetCache = assetCache;
        this.portfolioEvaluationRequestConsumer = portfolioEvaluationRequestConsumer;
        evaluationResultMap = assetCache.getPortfolioEvaluationResultMap();
    }

    /**
     * Starts this listener.
     */
    public void start() {
        Thread requestProcessor = new Thread(() -> {
            // continuously consume messages from the request queue and process
            while (!Thread.interrupted()) {
                try {
                    ClientMessage message = portfolioEvaluationRequestConsumer.receive();
                    String stringMessage = message.getBodyBuffer().readString();
                    String[] components = stringMessage.split(":");
                    String requestId = components[0];
                    String portfolioId = components[1];
                    String portfolioVersion = components[2];
                    TradeKey tradeKey;
                    if (components.length == 4) {
                        tradeKey = new TradeKey(components[3]);
                    } else {
                        tradeKey = null;
                    }
                    PortfolioKey portfolioKey =
                            new PortfolioKey(portfolioId, Integer.valueOf(portfolioVersion));
                    Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> results =
                            evaluatePortfolio(portfolioKey, tradeKey);
                    evaluationResultQueue
                            .add(new ImmutablePair<>(UUID.fromString(requestId), results));
                } catch (Exception e) {
                    // TODO handle this in some reasonable way
                    LOG.error("could not process message", e);
                    try {
                        Thread.sleep(5000);
                        continue;
                    } catch (InterruptedException ex) {
                        // hang it up
                        return;
                    }
                }
            }
        }, "PortfolioEvaluationRequestProcessor");
        requestProcessor.setDaemon(true);
        requestProcessor.start();

        Thread resultProcessor = new Thread(() -> {
            // continuously take results from the local queue and publish to the result map
            while (!Thread.interrupted()) {
                try {
                    Pair<UUID, Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> result;
                    result = evaluationResultQueue.take();
                    evaluationResultMap.set(result.getLeft(), result.getRight());
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    // TODO handle this in some reasonable way
                    LOG.error("could not process message", e);
                    try {
                        Thread.sleep(5000);
                        continue;
                    } catch (InterruptedException ex) {
                        // hang it up
                        return;
                    }
                }
            }
        }, "PortfolioEvaluationResultProcessor");
        resultProcessor.setDaemon(true);
        resultProcessor.start();
    }

    /**
     * Evaluates the specified {@code Portfolio}, optionally including the specified {@code Trade}
     * in the evaluation.
     *
     * @param portfolioKey
     *            the key of the {@code Portfolio} to be evaluated
     * @param tradeKey
     *            the key of the {@code Trade} to be evaluated with the {@code Portfolio}
     * @return the {@code Portfolio} evaluation results
     */
    protected Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>
            evaluatePortfolio(PortfolioKey portfolioKey, TradeKey tradeKey) {
        try {
            EvaluationContext evaluationContext =
                    new EvaluationContext(assetCache, assetCache, assetCache);
            if (tradeKey != null) {
                Trade trade = assetCache.getTrade(tradeKey);
                Transaction transaction = trade.getTransaction(portfolioKey);

                return new LocalPortfolioEvaluator().evaluate(assetCache.getPortfolio(portfolioKey),
                        transaction, evaluationContext).get();
            }

            return new LocalPortfolioEvaluator()
                    .evaluate(assetCache.getPortfolio(portfolioKey), evaluationContext).get();
        } catch (Exception e) {
            // TODO throw a real exception
            throw new RuntimeException("could not process PortfolioEvaluationRequest", e);
        }
    }
}
