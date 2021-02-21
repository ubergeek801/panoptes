package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of a {@code Rule} (actually a
 * {@code ConfigurableRule}.
 *
 * @author jeremy
 */
public class RuleSerializer extends HazelcastStreamSerializer<ConfigurableRule> {
    /**
     * Creates a new {@code RuleSerializer}. Hazelcast requires a public default constructor.
     */
    public RuleSerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.RuleSerializer());
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.RULE.ordinal();
    }
}
