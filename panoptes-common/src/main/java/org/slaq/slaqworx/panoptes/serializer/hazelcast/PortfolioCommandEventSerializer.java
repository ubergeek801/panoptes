package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.event.PortfolioCommandEvent;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code
 * PortfolioCommandEvent}.
 *
 * @author jeremy
 */
public class PortfolioCommandEventSerializer
    extends HazelcastStreamSerializer<PortfolioCommandEvent> {
  /**
   * Creates a new {@code PortfolioCommandEventSerializer}. Hazelcast requires a public default
   * constructor.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public PortfolioCommandEventSerializer() {
    super((ProtobufSerializer) new org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.PORTFOLIO_COMMAND_EVENT.ordinal();
  }
}
