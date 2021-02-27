package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code
 * PortfolioRuleKey}.
 *
 * @author jeremy
 */
public class PortfolioRuleKeySerializer extends HazelcastStreamSerializer<PortfolioRuleKey> {
  /**
   * Creates a new {@code PortfolioRuleKeySerializer}. Hazelcast requires a public default
   * constructor.
   */
  public PortfolioRuleKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioRuleKeySerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.PORTFOLIO_RULE_KEY.ordinal();
  }
}
