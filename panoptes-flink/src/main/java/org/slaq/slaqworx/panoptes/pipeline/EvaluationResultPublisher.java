package org.slaq.slaqworx.panoptes.pipeline;

import java.io.Serial;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sink which consumes {@link RuleEvaluationResult}s. Currently this merely writes a summary of
 * each result to the log.
 *
 * @author jeremy
 */
public class EvaluationResultPublisher implements SinkFunction<RuleEvaluationResult> {
  @Serial private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipeline.class);

  @Override
  public void invoke(RuleEvaluationResult evaluationResult, Context context) {
    EvaluationResult result = evaluationResult.evaluationResult();
    LOG.info("produced {} results for rule {}", result.results().size(), result.getKey());
  }
}
