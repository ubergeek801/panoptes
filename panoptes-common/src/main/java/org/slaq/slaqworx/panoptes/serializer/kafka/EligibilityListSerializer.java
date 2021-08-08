package org.slaq.slaqworx.panoptes.serializer.kafka;

import org.slaq.slaqworx.panoptes.asset.EligibilityList;

/**
 * A {@link KafkaSerializer} which (de)serializes the state of an {@link EligibilityList}.
 *
 * @author jeremy
 */
public class EligibilityListSerializer extends KafkaSerializer<EligibilityList> {
  /**
   * Creates a new {@link EligibilityListSerializer}. Kafka requires a public default constructor.
   */
  public EligibilityListSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.EligibilityListSerializer());
  }
}
