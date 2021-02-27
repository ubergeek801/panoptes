package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer;

public class PortfolioEventKryoSerializer extends ProtobufKryoSerializer<PortfolioEvent> {
  public PortfolioEventKryoSerializer() {
    // nothing to do
  }

  @Override
  protected PortfolioEventSerializer createProtobufSerializer() {
    return new PortfolioEventSerializer();
  }
}
