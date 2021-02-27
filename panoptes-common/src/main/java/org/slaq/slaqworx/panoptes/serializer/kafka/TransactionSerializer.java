package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code Transaction}.
 *
 * @author jeremy
 */
public class TransactionSerializer extends KafkaSerializer<Transaction> {
  /**
   * Creates a new {@code TransactionSerializer}. Kafka requires a public default constructor.
   */
  public TransactionSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TransactionSerializer());
  }
}
