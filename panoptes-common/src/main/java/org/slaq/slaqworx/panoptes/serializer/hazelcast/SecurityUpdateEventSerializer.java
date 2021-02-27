package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.event.SecurityUpdateEvent;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code
 * SecurityUpdateEvent}.
 *
 * @author jeremy
 */
public class SecurityUpdateEventSerializer extends HazelcastStreamSerializer<SecurityUpdateEvent> {
  /**
   * Creates a new {@code SecurityUpdateEventSerializer}. Hazelcast requires a public default
   * constructor.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public SecurityUpdateEventSerializer() {
    super((ProtobufSerializer) new org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.SECURITY_UPDATE_EVENT.ordinal();
  }
}
