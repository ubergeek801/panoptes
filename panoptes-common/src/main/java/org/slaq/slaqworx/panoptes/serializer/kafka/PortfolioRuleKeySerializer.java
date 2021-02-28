package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link PortfolioRuleKey}.
 *
 * @author jeremy
 */
public class PortfolioRuleKeySerializer extends KafkaSerializer<PortfolioRuleKey> {
  /**
   * Creates a new {@link PortfolioRuleKeySerializer}. Kafka requires a public default constructor.
   */
  public PortfolioRuleKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioRuleKeySerializer());
  }
}
