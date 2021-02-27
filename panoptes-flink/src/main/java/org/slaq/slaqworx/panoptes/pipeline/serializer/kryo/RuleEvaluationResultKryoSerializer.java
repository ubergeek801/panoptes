package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.serializer.RuleEvaluationResultSerializer;

public class RuleEvaluationResultKryoSerializer
    extends ProtobufKryoSerializer<RuleEvaluationResult> {
  public RuleEvaluationResultKryoSerializer() {
    // nothing to do
  }

  @Override
  protected RuleEvaluationResultSerializer createProtobufSerializer() {
    return new RuleEvaluationResultSerializer();
  }
}
