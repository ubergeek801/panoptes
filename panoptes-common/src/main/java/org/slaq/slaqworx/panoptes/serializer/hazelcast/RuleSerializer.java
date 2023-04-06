package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A {@link HazelcastStreamSerializer} which (de)serializes the state of a {@link Rule} (actually a
 * {@link ConfigurableRule}.
 *
 * @author jeremy
 */
public class RuleSerializer extends HazelcastStreamSerializer<ConfigurableRule> {
  /** Creates a new {@link RuleSerializer}. Hazelcast requires a public default constructor. */
  public RuleSerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.RuleSerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.RULE.ordinal();
  }
}
