package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.function.TriFunction;
import com.hazelcast.map.IMap;
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
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.event.SecurityUpdateEvent;
import org.slaq.slaqworx.panoptes.pipeline.BenchmarkRuleEvaluator.BenchmarkRuleEvaluatorState;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A process function which, similarly to {@link PortfolioRuleEvaluator}, collects security and
 * portfolio position data. However, this class evaluates rules only against benchmarks (which are
 * merely portfolios that are specially designated as such). The rules to be evaluated against a
 * particular benchmark are obtained by collecting rules from non-benchmark portfolios which are
 * encountered.
 *
 * @author jeremy
 */
public class BenchmarkRuleEvaluator implements SupplierEx<BenchmarkRuleEvaluatorState>,
    TriFunction<BenchmarkRuleEvaluatorState, PortfolioKey, PortfolioEvent,
        Traverser<RuleEvaluationResult>> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(BenchmarkRuleEvaluator.class);
  private transient BenchmarkRuleEvaluatorState processState;

  /**
   * Creates a new {@link BenchmarkRuleEvaluator}.
   */
  public BenchmarkRuleEvaluator() {
    // nothing to do
  }

  @Override
  public Traverser<RuleEvaluationResult> applyEx(BenchmarkRuleEvaluatorState processState,
      PortfolioKey eventKey, PortfolioEvent event) {
    this.processState = processState;

    ArrayList<RuleEvaluationResult> results = new ArrayList<>();
    if (event instanceof SecurityUpdateEvent) {
      SecurityKey securityKey = ((SecurityUpdateEvent) event).getSecurityKey();

      IMap<SecurityKey, Security> securityMap = PanoptesApp.getAssetCache().getSecurityCache();
      Security security = securityMap.get(securityKey);
      handleSecurityEvent(security, results);
    } else {
      handleBenchmarkEvent(event, results);
    }

    return Traversers.traverseIterable(results);
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
   * @param results
   *     a {@link Collection} into which evaluation results, if any, are published
   */
  public void handleBenchmarkEvent(PortfolioEvent benchmarkEvent,
      Collection<RuleEvaluationResult> results) {
    if (!(benchmarkEvent instanceof PortfolioDataEvent)) {
      // not interesting to us
      return;
    }

    Portfolio portfolio = ((PortfolioDataEvent) benchmarkEvent).getPortfolio();

    if (portfolio.isAbstract()) {
      processState.portfolioTracker.trackPortfolio(portfolio);
      // the portfolio is a benchmark, so try to process it
      IMap<SecurityKey, Security> securityMap = PanoptesApp.getAssetCache().getSecurityCache();
      processState.portfolioTracker.processPortfolio(results, portfolio, null, securityMap,
          () -> processState.benchmarkRules.values().stream());
    } else {
      // the portfolio is not a benchmark, but it may have rules that are of interest, so try
      // to extract and process them
      Collection<Rule> newRules = extractRules(portfolio);
      // process any newly-encountered rules against the benchmark
      IMap<SecurityKey, Security> securityMap = PanoptesApp.getAssetCache().getSecurityCache();
      processState.portfolioTracker
          .processPortfolio(results, processState.portfolioTracker.getPortfolio(), null,
              securityMap, newRules::stream);
    }
  }

  /**
   * Handles a security event.
   *
   * @param security
   *     the {@link Security} update information obtained from the event
   * @param results
   *     a {@link Collection} into which evaluation results, if any, are published
   */
  public void handleSecurityEvent(Security security, Collection<RuleEvaluationResult> results) {
    processState.portfolioTracker
        .applySecurity(security, () -> processState.benchmarkRules.values().stream(), results);
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
    private static final long serialVersionUID = 1L;

    PortfolioTracker portfolioTracker;
    ConcurrentHashMap<RuleKey, Rule> benchmarkRules;
  }
}
