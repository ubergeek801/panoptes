package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.BiConsumerEx;
import com.hazelcast.jet.core.Processor;
import com.hazelcast.jet.pipeline.Sink;
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
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(EvaluationResultPublisher.class);

  @Override
  public void acceptEx(Processor.Context context, RuleEvaluationResult evaluationResult) {
    EvaluationResult result = evaluationResult.getEvaluationResult();
    LOG.info("produced {} results for rule {}", result.getResults().size(), result.getKey());
  }
}
