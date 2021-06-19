package org.slaq.slaqworx.panoptes.serializer;

import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import org.slaq.slaqworx.panoptes.asset.SecurityAttributes;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationContextMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of an {@link EvaluationContext}.
 *
 * @author jeremy
 */
@Singleton
public class EvaluationContextSerializer implements ProtobufSerializer<EvaluationContext> {
  /**
   * Creates a new {@link EvaluationContextSerializer}.
   */
  public EvaluationContextSerializer() {
    // nothing to do
  }

  /**
   * Converts an {@link EvaluationContext} into a new {@link EvaluationContextMsg}.
   *
   * @param evaluationContext
   *     the {@link EvaluationContext} to be converted
   *
   * @return a {@link EvaluationContextMsg}
   */
  public static EvaluationContextMsg convert(EvaluationContext evaluationContext) {
    EvaluationContextMsg.Builder evaluationContextBuilder = EvaluationContextMsg.newBuilder();
    evaluationContextBuilder.setEvaluationMode(evaluationContext.getEvaluationMode().name());
    evaluationContext.getSecurityOverrides().forEach(
        (securityKey, attributes) -> evaluationContextBuilder
            .putSecurityOverrides(securityKey.id(), SecuritySerializer.convert(attributes)));

    return evaluationContextBuilder.build();
  }

  /**
   * Converts a {@link EvaluationContextMsg} into a new {@link EvaluationContext}.
   *
   * @param evaluationContextMsg
   *     the message to be converted
   *
   * @return a {@link EvaluationContext}
   */
  public static EvaluationContext convert(EvaluationContextMsg evaluationContextMsg) {
    Map<SecurityKey, SecurityAttributes> securityAttributeOverrides =
        evaluationContextMsg.getSecurityOverridesMap().entrySet().stream().collect(Collectors
            .toMap(e -> new SecurityKey(e.getKey()),
                e -> new SecurityAttributes(SecuritySerializer.convert(e.getValue()))));

    return new EvaluationContext(EvaluationMode.valueOf(evaluationContextMsg.getEvaluationMode()),
        securityAttributeOverrides);
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
