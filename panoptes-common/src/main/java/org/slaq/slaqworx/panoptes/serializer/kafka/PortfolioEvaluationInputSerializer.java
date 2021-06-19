package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.event.PortfolioEvaluationInput;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link PortfolioEvaluationInput}.
 *
 * @author jeremy
 */
public class PortfolioEvaluationInputSerializer extends KafkaSerializer<PortfolioEvaluationInput> {
  /**
   * Creates a new {@link PortfolioEvaluationInputSerializer}. Kafka requires a public default
   * constructor.
   */
  public PortfolioEvaluationInputSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioEvaluationInputSerializer());
  }
}
