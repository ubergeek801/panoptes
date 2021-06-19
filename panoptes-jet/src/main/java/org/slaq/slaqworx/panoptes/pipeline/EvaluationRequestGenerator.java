package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.function.TriFunction;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.event.PortfolioEvaluationInput;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RulesProvider;

public class EvaluationRequestGenerator
    implements SupplierEx<EvaluationRequestGenerator.EvaluationRequestGeneratorState> {
  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Provides a {@link TriFunction} to handle {@link Portfolio} evaluation events.
   *
   * @return the {@link PortfolioEvaluationInput} handling function
   */
  @Nonnull
  public TriFunction<EvaluationRequestGeneratorState, PortfolioKey, PortfolioKey,
      Traverser<PortfolioEvaluationInput>> evaluationEventHandler() {
    return (s, k, e) -> handleEvaluationEvent(s, e);
  }

  /**
   * Provides a {@link TriFunction} to handle rule events.
   *
   * @return the rule event handling function
   */
  @Nonnull
  public TriFunction<EvaluationRequestGeneratorState, PortfolioKey, Tuple3<EvaluationSource,
      PortfolioKey, Rule>, Traverser<PortfolioEvaluationInput>> ruleEventHandler() {
    return (s, k, e) -> handleRuleEvent(s, e);
  }

  /**
   * Creates a new {@link EvaluationRequestGenerator}.
   */
  public EvaluationRequestGenerator() {
    // nothing to do
  }

  @Override
  @Nonnull
  public EvaluationRequestGeneratorState getEx() {
    return new EvaluationRequestGeneratorState();
  }

  @Nonnull
  protected Traverser<PortfolioEvaluationInput> handleEvaluationEvent(
      @Nonnull EvaluationRequestGeneratorState state, @Nonnull PortfolioKey portfolioKey) {
    state.setPortfolioKey(portfolioKey);

    if (!state.hasRules()) {
      // nothing we can/need to do
      return Traversers.empty();
    }

    return Traversers
        .singleton(new PortfolioEvaluationInput(state.getEvaluationSource(), portfolioKey, state));
  }

  @Nonnull
  protected Traverser<PortfolioEvaluationInput> handleRuleEvent(
      @Nonnull EvaluationRequestGeneratorState state,
      @Nonnull Tuple3<EvaluationSource, PortfolioKey, Rule> rule) {
    state.setEvaluationSource(rule.f0());
    state.addRule(rule.f2());
    if (state.getPortfolioKey() == null) {
      // nothing we can do yet
      return Traversers.empty();
    }

    return Traversers.singleton(
        new PortfolioEvaluationInput(state.getEvaluationSource(), state.getPortfolioKey(),
            () -> Stream.of(rule.f2())));
  }

  /**
   * FIXME implement Protobuf serialization once the interface settles down
   *
   * @author jeremy
   */
  static class EvaluationRequestGeneratorState implements RulesProvider, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private EvaluationSource evaluationSource;
    private PortfolioKey portfolioKey;
    private final HashMap<RuleKey, Rule> rules = new HashMap<>();

    public EvaluationRequestGeneratorState() {
      // nothing to do
    }

    public EvaluationSource getEvaluationSource() {
      return evaluationSource;
    }

    public PortfolioKey getPortfolioKey() {
      return portfolioKey;
    }

    public void addRule(@Nonnull Rule rule) {
      this.rules.put(rule.getKey(), rule);
    }

    public void addRules(@Nonnull Collection<Rule> rules) {
      rules.forEach(this::addRule);
    }

    @Override
    @Nonnull
    public Stream<Rule> getRules() {
      return rules.values().stream();
    }

    public void setEvaluationSource(@Nonnull EvaluationSource evaluationSource) {
      this.evaluationSource = evaluationSource;
    }

    public void setPortfolioKey(@Nonnull PortfolioKey portfolioKey) {
      this.portfolioKey = portfolioKey;
    }

    public boolean hasRules() {
      return !rules.isEmpty();
    }
  }
}
