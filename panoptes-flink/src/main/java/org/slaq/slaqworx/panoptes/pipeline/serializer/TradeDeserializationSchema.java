package org.slaq.slaqworx.panoptes.pipeline.serializer;

import java.io.Serial;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeSerializer;
import org.slaq.slaqworx.panoptes.trade.Trade;

public class TradeDeserializationSchema extends ProtobufDeserializationSchema<Trade> {
  @Serial private static final long serialVersionUID = 1L;

  public TradeDeserializationSchema() {
    // nothing to do
  }

  @Override
  public TypeInformation<Trade> getProducedType() {
    return TypeInformation.of(Trade.class);
  }

  @Override
  protected ProtobufSerializer<Trade> createSerializer() {
    return new TradeSerializer();
  }
}
