package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of a {@link TransactionKey}.
 *
 * @author jeremy
 */
public class TransactionKeySerializer extends KafkaSerializer<TransactionKey> {
  /**
   * Creates a new {@link TransactionKeySerializer}. Kafka requires a public default constructor.
   */
  public TransactionKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.TransactionKeySerializer());
  }
}
