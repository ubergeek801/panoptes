package org.slaq.slaqworx.panoptes.pipeline.serializer;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.serializer.EvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

public class EvaluationResultSerializationSchema
    extends ProtobufSerializationSchema<EvaluationResult> {
  private static final long serialVersionUID = 1L;

  public EvaluationResultSerializationSchema(String topic) {
    super(topic);
  }

  @Override
  protected ProtobufSerializer<EvaluationResult> createSerializer() {
    return new EvaluationResultSerializer();
  }

  @Override
  protected byte[] serializeKey(EvaluationResult result) {
    // not currently needed
    return null;
  }
}
