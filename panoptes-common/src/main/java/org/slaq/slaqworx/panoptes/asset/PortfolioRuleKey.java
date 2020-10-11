package org.slaq.slaqworx.panoptes.asset;

import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * {@code PortfolioRuleKey} is used as a key to specify or retrieve evaluation results by
 * {@code Portfolio} and {@code Rule}.
 */
public class PortfolioRuleKey implements ProtobufSerializable {
    private final PortfolioKey portfolioKey;
    private final RuleKey ruleKey;

    /**
     * Creates a new {@code PortfolioRuleKey} for the given {@code Portfolio} and {@code Rule} keys.
     *
     * @param portfolioKey
     *            the key of the referenced {@code Portfolio}
     * @param ruleKey
     *            the key of the referenced {@code Rule}
     */
    public PortfolioRuleKey(PortfolioKey portfolioKey, RuleKey ruleKey) {
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
        PortfolioRuleKey other = (PortfolioRuleKey)obj;
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
     * Obtains the key of the {@code Portfolio} referenced by this key.
     *
     * @return a {@code Portfolio} key
     */
    public PortfolioKey getPortfolioKey() {
        return portfolioKey;
    }

    /**
     * Obtains the key of the {@code Rule} referenced by this key.
     *
     * @return a {@code Rule} key
     */
    public RuleKey getRuleKey() {
        return ruleKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((portfolioKey == null) ? 0 : portfolioKey.hashCode());
        result = prime * result + ((ruleKey == null) ? 0 : ruleKey.hashCode());
        return result;
    }
}
