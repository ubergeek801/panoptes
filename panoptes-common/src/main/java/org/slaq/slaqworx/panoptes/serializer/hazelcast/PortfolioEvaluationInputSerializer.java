package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.event.PortfolioEvaluationInput;

/**
 * A {@link HazelcastStreamSerializer} wihch (de)serializes the state of a {@link
 * PortfolioEvaluationInput}.
 *
 * @author jeremy
 */
public class PortfolioEvaluationInputSerializer
    extends HazelcastStreamSerializer<PortfolioEvaluationInput> {
  /**
   * Creates a new {@link PortfolioEvaluationInputSerializer}. Hazelcast requires a public default
   * constructor.
   */
  public PortfolioEvaluationInputSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.PortfolioEvaluationInputSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.PORTFOLIO_EVALUATION_INPUT.ordinal();
  }
}
