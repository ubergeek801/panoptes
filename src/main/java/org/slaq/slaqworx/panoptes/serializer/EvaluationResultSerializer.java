package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.StringValue;
import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.apache.commons.lang3.tuple.Pair;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationResultMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.ExceptionMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleResultMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleResult;
import org.slaq.slaqworx.panoptes.rule.RuleResult.Threshold;

/**
 * {@code EvaluationResultSerializer} (de)serializes the state of a {@code EvaluationResult} using
 * Protobuf.
 *
 * @author jeremy
 */
public class EvaluationResultSerializer implements ByteArraySerializer<EvaluationResult> {
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
        IdKeyMsg ruleKeyMsg = evaluationResultMsg.getRuleKey();

        RuleKey ruleKey = new RuleKey(ruleKeyMsg.getId());

        Map<EvaluationGroup, RuleResult> results =
                convertResults(evaluationResultMsg.getResultList());
        Map<EvaluationGroup, RuleResult> proposedResults =
                convertResults(evaluationResultMsg.getProposedResultList());

        return new EvaluationResult(ruleKey, results, proposedResults);
    }

    @Override
    public byte[] write(EvaluationResult result) throws IOException {
        IdKeyMsg.Builder ruleKeyBuilder = IdKeyMsg.newBuilder();
        ruleKeyBuilder.setId(result.getRuleKey().getId());

        EvaluationResultMsg.Builder resultBuilder = EvaluationResultMsg.newBuilder();
        resultBuilder.setRuleKey(ruleKeyBuilder);
        resultBuilder.addAllResult(convertResults(result.getResults()));
        resultBuilder.addAllProposedResult(convertResults(result.getProposedResults()));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        resultBuilder.build().writeTo(out);
        return out.toByteArray();
    }

    /**
     * Converts an {@code EvaluationGroup}/{@code RuleResult} pair into its serialized form.
     *
     * @param evaluationGroup
     *            the {@code EvaluationGroup} from which to create a {@code RuleResultMsg}
     * @param ruleResult
     *            the {@code RuleResult} from which to create a {@code RuleResultMsg}
     * @return a {@code RuleResultMsg} providing a serialization of the given data
     */
    protected RuleResultMsg convert(EvaluationGroup evaluationGroup, RuleResult ruleResult) {
        RuleResultMsg.Builder resultMsgBuilder = RuleResultMsg.newBuilder();
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
            if (ruleResult.getBenchmarkValue() != null) {
                resultMsgBuilder.setBenchmarkValue(DoubleValue.of(ruleResult.getBenchmarkValue()));
            }
            resultMsgBuilder.setIsPassed(ruleResult.isPassed());
        } else {
            resultMsgBuilder.setIsPassed(ruleResult.isPassed());
        }

        return resultMsgBuilder.build();
    }

    /**
     * Converts a {@code RuleResultMsg} into its deserialized form.
     *
     * @param resultMsg
     *            the {@code RuleResultMsg} to be deserialized
     * @return a {@code RuleResult} constructed from the serialized data
     */
    protected RuleResult convert(RuleResultMsg resultMsg) {
        if (resultMsg.hasException()) {
            ExceptionMsg exceptionMsg = resultMsg.getException();

            // FIXME fully reconstruct the exception
            return new RuleResult(new Exception(exceptionMsg.getExceptionClass()
                    + " thrown with message: " + exceptionMsg.getMessage()));
        }

        if (!resultMsg.hasThreshold() || !resultMsg.hasValue()) {
            // must be a "simple" result
            return new RuleResult(resultMsg.getIsPassed());
        }

        // must be a "value" result
        Double benchmarkValue =
                (resultMsg.hasBenchmarkValue() ? resultMsg.getBenchmarkValue().getValue() : null);
        return new RuleResult(Threshold.valueOf(resultMsg.getThreshold().getValue()),
                resultMsg.getValue().getValue(), benchmarkValue);
    }

    /**
     * Converts a collection of {@code RuleResultMsg}s into deserialized form.
     *
     * @param resultMsgs
     *            the {@code RuleResultMsg}s to be deserialized
     * @return a {@code Map} correlating each {@code EvaluationGroup} to the {@code RuleResult}
     *         computed for that group
     */
    protected Map<EvaluationGroup, RuleResult>
            convertResults(Collection<RuleResultMsg> resultMsgs) {
        return resultMsgs.stream().map(resultMsg -> {
            String aggregationKey =
                    (resultMsg.hasAggregationKey() ? resultMsg.getAggregationKey().getValue()
                            : null);
            return Pair.of(new EvaluationGroup(resultMsg.getId(), aggregationKey),
                    convert(resultMsg));
        }).collect(Collectors.toMap(p -> p.getLeft(), p -> p.getRight()));
    }

    /**
     * Converts a map of evaluation results to serialized form.
     *
     * @param results
     *            a {@code Map} correlating each {@code EvaluationGroup} to the {@code RuleResult}
     *            computed for that group
     * @return a {@code Collection<RuleResultMsg>} representing the serialized form
     */
    protected Collection<RuleResultMsg> convertResults(Map<EvaluationGroup, RuleResult> results) {
        return results.entrySet().stream().map(e -> convert(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
