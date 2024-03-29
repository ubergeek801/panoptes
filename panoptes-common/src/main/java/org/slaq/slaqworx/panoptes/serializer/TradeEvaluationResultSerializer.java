package org.slaq.slaqworx.panoptes.serializer;

import com.google.protobuf.StringValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioRuleImpactMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleImpactMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TradeEvaluationResultMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.ValueResult.Impact;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link TradeEvaluationResult}.
 *
 * @author jeremy
 */
public class TradeEvaluationResultSerializer implements ProtobufSerializer<TradeEvaluationResult> {
  /** Creates a new {@link TradeEvaluationResultSerializer}. */
  public TradeEvaluationResultSerializer() {
    // nothing to do
  }

  @Override
  public TradeEvaluationResult read(byte[] buffer) throws IOException {
    TradeEvaluationResultMsg evaluationResultMsg = TradeEvaluationResultMsg.parseFrom(buffer);

    IdKeyMsg tradeKey = evaluationResultMsg.getTradeKey();

    TradeEvaluationResult result = new TradeEvaluationResult(new TradeKey(tradeKey.getId()));
    evaluationResultMsg
        .getPortfolioRuleImpactList()
        .forEach(
            ruleImpact -> {
              PortfolioKey portfolioKey =
                  new PortfolioKey(
                      ruleImpact.getPortfolioKey().getId(),
                      ruleImpact.getPortfolioKey().getVersion());
              RuleKey ruleKey = new RuleKey(ruleImpact.getRuleKey().getId());
              ruleImpact
                  .getRuleImpactList()
                  .forEach(
                      impactMsg -> {
                        String aggregationKey =
                            (impactMsg.hasAggregationKey()
                                ? impactMsg.getAggregationKey().getValue()
                                : null);
                        EvaluationGroup evaluationGroup =
                            new EvaluationGroup(impactMsg.getId(), aggregationKey);
                        result.addImpact(
                            portfolioKey,
                            ruleKey,
                            evaluationGroup,
                            Impact.valueOf(impactMsg.getImpact()));
                      });
            });

    return result;
  }

  @Override
  public byte[] write(TradeEvaluationResult result) throws IOException {
    IdKeyMsg.Builder tradeKeyBuilder = IdKeyMsg.newBuilder();
    tradeKeyBuilder.setId(result.getTradeKey().getId());

    TradeEvaluationResultMsg.Builder resultBuilder = TradeEvaluationResultMsg.newBuilder();

    resultBuilder.setTradeKey(tradeKeyBuilder);

    result
        .getImpacts()
        .forEach(
            (portfolioRuleKey, impactMap) -> {
              PortfolioRuleImpactMsg.Builder impactMsgBuilder = PortfolioRuleImpactMsg.newBuilder();
              IdVersionKeyMsg.Builder portfolioKeyBuilder = IdVersionKeyMsg.newBuilder();
              portfolioKeyBuilder.setId(portfolioRuleKey.portfolioKey().getId());
              portfolioKeyBuilder.setVersion(portfolioRuleKey.portfolioKey().getVersion());
              IdKeyMsg.Builder ruleKeyBuilder = IdKeyMsg.newBuilder();
              ruleKeyBuilder.setId(portfolioRuleKey.ruleKey().id());

              impactMsgBuilder.setPortfolioKey(portfolioKeyBuilder);
              impactMsgBuilder.setRuleKey(ruleKeyBuilder);
              impactMap.forEach(
                  (evaluationGroup, impact) -> {
                    RuleImpactMsg.Builder ruleImpactMsgBuilder = RuleImpactMsg.newBuilder();
                    ruleImpactMsgBuilder.setId(evaluationGroup.id());
                    String aggregationGroup = evaluationGroup.aggregationKey();
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
