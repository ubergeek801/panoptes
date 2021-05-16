package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.function.TriFunction;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.SecurityUpdateEvent;
import org.slaq.slaqworx.panoptes.pipeline.BenchmarkReadinessEvaluator.BenchmarkRuleEvaluatorState;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A process function which, similarly to {@link PortfolioReadinessEvaluator}, collects security and
 * portfolio position data. However, this class evaluates rules only against benchmarks (which are
 * merely portfolios that are specially designated as such). The rules to be evaluated against a
 * particular benchmark are obtained by collecting rules from non-benchmark portfolios which are
 * encountered. FIXME update this doc
 *
 * @author jeremy
 */
public class BenchmarkReadinessEvaluator implements SupplierEx<BenchmarkRuleEvaluatorState>,
    TriFunction<BenchmarkRuleEvaluatorState, PortfolioKey, PortfolioEvent,
        Traverser<PortfolioEvaluationInput>> {
  @Serial
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(BenchmarkReadinessEvaluator.class);
  private transient BenchmarkRuleEvaluatorState processState;

  /**
   * Creates a new {@link BenchmarkReadinessEvaluator}.
   */
  public BenchmarkReadinessEvaluator() {
    // nothing to do
  }

  @Override
  public Traverser<PortfolioEvaluationInput> applyEx(BenchmarkRuleEvaluatorState processState,
      PortfolioKey eventKey, PortfolioEvent event) {
    this.processState = processState;

    PortfolioEvaluationInput evaluationInput;
    if (event instanceof SecurityUpdateEvent) {
      SecurityKey securityKey = ((SecurityUpdateEvent) event).getSecurityKey();

      evaluationInput = handleSecurityEvent(securityKey);
    } else {
      evaluationInput = handleBenchmarkEvent(event);
    }

    return (evaluationInput != null ? Traversers.singleton(evaluationInput) : Traversers.empty());
  }

  @Override
  public BenchmarkRuleEvaluatorState getEx() {
    BenchmarkRuleEvaluatorState state = new BenchmarkRuleEvaluatorState();
    state.portfolioTracker = new PortfolioTracker(EvaluationSource.BENCHMARK);
    state.benchmarkRules = new ConcurrentHashMap<>();

    return state;
  }

  /**
   * Handles a benchmark event, which is expected to be a {@link PortfolioDataEvent}.
   *
   * @param benchmarkEvent
   *     an event containing benchmark constituent data
   *
   * @return a {@link PortfolioEvaluationInput} providing input to the next stage, or {@code null}
   *     if the portfolio is not ready
   */
  public PortfolioEvaluationInput handleBenchmarkEvent(PortfolioEvent benchmarkEvent) {
    if (!(benchmarkEvent instanceof PortfolioDataEvent)) {
      // not interesting to us
      return null;
    }

    Portfolio portfolio = ((PortfolioDataEvent) benchmarkEvent).getPortfolio();

    if (portfolio.isAbstract()) {
      processState.portfolioTracker.trackPortfolio(portfolio);
      // the portfolio is a benchmark, so try to process it
      return processState.portfolioTracker
          .processPortfolio(portfolio, null, () -> processState.benchmarkRules.values().stream());
    }

    // the portfolio is not a benchmark, but it may have rules that are of interest, so try to
    // extract and process them
    Collection<Rule> newRules = extractRules(portfolio);
    // process any newly-encountered rules against the benchmark
    return processState.portfolioTracker
        .processPortfolio(processState.portfolioTracker.getPortfolio(), null, newRules::stream);
  }

  /**
   * Handles a security event.
   *
   * @param securityKey
   *     a key identifying the {@link Security} update information obtained from the event
   *
   * @return a {@link PortfolioEvaluationInput} providing input to the next stage, or {@code null}
   *     if the portfolio is not ready
   */
  public PortfolioEvaluationInput handleSecurityEvent(SecurityKey securityKey) {
    return processState.portfolioTracker
        .applySecurity(securityKey, () -> processState.benchmarkRules.values().stream());
  }

  /**
   * Extracts benchmark-enabled rules from the given portfolio.
   *
   * @param portfolio
   *     the {@link Portfolio} from which to extract rules
   *
   * @return a {@link Collection} of rules which are benchmark-enabled
   */
  protected Collection<Rule> extractRules(Portfolio portfolio) {
    ArrayList<Rule> newRules = new ArrayList<>();

    // since our input stream is keyed on the portfolio's benchmark, any portfolio that we
    // encounter should have its (benchmark-enabled) rules evaluated against the benchmark
    if (portfolio.getBenchmarkKey() != null) {
      List<Rule> benchmarkEnabledRules =
          portfolio.getRules().filter(Rule::isBenchmarkSupported).collect(Collectors.toList());
      if (!benchmarkEnabledRules.isEmpty()) {
        LOG.info("adding {} rules to benchmark {} from portfolio {}", benchmarkEnabledRules.size(),
            portfolio.getBenchmarkKey(), portfolio.getKey());
        benchmarkEnabledRules.forEach(r -> {
          if (!processState.benchmarkRules.containsKey(r.getKey())) {
            // we haven't seen this rule before
            newRules.add(r);
          }
          processState.benchmarkRules.put(r.getKey(), r);
        });
      }
    }

    return newRules;
  }

  /**
   * Contains the benchmark rule evaluation process state.
   */
  static class BenchmarkRuleEvaluatorState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    PortfolioTracker portfolioTracker;
    ConcurrentHashMap<RuleKey, Rule> benchmarkRules;
  }
}
