package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.datamodel.Tuple3;
import java.io.Serial;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A transformation for extracting rules from {@link Portfolio}s:
 * <ul>
 * <li>In the case of a standard {@link Portfolio}, the rules are simply taken from the object
 * itself.</li>
 * <li>In the case of a benchmark, which does not define its own rules, all standard
 * {@link Portfolio}s are observed to find rules which reference the benchmark, and these are
 * emitted with the benchmark's key.</li>
 * </ul>
 * Rules are emitted as they are discovered. For a standard portfolio, this is the result of
 * processing an initial {@link PortfolioDataEvent}, while for a benchmark portfolio, associated
 * rules are discovered incrementally with each event. Currently, the effect of such events is
 * cumulative; rules are never removed from an association with a portfolio or benchmark.
 *
 * @author jeremy
 */
public class RuleExtractor
    implements FunctionEx<PortfolioEvent, Traverser<Tuple3<EvaluationSource, PortfolioKey, Rule>>> {
  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@link RuleExtractor}.
   */
  public RuleExtractor() {
    // nothing to do
  }

  @Override
  public Traverser<Tuple3<EvaluationSource, PortfolioKey, Rule>> applyEx(
      PortfolioEvent portfolioEvent) {
    if (!(portfolioEvent instanceof PortfolioDataEvent portfolioDataEvent)) {
      // nothing to see here
      return Traversers.empty();
    }

    Portfolio portfolio = portfolioDataEvent.portfolio();
    if (portfolio.isAbstract()) {
      // this is a benchmark and will have no rules of its own
      return Traversers.empty();
    }

    // this is a standard portfolio

    // emit all of the portfolio's rules, keyed on the portfolio key
    ArrayList<Tuple3<EvaluationSource, PortfolioKey, Rule>> rules = portfolio.getRules()
        .map(r -> Tuple3.tuple3(EvaluationSource.PORTFOLIO, portfolioEvent.getPortfolioKey(), r))
        .collect(Collectors.toCollection(ArrayList::new));

    // also emit any of the portfolio's benchmark rules, keyed on the benchmark's key
    if (portfolio.getBenchmarkKey() != null) {
      portfolio.getRules().filter(Rule::isBenchmarkSupported).forEach(r -> rules
          .add(Tuple3.tuple3(EvaluationSource.BENCHMARK, portfolio.getBenchmarkKey(), r)));
    }

    return Traversers.traverseIterable(rules);
  }
}
