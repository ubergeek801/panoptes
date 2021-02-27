package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code PortfolioRuleKey}.
 *
 * @author jeremy
 */
public class PortfolioRuleKeySerializer extends KafkaSerializer<PortfolioRuleKey> {
  /**
   * Creates a new {@code PortfolioRuleKeySerializer}. Kafka requires a public default
   * constructor.
   */
  public PortfolioRuleKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioRuleKeySerializer());
  }
}
