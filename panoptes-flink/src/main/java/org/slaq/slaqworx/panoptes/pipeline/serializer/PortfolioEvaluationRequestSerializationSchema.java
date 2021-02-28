package org.slaq.slaqworx.panoptes.pipeline.serializer;

import java.io.IOException;
import javax.inject.Provider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.serializer.PortfolioEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

public class PortfolioEvaluationRequestSerializationSchema
    extends ProtobufSerializationSchema<PortfolioEvaluationRequest> {
  private static final long serialVersionUID = 1L;

  private final PortfolioKeySerializer keySerializer;

  public PortfolioEvaluationRequestSerializationSchema(String topic,
      Provider<AssetCache> assetCacheProvider) {
    super(topic);
    keySerializer = new PortfolioKeySerializer();
  }

  @Override
  protected ProtobufSerializer<PortfolioEvaluationRequest> createSerializer() {
    return new PortfolioEvaluationRequestSerializer();
  }

  @Override
  protected byte[] serializeKey(PortfolioEvaluationRequest request) throws IOException {
    return keySerializer.write(request.getPortfolioKey());
  }
}
