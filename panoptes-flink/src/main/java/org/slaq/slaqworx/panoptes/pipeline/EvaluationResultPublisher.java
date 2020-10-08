package org.slaq.slaqworx.panoptes.pipeline;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;

public class EvaluationResultPublisher implements SinkFunction<EvaluationResult> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipeline.class);

    @Override
    public void invoke(EvaluationResult result, Context context) {
        LOG.info("produced result: {}",
                ToStringBuilder.reflectionToString(result, ToStringStyle.JSON_STYLE));
    }
}
