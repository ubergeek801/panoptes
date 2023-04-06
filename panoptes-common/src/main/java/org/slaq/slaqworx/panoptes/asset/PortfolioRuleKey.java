package org.slaq.slaqworx.panoptes.asset;

import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A key to specify or retrieve evaluation results by {@link Portfolio} and {@link Rule}.
 *
 * @param portfolioKey the key of the referenced {@link Portfolio}
 * @param ruleKey the key of the referenced {@link Rule}
 */
public record PortfolioRuleKey(@Nonnull PortfolioKey portfolioKey, @Nonnull RuleKey ruleKey)
    implements ProtobufSerializable {
  // trivial
}
