package org.slaq.slaqworx.panoptes.serializer;

import com.google.protobuf.DoubleValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg.EvaluationSource;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code RuleEvaluationResult}.
 *
 * @author jeremy
 */
public class RuleEvaluationResultSerializer implements ProtobufSerializer<RuleEvaluationResult> {
  /**
   * Creates a new {@code RuleEvaluationResultSerializer}.
   */
  public RuleEvaluationResultSerializer() {
    // nothing to do
  }

  @Override
  public RuleEvaluationResult read(byte[] buffer) throws IOException {
    RuleEvaluationResultMsg msg = RuleEvaluationResultMsg.parseFrom(buffer);

    long eventId = msg.getEventId();
    IdVersionKeyMsg keyMsg = msg.getPortfolioKey();
    PortfolioKey portfolioKey =
        (keyMsg == null ? null : new PortfolioKey(keyMsg.getId(), keyMsg.getVersion()));
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

    return new RuleEvaluationResult(eventId, portfolioKey, benchmarkKey, source,
        isBenchmarkSupported, lowerLimit, upperLimit, evaluationResult);
  }

  @Override
  public byte[] write(RuleEvaluationResult result) throws IOException {
    RuleEvaluationResultMsg.Builder msg = RuleEvaluationResultMsg.newBuilder();

    msg.setEventId(result.getEventId());

    IdVersionKeyMsg.Builder portfolioKey = IdVersionKeyMsg.newBuilder();
    portfolioKey.setId(result.getPortfolioKey().getId());
    portfolioKey.setVersion(result.getPortfolioKey().getVersion());
    msg.setPortfolioKey(portfolioKey);

    if (result.getBenchmarkKey() != null) {
      IdVersionKeyMsg.Builder benchmarkKey = IdVersionKeyMsg.newBuilder();
      benchmarkKey.setId(result.getBenchmarkKey().getId());
      benchmarkKey.setVersion(result.getBenchmarkKey().getVersion());
      msg.setBenchmarkKey(benchmarkKey);
    }

    msg.setIsBenchmarkSupported(result.isBenchmarkSupported());

    if (result.getLowerLimit() != null) {
      msg.setLowerLimit(DoubleValue.of(result.getLowerLimit()));
    }

    if (result.getUpperLimit() != null) {
      msg.setUpperLimit(DoubleValue.of(result.getUpperLimit()));
    }

    msg.setEvaluationResult(EvaluationResultSerializer.convert(result.getEvaluationResult()));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.build().writeTo(out);
    return out.toByteArray();
  }
}
