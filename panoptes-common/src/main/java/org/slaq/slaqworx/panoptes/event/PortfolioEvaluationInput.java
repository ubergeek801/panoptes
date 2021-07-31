package org.slaq.slaqworx.panoptes.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RulesProvider;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * Encapsulates the inputs to the {@code PortfolioEvaluationService}.
 */
public class PortfolioEvaluationInput implements ProtobufSerializable {
  @Nonnull
  private final EvaluationSource evaluationSource;
  @Nonnull
  private final PortfolioKey portfolioKey;
  @Nonnull
  private final ArrayList<Rule> rules;

  public PortfolioEvaluationInput(@Nonnull EvaluationSource evaluationSource,
      @Nonnull PortfolioKey portfolioKey, @Nonnull RulesProvider rulesProvider) {
    this.evaluationSource = evaluationSource;
    this.portfolioKey = portfolioKey;
    rules = rulesProvider.getRules().collect(Collectors.toCollection(ArrayList::new));
  }

  public PortfolioEvaluationInput(@Nonnull EvaluationSource evaluationSource,
      @Nonnull PortfolioKey portfolioKey, @Nonnull Collection<Rule> rules) {
    this.evaluationSource = evaluationSource;
    this.portfolioKey = portfolioKey;
    this.rules = new ArrayList<>(rules);
  }

  @Nonnull
  public EvaluationSource getEvaluationSource() {
    return evaluationSource;
  }

  @Nonnull
  public PortfolioKey getPortfolioKey() {
    return portfolioKey;
  }

  @Nonnull
  public ArrayList<Rule> getRules() {
    return rules;
  }
}
