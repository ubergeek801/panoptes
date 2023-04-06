package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link RuleKey}.
 *
 * @author jeremy
 */
public class RuleKeySerializer extends HazelcastStreamSerializer<RuleKey> {
  /** Creates a new {@link RuleKeySerializer}. Hazelcast requires a public default constructor. */
  public RuleKeySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.RuleKeySerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.RULE_KEY.ordinal();
  }
}
