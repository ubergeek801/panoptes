package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.inject.Singleton;
import org.slaq.slaqworx.panoptes.cache.PortfolioSummarizer;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationContextMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link PortfolioSummarizer}.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioSummarizerSerializer implements ProtobufSerializer<PortfolioSummarizer> {
  /**
   * Creates a new {@link PortfolioSummarizerSerializer}.
   */
  public PortfolioSummarizerSerializer() {
    // nothing to do
  }

  /**
   * Converts an {@link PortfolioSummarizer} into a new {@link EvaluationContextMsg}.
   *
   * @param portfolioSummarizer
   *     the {@link PortfolioSummarizer} to be converted
   *
   * @return a {@link EvaluationContextMsg}
   */
  public static EvaluationContextMsg convert(PortfolioSummarizer portfolioSummarizer) {
    EvaluationContextMsg.Builder evaluationContextBuilder = EvaluationContextMsg.newBuilder();
    evaluationContextBuilder
        .setEvaluationMode(portfolioSummarizer.getEvaluationContext().getEvaluationMode().name());
    portfolioSummarizer.getEvaluationContext().getSecurityOverrides().forEach(
        (securityKey, attributes) -> evaluationContextBuilder
            .putSecurityOverrides(securityKey.getId(), SecuritySerializer.convert(attributes)));

    return evaluationContextBuilder.build();
  }

  @Override
  public PortfolioSummarizer read(byte[] buffer) throws IOException {
    EvaluationContextMsg evaluationContextMsg = EvaluationContextMsg.parseFrom(buffer);

    EvaluationContext evaluationContext = EvaluationContextSerializer.convert(evaluationContextMsg);
    return new PortfolioSummarizer(evaluationContext);
  }

  @Override
  public byte[] write(PortfolioSummarizer portfolioSummarizer) throws IOException {
    EvaluationContextMsg evaluationContextMsg = convert(portfolioSummarizer);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    evaluationContextMsg.writeTo(out);
    return out.toByteArray();
  }
}
