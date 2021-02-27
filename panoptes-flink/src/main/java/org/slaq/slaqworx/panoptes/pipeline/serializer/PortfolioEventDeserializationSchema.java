package org.slaq.slaqworx.panoptes.pipeline.serializer;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

public class PortfolioEventDeserializationSchema
    extends ProtobufDeserializationSchema<PortfolioEvent> {
  private static final long serialVersionUID = 1L;

  public PortfolioEventDeserializationSchema() {
    // nothing to do
  }

  @Override
  public TypeInformation<PortfolioEvent> getProducedType() {
    return TypeInformation.of(PortfolioEvent.class);
  }

  @Override
  protected ProtobufSerializer<PortfolioEvent> createSerializer() {
    return new PortfolioEventSerializer();
  }
}
