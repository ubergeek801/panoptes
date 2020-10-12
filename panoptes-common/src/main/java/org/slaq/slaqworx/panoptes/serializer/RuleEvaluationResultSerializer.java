package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.protobuf.DoubleValue;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg;

/**
 * A (de)serializer for the state of a {@code RuleEvaluationResult} using Protobuf.
 *
 * @author jeremy
 */
public class RuleEvaluationResultSerializer implements ProtobufSerializer<RuleEvaluationResult> {
    /**
     * Creates a new {@code TradeEvaluationResultSerializer}.
     */
    public RuleEvaluationResultSerializer() {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.RULE_EVALUATION_RESULT.ordinal();
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
        boolean isBenchmarkSupported = msg.getIsBenchmarkSupported();
        Double lowerLimit = (msg.hasLowerLimit() ? msg.getLowerLimit().getValue() : null);
        Double upperLimit = (msg.hasUpperLimit() ? msg.getUpperLimit().getValue() : null);
        EvaluationResult evaluationResult =
                EvaluationResultSerializer.convert(msg.getEvaluationResult());

        return new RuleEvaluationResult(eventId, portfolioKey, benchmarkKey, isBenchmarkSupported,
                lowerLimit, upperLimit, evaluationResult);
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
