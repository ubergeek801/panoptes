package org.slaq.slaqworx.panoptes.pipeline;

import java.io.Serial;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.sink2.WriterInitContext;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Sink} which consumes {@link RuleEvaluationResult}s. Currently this merely writes a
 * summary of each result to the log.
 *
 * @author jeremy
 */
public class EvaluationResultPublisher implements Sink<RuleEvaluationResult> {
  @Serial private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipeline.class);

  @Override
  public SinkWriter<RuleEvaluationResult> createWriter(WriterInitContext context) {
    return new SinkWriter<>() {
      @Override
      public void write(RuleEvaluationResult evaluationResult, Context context) {
        EvaluationResult result = evaluationResult.evaluationResult();
        LOG.info("produced {} results for rule {}", result.results().size(), result.getKey());
      }

      @Override
      public void flush(boolean endOfInput) {
        // nothing to do
      }

      @Override
      public void close() {
        // nothing to do
      }
    };
  }
}
