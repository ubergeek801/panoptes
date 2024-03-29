package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link TransactionKey}.
 *
 * @author jeremy
 */
public class TransactionKeySerializer extends HazelcastStreamSerializer<TransactionKey> {
  /**
   * Creates a new {@link TransactionKeySerializer}. Hazelcast requires a public default
   * constructor.
   */
  public TransactionKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TransactionKeySerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.TRANSACTION_KEY.ordinal();
  }
}
