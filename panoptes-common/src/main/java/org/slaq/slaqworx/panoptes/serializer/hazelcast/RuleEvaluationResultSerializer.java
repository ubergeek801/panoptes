package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code
 * RuleEvaluationResult}.
 *
 * @author jeremy
 */
public class RuleEvaluationResultSerializer
    extends HazelcastStreamSerializer<RuleEvaluationResult> {
  /**
   * Creates a new {@code RuleEvaluationResultSerializer}. Hazelcast requires a public default
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
