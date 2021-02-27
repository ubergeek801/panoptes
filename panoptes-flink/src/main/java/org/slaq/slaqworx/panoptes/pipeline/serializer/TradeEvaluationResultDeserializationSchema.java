package org.slaq.slaqworx.panoptes.pipeline.serializer;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeEvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;

public class TradeEvaluationResultDeserializationSchema
    extends ProtobufDeserializationSchema<TradeEvaluationResult> {
  private static final long serialVersionUID = 1L;

  public TradeEvaluationResultDeserializationSchema() {
    // nothing to do
  }

  @Override
  public TypeInformation<TradeEvaluationResult> getProducedType() {
    return TypeInformation.of(TradeEvaluationResult.class);
  }

  @Override
  protected ProtobufSerializer<TradeEvaluationResult> createSerializer() {
    return new TradeEvaluationResultSerializer();
  }
}
