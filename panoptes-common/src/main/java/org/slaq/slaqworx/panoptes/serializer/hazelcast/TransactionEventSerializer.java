package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.event.TransactionEvent;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link TransactionEvent}.
 *
 * @author jeremy
 */
public class TransactionEventSerializer extends HazelcastStreamSerializer<TransactionEvent> {
  /**
   * Creates a new {@link TransactionEventSerializer}. Hazelcast requires a public default
   * constructor.
   */
  public TransactionEventSerializer() {
    super(
        (ProtobufSerializer) new org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.TRANSACTION_EVENT.ordinal();
  }
}
