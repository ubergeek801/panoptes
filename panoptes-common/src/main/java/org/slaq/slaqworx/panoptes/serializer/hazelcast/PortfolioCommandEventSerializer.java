package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.event.PortfolioCommandEvent;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link
 * PortfolioCommandEvent}.
 *
 * @author jeremy
 */
public class PortfolioCommandEventSerializer
    extends HazelcastStreamSerializer<PortfolioCommandEvent> {
  /**
   * Creates a new {@link PortfolioCommandEventSerializer}. Hazelcast requires a public default
   * constructor.
   */
  public PortfolioCommandEventSerializer() {
    super(
        (ProtobufSerializer) new org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.PORTFOLIO_COMMAND_EVENT.ordinal();
  }
}
