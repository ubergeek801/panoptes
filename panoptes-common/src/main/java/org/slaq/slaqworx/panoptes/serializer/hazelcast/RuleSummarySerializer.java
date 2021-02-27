package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.rule.RuleSummary;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code RuleSummary}.
 *
 * @author jeremy
 */
public class RuleSummarySerializer extends HazelcastStreamSerializer<RuleSummary> {
  /**
   * Creates a new {@code RuleSummarySerializer}. Hazelcast requires a public default constructor.
   */
  public RuleSummarySerializer() {
    super(new org.slaq.slaqworx.panoptes.serializer.RuleSummarySerializer());
  }

  @Override
  public int getTypeId() {
    return SerializerTypeId.RULE_SUMMARY.ordinal();
  }
}
