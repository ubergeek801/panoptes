package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link Transaction}.
 *
 * @author jeremy
 */
public class TransactionSerializer extends HazelcastStreamSerializer<Transaction> {
  /**
   * Creates a new {@link TransactionSerializer}. Hazelcast requires a public default constructor.
   */
  public TransactionSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TransactionSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.TRANSACTION.ordinal();
  }
}
