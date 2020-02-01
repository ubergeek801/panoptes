package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.SecurityAttributes;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationContextMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;

/**
 * {@code EvaluationContextSerializer} (de)serializes the state of an {@code EvaluationContext}
 * using Protobuf.
 *
 * @author jeremy
 */
@Singleton
public class EvaluationContextSerializer implements ByteArraySerializer<EvaluationContext> {
    /**
     * Converts an {@code EvaluationContext} into a new {@code EvaluationContextMsg}.
     *
     * @param evaluationContext
     *            the {@code EvaluationContext} to be converted
     * @return a {@code EvaluationContextMsg}
     */
    public static EvaluationContextMsg convert(EvaluationContext evaluationContext) {
        EvaluationContextMsg.Builder evaluationContextBuilder = EvaluationContextMsg.newBuilder();
        evaluationContextBuilder.setEvaluationMode(evaluationContext.getEvaluationMode().name());
        evaluationContext.getSecurityOverrides().forEach((securityKey, attributes) -> {
            evaluationContextBuilder.putSecurityOverrides(securityKey.getId(),
                    SecuritySerializer.convert(attributes));
        });

        return evaluationContextBuilder.build();
    }

    /**
     * Converts a {@code EvaluationContextMsg} into a new {@code EvaluationContext}.
     *
     * @param evaluationContextMsg
     *            the message to be converted
     * @return a {@code EvaluationContext}
     */
    public static EvaluationContext convert(EvaluationContextMsg evaluationContextMsg) {
        Map<SecurityKey, SecurityAttributes> securityAttributeOverrides = evaluationContextMsg
                .getSecurityOverridesMap().entrySet().stream()
                .collect(Collectors.toMap(e -> new SecurityKey(e.getKey()),
                        e -> new SecurityAttributes(SecuritySerializer.convert(e.getValue()))));

        return new EvaluationContext(
                EvaluationMode.valueOf(evaluationContextMsg.getEvaluationMode()),
                securityAttributeOverrides);
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.EVALUATION_CONTEXT.ordinal();
    }

    @Override
    public EvaluationContext read(byte[] buffer) throws IOException {
        EvaluationContextMsg evaluationContextMsg = EvaluationContextMsg.parseFrom(buffer);

        return convert(evaluationContextMsg);
    }

    @Override
    public byte[] write(EvaluationContext evaluationContext) throws IOException {
        EvaluationContextMsg evaluationContextMsg = convert(evaluationContext);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        evaluationContextMsg.writeTo(out);
        return out.toByteArray();
    }
}
