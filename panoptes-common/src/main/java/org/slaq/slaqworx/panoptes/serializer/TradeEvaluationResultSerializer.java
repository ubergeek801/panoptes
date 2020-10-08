package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import com.google.protobuf.StringValue;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioRuleImpactMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleImpactMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TradeEvaluationResultMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Impact;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;

/**
 * {@code TradeEvaluationResultSerializer} (de)serializes the state of a
 * {@code TradeEvaluationResult} using Protobuf.
 *
 * @author jeremy
 */
public class TradeEvaluationResultSerializer implements ProtobufSerializer<TradeEvaluationResult> {
    /**
     * Creates a new {@code TradeEvaluationResultSerializer}.
     */
    public TradeEvaluationResultSerializer() {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.TRADE_EVALUATION_RESULT.ordinal();
    }

    @Override
    public TradeEvaluationResult read(byte[] buffer) throws IOException {
        TradeEvaluationResultMsg evaluationResultMsg = TradeEvaluationResultMsg.parseFrom(buffer);

        TradeEvaluationResult result = new TradeEvaluationResult();
        evaluationResultMsg.getPortfolioRuleImpactList().forEach(ruleImpact -> {
            PortfolioKey portfolioKey = new PortfolioKey(ruleImpact.getPortfolioKey().getId(),
                    ruleImpact.getPortfolioKey().getVersion());
            RuleKey ruleKey = new RuleKey(ruleImpact.getRuleKey().getId());
            ruleImpact.getRuleImpactList().forEach(impactMsg -> {
                String aggregationKey =
                        (impactMsg.hasAggregationKey() ? impactMsg.getAggregationKey().getValue()
                                : null);
                EvaluationGroup evaluationGroup =
                        new EvaluationGroup(impactMsg.getId(), aggregationKey);
                result.addImpact(portfolioKey, ruleKey, evaluationGroup,
                        Impact.valueOf(impactMsg.getImpact()));
            });
        });

        return result;
    }

    @Override
    public byte[] write(TradeEvaluationResult result) throws IOException {
        TradeEvaluationResultMsg.Builder resultBuilder = TradeEvaluationResultMsg.newBuilder();

        result.getImpacts().entrySet().forEach(impactMapEntry -> {
            PortfolioRuleKey portfolioRuleKey = impactMapEntry.getKey();
            Map<EvaluationGroup, Impact> impactMap = impactMapEntry.getValue();
            PortfolioRuleImpactMsg.Builder impactMsgBuilder = PortfolioRuleImpactMsg.newBuilder();
            IdVersionKeyMsg.Builder portfolioKeyBuilder = IdVersionKeyMsg.newBuilder();
            portfolioKeyBuilder.setId(portfolioRuleKey.getPortfolioKey().getId());
            portfolioKeyBuilder.setVersion(portfolioRuleKey.getPortfolioKey().getVersion());
            IdKeyMsg.Builder ruleKeyBuilder = IdKeyMsg.newBuilder();
            ruleKeyBuilder.setId(portfolioRuleKey.getRuleKey().getId());

            impactMsgBuilder.setPortfolioKey(portfolioKeyBuilder);
            impactMsgBuilder.setRuleKey(ruleKeyBuilder);
            impactMap.entrySet().forEach(impactEntry -> {
                EvaluationGroup evaluationGroup = impactEntry.getKey();
                Impact impact = impactEntry.getValue();
                RuleImpactMsg.Builder ruleImpactMsgBuilder = RuleImpactMsg.newBuilder();
                ruleImpactMsgBuilder.setId(evaluationGroup.getId());
                String aggregationGroup = evaluationGroup.getAggregationKey();
                if (aggregationGroup != null) {
                    ruleImpactMsgBuilder.setAggregationKey(StringValue.of(aggregationGroup));
                }
                ruleImpactMsgBuilder.setImpact(impact.name());
                impactMsgBuilder.addRuleImpact(ruleImpactMsgBuilder);
            });
            resultBuilder.addPortfolioRuleImpact(impactMsgBuilder);
        });

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        resultBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
