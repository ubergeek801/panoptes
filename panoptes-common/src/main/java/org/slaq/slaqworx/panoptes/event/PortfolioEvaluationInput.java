package org.slaq.slaqworx.panoptes.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RulesProvider;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * Encapsulates the inputs to the {@code PortfolioEvaluationService}.
 */
public class PortfolioEvaluationInput implements ProtobufSerializable {
  private final EvaluationSource evaluationSource;
  private final PortfolioKey portfolioKey;
  private final ArrayList<Rule> rules;

  public PortfolioEvaluationInput(EvaluationSource evaluationSource, PortfolioKey portfolioKey,
      RulesProvider rulesProvider) {
    this.evaluationSource = evaluationSource;
    this.portfolioKey = portfolioKey;
    rules = rulesProvider.getRules().collect(Collectors.toCollection(ArrayList::new));
  }

  public PortfolioEvaluationInput(EvaluationSource evaluationSource, PortfolioKey portfolioKey,
      Collection<Rule> rules) {
    this.evaluationSource = evaluationSource;
    this.portfolioKey = portfolioKey;
    this.rules = new ArrayList<>(rules);
  }

  public EvaluationSource getEvaluationSource() {
    return evaluationSource;
  }

  public PortfolioKey getPortfolioKey() {
    return portfolioKey;
  }

  public ArrayList<Rule> getRules() {
    return rules;
  }
}
