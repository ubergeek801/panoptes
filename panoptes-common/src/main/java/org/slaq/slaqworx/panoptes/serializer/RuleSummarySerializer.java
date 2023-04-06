package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleSummaryMsg;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleSummary;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link RuleSummary}.
 *
 * @author jeremy
 */
public class RuleSummarySerializer implements ProtobufSerializer<RuleSummary> {
  /** Creates a new {@link RuleSummarySerializer}. */
  public RuleSummarySerializer() {
    // nothing to do
  }

  @Override
  public RuleSummary read(byte[] buffer) throws IOException {
    RuleSummaryMsg ruleMsg = RuleSummaryMsg.parseFrom(buffer);
    IdKeyMsg keyMsg = ruleMsg.getKey();

    return new RuleSummary(
        new RuleKey(keyMsg.getId()), ruleMsg.getDescription(), ruleMsg.getParameterDescription());
  }

  @Override
  public byte[] write(RuleSummary rule) throws IOException {
    IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
    keyBuilder.setId(rule.getKey().id());

    RuleSummaryMsg.Builder ruleBuilder = RuleSummaryMsg.newBuilder();
    ruleBuilder.setKey(keyBuilder);
    ruleBuilder.setDescription(rule.description());
    ruleBuilder.setParameterDescription(rule.parameterDescription());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ruleBuilder.build().writeTo(out);
    return out.toByteArray();
  }
}
