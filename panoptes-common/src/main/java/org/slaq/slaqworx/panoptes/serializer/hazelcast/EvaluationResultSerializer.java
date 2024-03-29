package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link EvaluationResult}.
 *
 * @author jeremy
 */
public class EvaluationResultSerializer extends HazelcastStreamSerializer<EvaluationResult> {
  /**
   * Creates a new {@link EvaluationResultSerializer}. Hazelcast requires a public default
   * constructor.
   */
  public EvaluationResultSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.EvaluationResultSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.EVALUATION_RESULT.ordinal();
  }
}
