package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.BiConsumerEx;
import com.hazelcast.jet.core.Processor;
import com.hazelcast.jet.pipeline.Sink;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.Serial;
import java.util.HashSet;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link BiConsumerEx}, intended for use as a {@link Sink} receive function, which consumes
 * {@link RuleEvaluationResult}s. Currently this merely writes a summary of each result to the log.
 *
 * @author jeremy
 */
public class EvaluationResultPublisher
    implements BiConsumerEx<Processor.Context, RuleEvaluationResult> {
  @Serial
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(EvaluationResultPublisher.class);

  private static final HashSet<PortfolioKey> distinctPortfolios = new HashSet<>();

  // EvaluationResultPublisher must be Serializable but this is not; FIXME find a better way
  private static MeterRegistry meterRegistry;

  /**
   * Creates a new {@link EvaluationResultPublisher} which publishes metrics to the given {@link
   * MeterRegistry}.
   *
   * @param meterRegistry
   *     the {@link MeterRegistry} to which to publish metrics
   */
  public EvaluationResultPublisher(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public void acceptEx(Processor.Context context, RuleEvaluationResult evaluationResult) {
    EvaluationResult result = evaluationResult.evaluationResult();
    LOG.info("produced {} results for rule {} on portfolio {}", result.results().size(),
        result.getKey(), evaluationResult.portfolioKey());

    String counter = switch (evaluationResult.source()) {
      case BENCHMARK -> "benchmark";
      case PORTFOLIO -> "portfolio";
      case BENCHMARK_COMPARISON -> "benchmarkComparison";
      case UNRECOGNIZED -> "unrecognized";
    };

    meterRegistry.counter(counter + "Results").increment();
    meterRegistry.counter(counter + "RuleResults").increment(result.results().size());

    synchronized (distinctPortfolios) {
      if (distinctPortfolios.add(evaluationResult.portfolioKey())) {
        LOG.info("produced results for {} distinct portfolios", distinctPortfolios.size());
      }
    }
  }
}
