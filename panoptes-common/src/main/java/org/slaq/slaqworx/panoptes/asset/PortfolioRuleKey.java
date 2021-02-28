package org.slaq.slaqworx.panoptes.asset;

import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A key to specify or retrieve evaluation results by {@link Portfolio} and {@link Rule}.
 */
public class PortfolioRuleKey implements ProtobufSerializable {
  private final PortfolioKey portfolioKey;
  private final RuleKey ruleKey;

  /**
   * Creates a new {@link PortfolioRuleKey} for the given {@link Portfolio} and {@link Rule} keys.
   *
   * @param portfolioKey
   *     the key of the referenced {@link Portfolio}
   * @param ruleKey
   *     the key of the referenced {@link Rule}
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
    PortfolioRuleKey other = (PortfolioRuleKey) obj;
    if (portfolioKey == null) {
      if (other.portfolioKey != null) {
        return false;
      }
    } else if (!portfolioKey.equals(other.portfolioKey)) {
      return false;
    }
    if (ruleKey == null) {
      return other.ruleKey == null;
    } else {
      return ruleKey.equals(other.ruleKey);
    }
  }

  /**
   * Obtains the key of the {@link Portfolio} referenced by this key.
   *
   * @return a {@link Portfolio} key
   */
  public PortfolioKey getPortfolioKey() {
    return portfolioKey;
  }

  /**
   * Obtains the key of the {@link Rule} referenced by this key.
   *
   * @return a {@link Rule} key
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
