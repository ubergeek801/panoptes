package org.slaq.slaqworx.panoptes.pipeline;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;

public class EvaluationResultPublisher implements SinkFunction<RuleEvaluationResult> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipeline.class);

    @Override
    public void invoke(RuleEvaluationResult evaluationResult, Context context) {
        EvaluationResult result = evaluationResult.getEvaluationResult();
        LOG.info("produced {} results for rule {}", result.getResults().size(), result.getKey());
    }
}
