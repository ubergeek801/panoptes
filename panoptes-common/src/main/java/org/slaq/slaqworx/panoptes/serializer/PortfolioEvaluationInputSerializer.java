package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.event.PortfolioEvaluationInput;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioEvaluationInputMsg;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link
 * PortfolioEvaluationInput}.
 *
 * @author jeremy
 */
public class PortfolioEvaluationInputSerializer
    implements ProtobufSerializer<PortfolioEvaluationInput> {
  /**
   * Creates a new {@link PortfolioEvaluationInputSerializer}.
   */
  public PortfolioEvaluationInputSerializer() {
    // nothing to do
  }

  @Override
  public PortfolioEvaluationInput read(byte[] buffer) throws IOException {
    PortfolioEvaluationInputMsg inputMsg = PortfolioEvaluationInputMsg.parseFrom(buffer);

    EvaluationSource evaluationSource = inputMsg.getEvaluationSource();

    PortfolioKey portfolioKey = new PortfolioKey(inputMsg.getPortfolioKey().getId(),
        inputMsg.getPortfolioKey().getVersion());

    Set<Rule> rules =
        inputMsg.getRuleList().stream().map(RuleSerializer::convert).collect(Collectors.toSet());

    return new PortfolioEvaluationInput(evaluationSource, portfolioKey, rules);
  }

  @Override
  public byte[] write(PortfolioEvaluationInput input) throws IOException {
    PortfolioEvaluationInputMsg.Builder inputBuilder = PortfolioEvaluationInputMsg.newBuilder();

    EvaluationSource evaluationSource = input.getEvaluationSource();
    inputBuilder.setEvaluationSource(evaluationSource);

    IdVersionKeyMsg.Builder portfolioKeyBuilder = IdVersionKeyMsg.newBuilder();
    portfolioKeyBuilder.setId(input.getPortfolioKey().getId());
    portfolioKeyBuilder.setVersion(input.getPortfolioKey().getVersion());
    inputBuilder.setPortfolioKey(portfolioKeyBuilder.build());

    input.getRules()
        .forEach(r -> inputBuilder.addRule(RuleSerializer.convert((ConfigurableRule) r)));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    inputBuilder.build().writeTo(out);
    return out.toByteArray();
  }
}
