package org.slaq.slaqworx.panoptes.evaluator;

import java.util.Map;
import java.util.concurrent.Callable;

import io.micronaut.context.ApplicationContext;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.util.ApplicationContextAware;

/**
 * {@code PortfolioEvaluationRequest} is a {@code Callable} which facilitates clustered
 * {@code Portfolio} evaluation by serializing the evaluation parameters for execution on a remote
 * cluster node.
 *
 * @author jeremy
 */
public class PortfolioEvaluationRequest
        implements Callable<Map<RuleKey, EvaluationResult>>, ApplicationContextAware {
    private final PortfolioKey portfolioKey;
    private final Transaction transaction;
    private final EvaluationContext evaluationContext;

    private ApplicationContext applicationContext;

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
    public PortfolioEvaluationRequest(Portfolio portfolio, Transaction transaction,
            EvaluationContext evaluationContext) {
        portfolioKey = portfolio.getKey();
        this.transaction = transaction;
        this.evaluationContext = evaluationContext;
    }

    @Override
    public Map<RuleKey, EvaluationResult> call() throws Exception {
        AssetCache assetCache = applicationContext.getBean(AssetCache.class);

        Portfolio portfolio = assetCache.getPortfolio(portfolioKey);

        return new LocalPortfolioEvaluator(assetCache)
                .evaluate(portfolio, transaction, evaluationContext).get();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PortfolioEvaluationRequest other = (PortfolioEvaluationRequest)obj;
        if (evaluationContext == null) {
            if (other.evaluationContext != null) {
                return false;
            }
        } else if (!evaluationContext.equals(other.evaluationContext)) {
            return false;
        }
        if (portfolioKey == null) {
            if (other.portfolioKey != null) {
                return false;
            }
        } else if (!portfolioKey.equals(other.portfolioKey)) {
            return false;
        }
        if (transaction == null) {
            if (other.transaction != null) {
                return false;
            }
        } else if (!transaction.equals(other.transaction)) {
            return false;
        }
        return true;
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    public PortfolioKey getPortfolioKey() {
        return portfolioKey;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((evaluationContext == null) ? 0 : evaluationContext.hashCode());
        result = prime * result + ((portfolioKey == null) ? 0 : portfolioKey.hashCode());
        result = prime * result + ((transaction == null) ? 0 : transaction.hashCode());
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
