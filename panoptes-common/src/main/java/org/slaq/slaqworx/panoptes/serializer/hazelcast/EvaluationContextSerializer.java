package org.slaq.slaqworx.panoptes.serializer.hazelcast;

import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * A {@code HazelcastStreamSerializer} which (de)serializes the state of an
 * {@code EvaluationContext}.
 *
 * @author jeremy
 */
public class EvaluationContextSerializer extends HazelcastStreamSerializer<EvaluationContext> {
    /**
     * Creates a new {@code EvaluationContextSerializer}. Hazelcast requires a public default
     * constructor.
     */
    public EvaluationContextSerializer() {
        super(new org.slaq.slaqworx.panoptes.serializer.EvaluationContextSerializer());
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.EVALUATION_CONTEXT.ordinal();
    }
}
