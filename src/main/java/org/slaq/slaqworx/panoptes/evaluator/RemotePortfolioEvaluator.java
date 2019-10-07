package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;

import io.micronaut.context.BeanContext;

import org.apache.ignite.lang.IgniteCallable;

import org.slaq.slaqworx.panoptes.ApplicationContextProvider;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * {@code RemotePortfolioEvaluator} is an {@code IgniteCallable} which facilitates clustered
 * {@code Portfolio} evaluation by serializing the evaluation parameters for execution on a remote
 * cluster node.
 *
 * @author jeremy
 */
public class RemotePortfolioEvaluator implements IgniteCallable<Map<RuleKey, EvaluationResult>> {
    private static final long serialVersionUID = 1L;

    private final PortfolioKey portfolioKey;
    private final TradeKey tradeKey;
    private final EvaluationContext evaluationContext;

    /**
     * Creates a new {@code RemotePortfolioEvaluator} with the given parameters.
     *
     * @param portfolio
     *            the {@code Portfolio} to be evaluated
     * @param transaction
     *            the (possibly {@code null} {@code Transaction} to be evaluated with the
     *            {@code Portfolio}
     * @param evaluationContext
     *            the {@code EvaluationContext} under which to evaluate
     */
    public RemotePortfolioEvaluator(Portfolio portfolio, Transaction transaction,
            EvaluationContext evaluationContext) {
        portfolioKey = portfolio.getKey();
        tradeKey = (transaction == null ? null : transaction.getTrade().getKey());
        this.evaluationContext = evaluationContext;
    }

    @Override
    public Map<RuleKey, EvaluationResult> call() throws Exception {
        BeanContext context = ApplicationContextProvider.getApplicationContext();
        AssetCache assetCache = context.getBean(AssetCache.class);

        Portfolio portfolio = assetCache.getPortfolio(portfolioKey);
        Trade trade = (tradeKey == null ? null : assetCache.getTrade(tradeKey));
        Transaction transaction = (trade == null ? null : trade.getTransaction(portfolioKey));

        return new LocalPortfolioEvaluator(assetCache)
                .evaluate(portfolio, transaction, evaluationContext).get();
    }
}
