package org.slaq.slaqworx.panoptes.evaluator;

import java.io.Serializable;
import java.util.UUID;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

public class RuleEvaluationMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID evaluationBatchId;
    private final PortfolioKey portfolioKey;
    private final RuleKey ruleKey;

    public RuleEvaluationMessage(UUID evaluationBatchId, PortfolioKey portfolioKey,
            RuleKey ruleKey) {
        this.evaluationBatchId = evaluationBatchId;
        this.portfolioKey = portfolioKey;
        this.ruleKey = ruleKey;
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
        RuleEvaluationMessage other = (RuleEvaluationMessage)obj;
        if (evaluationBatchId == null) {
            if (other.evaluationBatchId != null) {
                return false;
            }
        } else if (!evaluationBatchId.equals(other.evaluationBatchId)) {
            return false;
        }
        if (portfolioKey == null) {
            if (other.portfolioKey != null) {
                return false;
            }
        } else if (!portfolioKey.equals(other.portfolioKey)) {
            return false;
        }
        if (ruleKey == null) {
            if (other.ruleKey != null) {
                return false;
            }
        } else if (!ruleKey.equals(other.ruleKey)) {
            return false;
        }
        return true;
    }

    /**
     * Obtains the evaluation batch ID to which this request belongs.
     *
     * @return the evaluation batch ID
     */
    public UUID getEvaluationBatchId() {
        return evaluationBatchId;
    }

    /**
     * Obtains the key of the {@code Portfolio} to be evaluated.
     *
     * @return a {@code PortfolioKey} identifying the evaluated {@code Portfolio}
     */
    public PortfolioKey getPortfolioKey() {
        return portfolioKey;
    }

    /**
     * Obtains the key of the {@code Rule} to be evaluated.
     *
     * @return a {@code RuleKey} identifying the evaluated {@code Rule}
     */
    public RuleKey getRuleKey() {
        return ruleKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((evaluationBatchId == null) ? 0 : evaluationBatchId.hashCode());
        result = prime * result + ((portfolioKey == null) ? 0 : portfolioKey.hashCode());
        result = prime * result + ((ruleKey == null) ? 0 : ruleKey.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "RuleEvaluationMessage[batchId= " + evaluationBatchId + ", portfolioKey="
                + portfolioKey + ", ruleKey=" + ruleKey + "]";
    }
}
