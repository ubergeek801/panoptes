package org.slaq.slaqworx.panoptes.pipeline.serializer;

import java.io.IOException;
import javax.inject.Provider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeKeySerializer;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;

public class TradeEvaluationRequestSerializationSchema
    extends ProtobufSerializationSchema<TradeEvaluationRequest> {
  private static final long serialVersionUID = 1L;

  private final TradeKeySerializer keySerializer;

  public TradeEvaluationRequestSerializationSchema(String topic,
      Provider<AssetCache> assetCacheProvider) {
    super(topic);
    keySerializer = new TradeKeySerializer();
  }

  @Override
  protected ProtobufSerializer<TradeEvaluationRequest> createSerializer() {
    return new TradeEvaluationRequestSerializer();
  }

  @Override
  protected byte[] serializeKey(TradeEvaluationRequest request) throws IOException {
    return keySerializer.write(request.getTradeKey());
  }
}
