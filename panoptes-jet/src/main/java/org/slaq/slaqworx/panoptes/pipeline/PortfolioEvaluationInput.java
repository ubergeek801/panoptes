package org.slaq.slaqworx.panoptes.pipeline;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RulesProvider;

/**
 * Encapsulates the inputs to the {@link PortfolioEvaluationService}.
 * <p>
 * FIXME implement Protobuf serialization once the interface settles down
 */
public class PortfolioEvaluationInput implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private final EvaluationSource evaluationSource;
  private final Portfolio portfolio;
  private final ArrayList<Rule> rules;

  public PortfolioEvaluationInput(EvaluationSource evaluationSource, Portfolio portfolio,
      RulesProvider rulesProvider) {
    this.evaluationSource = evaluationSource;
    this.portfolio = portfolio;
    rules = rulesProvider.getRules().collect(Collectors.toCollection(ArrayList::new));
  }

  public EvaluationSource getEvaluationSource() {
    return evaluationSource;
  }

  public Portfolio getPortfolio() {
    return portfolio;
  }

  public ArrayList<Rule> getRules() {
    return rules;
  }
}
