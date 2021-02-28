package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link
 * RuleEvaluationResult}.
 *
 * @author jeremy
 */
public class RuleEvaluationResultSerializer
    extends HazelcastStreamSerializer<RuleEvaluationResult> {
  /**
   * Creates a new {@link RuleEvaluationResultSerializer}. Hazelcast requires a public default
   * constructor.
   */
  public RuleEvaluationResultSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.RuleEvaluationResultSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.RULE_EVALUATION_RESULT.ordinal();
  }
}
