package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.serializer.PortfolioKeySerializer;

public class PortfolioKeyKryoSerializer extends ProtobufKryoSerializer<PortfolioKey> {
  public PortfolioKeyKryoSerializer() {
    // nothing to do
  }

  @Override
  protected PortfolioKeySerializer createProtobufSerializer() {
    return new PortfolioKeySerializer();
  }
}
