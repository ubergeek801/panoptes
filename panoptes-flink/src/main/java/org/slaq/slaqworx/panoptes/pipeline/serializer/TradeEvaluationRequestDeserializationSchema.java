package org.slaq.slaqworx.panoptes.pipeline.serializer;

import java.io.Serial;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;

public class TradeEvaluationRequestDeserializationSchema
    extends ProtobufDeserializationSchema<TradeEvaluationRequest> {
  @Serial private static final long serialVersionUID = 1L;

  public TradeEvaluationRequestDeserializationSchema() {
    // nothing to do
  }

  @Override
  public TypeInformation<TradeEvaluationRequest> getProducedType() {
    return TypeInformation.of(TradeEvaluationRequest.class);
  }

  @Override
  protected ProtobufSerializer<TradeEvaluationRequest> createSerializer() {
    return new TradeEvaluationRequestSerializer();
  }
}
