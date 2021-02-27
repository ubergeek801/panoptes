package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * A {@code KafkaSerializer} which (de)serializes the state of a {@code TransactionKey}.
 *
 * @author jeremy
 */
public class TransactionKeySerializer extends KafkaSerializer<TransactionKey> {
  /**
   * Creates a new {@code TransactionKeySerializer}. Kafka requires a public default constructor.
   */
  public TransactionKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TransactionKeySerializer());
  }
}
