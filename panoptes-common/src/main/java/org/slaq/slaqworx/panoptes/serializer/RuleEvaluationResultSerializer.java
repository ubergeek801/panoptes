package org.slaq.slaqworx.panoptes.serializer;

import com.google.protobuf.DoubleValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link RuleEvaluationResult}.
 *
 * @author jeremy
 */
public class RuleEvaluationResultSerializer implements ProtobufSerializer<RuleEvaluationResult> {
  /** Creates a new {@link RuleEvaluationResultSerializer}. */
  public RuleEvaluationResultSerializer() {
    // nothing to do
  }

  @Override
  public RuleEvaluationResult read(byte[] buffer) throws IOException {
    RuleEvaluationResultMsg msg = RuleEvaluationResultMsg.parseFrom(buffer);

    long eventId = msg.getEventId();
    IdVersionKeyMsg keyMsg = msg.getPortfolioKey();
    PortfolioKey portfolioKey = new PortfolioKey(keyMsg.getId(), keyMsg.getVersion());
    PortfolioKey benchmarkKey;
    if (msg.hasBenchmarkKey()) {
      keyMsg = msg.getBenchmarkKey();
      benchmarkKey = new PortfolioKey(keyMsg.getId(), keyMsg.getVersion());
    } else {
      benchmarkKey = null;
    }
    EvaluationSource source = msg.getSource();
    boolean isBenchmarkSupported = msg.getIsBenchmarkSupported();
    Double lowerLimit = (msg.hasLowerLimit() ? msg.getLowerLimit().getValue() : null);
    Double upperLimit = (msg.hasUpperLimit() ? msg.getUpperLimit().getValue() : null);
    EvaluationResult evaluationResult =
        EvaluationResultSerializer.convert(msg.getEvaluationResult());

    return new RuleEvaluationResult(
        eventId,
        portfolioKey,
        benchmarkKey,
        source,
        isBenchmarkSupported,
        lowerLimit,
        upperLimit,
        evaluationResult);
  }

  @Override
  public byte[] write(RuleEvaluationResult result) throws IOException {
    RuleEvaluationResultMsg.Builder msg = RuleEvaluationResultMsg.newBuilder();

    msg.setEventId(result.eventId());

    IdVersionKeyMsg.Builder portfolioKey = IdVersionKeyMsg.newBuilder();
    portfolioKey.setId(result.portfolioKey().getId());
    portfolioKey.setVersion(result.portfolioKey().getVersion());
    msg.setPortfolioKey(portfolioKey);

    if (result.benchmarkKey() != null) {
      IdVersionKeyMsg.Builder benchmarkKey = IdVersionKeyMsg.newBuilder();
      benchmarkKey.setId(result.benchmarkKey().getId());
      benchmarkKey.setVersion(result.benchmarkKey().getVersion());
      msg.setBenchmarkKey(benchmarkKey);
    }

    msg.setSource(result.source());

    msg.setIsBenchmarkSupported(result.isBenchmarkSupported());

    if (result.lowerLimit() != null) {
      msg.setLowerLimit(DoubleValue.of(result.lowerLimit()));
    }

    if (result.upperLimit() != null) {
      msg.setUpperLimit(DoubleValue.of(result.upperLimit()));
    }

    msg.setEvaluationResult(EvaluationResultSerializer.convert(result.evaluationResult()));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.build().writeTo(out);
    return out.toByteArray();
  }
}
