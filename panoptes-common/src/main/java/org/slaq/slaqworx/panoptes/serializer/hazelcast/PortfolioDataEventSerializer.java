package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code
 * PortfolioDataEvent}.
 *
 * @author jeremy
 */
public class PortfolioDataEventSerializer extends HazelcastStreamSerializer<PortfolioDataEvent> {
  /**
   * Creates a new {@code PortfolioDataEventSerializer}. Hazelcast requires a public default
   * constructor.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public PortfolioDataEventSerializer() {
    super((ProtobufSerializer) new org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.PORTFOLIO_DATA_EVENT.ordinal();
  }
}
