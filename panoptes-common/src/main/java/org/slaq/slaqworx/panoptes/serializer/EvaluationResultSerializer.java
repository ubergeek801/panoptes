package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.StringValue;

import org.apache.commons.lang3.tuple.Pair;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationResultMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.ExceptionMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.ValueResultMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Threshold;

/**
 * {@code EvaluationResultSerializer} (de)serializes the state of a {@code EvaluationResult} using
 * Protobuf.
 *
 * @author jeremy
 */
public class EvaluationResultSerializer implements ProtobufSerializer<EvaluationResult> {
    public static EvaluationResultMsg convert(EvaluationResult result) {
        IdKeyMsg.Builder ruleKeyBuilder = IdKeyMsg.newBuilder();
        ruleKeyBuilder.setId(result.getRuleKey().getId());

        EvaluationResultMsg.Builder resultBuilder = EvaluationResultMsg.newBuilder();
        resultBuilder.setRuleKey(ruleKeyBuilder);
        resultBuilder.addAllResult(convertResults(result.getResults()));
        resultBuilder.addAllProposedResult(convertResults(result.getProposedResults()));

        return resultBuilder.build();
    }

    public static EvaluationResult convert(EvaluationResultMsg evaluationResultMsg) {
        IdKeyMsg ruleKeyMsg = evaluationResultMsg.getRuleKey();
        RuleKey ruleKey = new RuleKey(ruleKeyMsg.getId());

        Map<EvaluationGroup, ValueResult> results =
                convertResults(evaluationResultMsg.getResultList());
        Map<EvaluationGroup, ValueResult> proposedResults =
                convertResults(evaluationResultMsg.getProposedResultList());

        return new EvaluationResult(ruleKey, results, proposedResults);
    }

    /**
     * Converts an {@code EvaluationGroup}/{@code RuleResult} pair into its serialized form.
     *
     * @param evaluationGroup
     *            the {@code EvaluationGroup} from which to create a {@code ValueResultMsg}
     * @param ruleResult
     *            the {@code RuleResult} from which to create a {@code ValueResultMsg}
     * @return a {@code ValueResultMsg} providing a serialization of the given data
     */
    protected static ValueResultMsg convert(EvaluationGroup evaluationGroup,
            ValueResult ruleResult) {
        ValueResultMsg.Builder resultMsgBuilder = ValueResultMsg.newBuilder();
        resultMsgBuilder.setId(evaluationGroup.getId());
        String aggregationKey = evaluationGroup.getAggregationKey();
        if (aggregationKey != null) {
            resultMsgBuilder.setAggregationKey(StringValue.of(aggregationKey));
        }
        Throwable exception = ruleResult.getException();
        if (exception != null) {
            ExceptionMsg.Builder exceptionMsgBuilder = ExceptionMsg.newBuilder();
            exceptionMsgBuilder.setExceptionClass(exception.getClass().getName());
            exceptionMsgBuilder.setMessage(exception.getMessage());
            // FIXME fully populate exception
            resultMsgBuilder.setException(exceptionMsgBuilder.build());
        } else if (ruleResult.getThreshold() != null && ruleResult.getValue() != null) {
            resultMsgBuilder.setThreshold(StringValue.of(ruleResult.getThreshold().name()));
            resultMsgBuilder.setValue(DoubleValue.of(ruleResult.getValue()));
            resultMsgBuilder.setIsPassed(ruleResult.isPassed());
        } else {
            resultMsgBuilder.setIsPassed(ruleResult.isPassed());
        }

        return resultMsgBuilder.build();
    }

    /**
     * Converts a {@code ValueResultMsg} into its deserialized form.
     *
     * @param resultMsg
     *            the {@code ValueResultMsg} to be deserialized
     * @return a {@code RuleResult} constructed from the serialized data
     */
    protected static ValueResult convert(ValueResultMsg resultMsg) {
        if (resultMsg.hasException()) {
            ExceptionMsg exceptionMsg = resultMsg.getException();

            // FIXME fully reconstruct the exception
            return new ValueResult(new Exception(exceptionMsg.getExceptionClass()
                    + " thrown with message: " + exceptionMsg.getMessage()));
        }

        if (!resultMsg.hasThreshold() || !resultMsg.hasValue()) {
            // must be a "simple" result
            return new ValueResult(resultMsg.getIsPassed());
        }

        // must be a "value" result
        return new ValueResult(Threshold.valueOf(resultMsg.getThreshold().getValue()),
                resultMsg.getValue().getValue());
    }

    /**
     * Converts a collection of {@code ValueResultMsg}s into deserialized form.
     *
     * @param resultMsgs
     *            the {@code ValueResultMsg}s to be deserialized
     * @return a {@code Map} correlating each {@code EvaluationGroup} to the {@code RuleResult}
     *         computed for that group
     */
    protected static Map<EvaluationGroup, ValueResult>
            convertResults(Collection<ValueResultMsg> resultMsgs) {
        return resultMsgs.stream().map(resultMsg -> {
            String aggregationKey =
                    (resultMsg.hasAggregationKey() ? resultMsg.getAggregationKey().getValue()
                            : null);
            return Pair.of(new EvaluationGroup(resultMsg.getId(), aggregationKey),
                    convert(resultMsg));
        }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    /**
     * Converts a map of evaluation results to serialized form.
     *
     * @param results
     *            a {@code Map} correlating each {@code EvaluationGroup} to the {@code RuleResult}
     *            computed for that group
     * @return a {@code Collection<ValueResultMsg>} representing the serialized form
     */
    protected static Collection<ValueResultMsg>
            convertResults(Map<EvaluationGroup, ValueResult> results) {
        return results.entrySet().stream().map(e -> convert(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a new {@code EvaluationResultSerializer}.
     */
    public EvaluationResultSerializer() {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.EVALUATION_RESULT.ordinal();
    }

    @Override
    public EvaluationResult read(byte[] buffer) throws IOException {
        EvaluationResultMsg evaluationResultMsg = EvaluationResultMsg.parseFrom(buffer);

        return convert(evaluationResultMsg);
    }

    @Override
    public byte[] write(EvaluationResult result) throws IOException {
        EvaluationResultMsg msg = convert(result);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        msg.writeTo(out);
        return out.toByteArray();
    }
}
