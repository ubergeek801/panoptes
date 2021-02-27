package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSummarySerializer;

public class PortfolioSummaryKryoSerializer extends ProtobufKryoSerializer<PortfolioSummary> {
  public PortfolioSummaryKryoSerializer() {
    // nothing to do
  }

  @Override
  protected PortfolioSummarySerializer createProtobufSerializer() {
    return new PortfolioSummarySerializer();
  }
}
