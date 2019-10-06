package org.slaq.slaqworx.panoptes.evaluator;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.micronaut.context.BeanContext;

import org.apache.ignite.lang.IgniteCallable;

import org.slaq.slaqworx.panoptes.ApplicationContextProvider;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluator.TradeEvaluationMode;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;

public class RemotePortfolioEvaluator
        implements IgniteCallable<Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>>> {
    private static final long serialVersionUID = 1L;

    private final ArrayList<RuleKey> ruleKeys;
    private final PortfolioKey portfolioKey;
    private final TradeKey tradeKey;
    private final TradeEvaluationMode evaluationMode;

    public RemotePortfolioEvaluator(Stream<Rule> rules, Portfolio portfolio,
            Transaction transaction, EvaluationContext evaluationContext) {
        ruleKeys = (rules == null ? null
                : rules.map(r -> r.getKey()).collect(Collectors.toCollection(ArrayList::new)));
        portfolioKey = portfolio.getKey();
        tradeKey = (transaction == null ? null : transaction.getTrade().getKey());
        evaluationMode = evaluationContext.getEvaluationMode();
    }

    @Override
    public Map<RuleKey, Map<EvaluationGroup<?>, EvaluationResult>> call() throws Exception {
        BeanContext context = ApplicationContextProvider.getApplicationContext();
        AssetCache assetCache = context.getBean(AssetCache.class);

        Portfolio portfolio = assetCache.getPortfolio(portfolioKey);
        Stream<Rule> rules = (ruleKeys == null ? portfolio.getRules()
                : ruleKeys.stream().map(k -> assetCache.getRule(k)));
        Trade trade = (tradeKey == null ? null : assetCache.getTrade(tradeKey));
        Transaction transaction = (trade == null ? null : trade.getTransaction(portfolioKey));
        Portfolio benchmark = portfolio.getBenchmark(assetCache);

        return new LocalPortfolioEvaluator().evaluate(rules, portfolio, transaction, benchmark,
                new EvaluationContext(assetCache, assetCache, assetCache, evaluationMode));
    }
}
