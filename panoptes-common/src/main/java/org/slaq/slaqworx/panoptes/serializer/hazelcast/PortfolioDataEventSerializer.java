package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link
 * PortfolioDataEvent}.
 *
 * @author jeremy
 */
public class PortfolioDataEventSerializer extends HazelcastStreamSerializer<PortfolioDataEvent> {
  /**
   * Creates a new {@link PortfolioDataEventSerializer}. Hazelcast requires a public default
   * constructor.
   */
  public PortfolioDataEventSerializer() {
    super(
        (ProtobufSerializer) new org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.PORTFOLIO_DATA_EVENT.ordinal();
  }
}
