package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.asset.EligibilityList;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of an {@link EligibilityList}.
 *
 * @author jeremy
 */
public class EligibilityListSerializer extends HazelcastStreamSerializer<EligibilityList> {
  /**
   * Creates a new {@link EligibilityListSerializer}. Hazelcast requires a public default constructor.
   */
  public EligibilityListSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.EligibilityListSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.ELIGIBILITY_LIST.ordinal();
  }
}
