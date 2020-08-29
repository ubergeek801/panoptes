package org.slaq.slaqworx.panoptes.pipeline;

import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer.Semantic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.pipeline.serializer.PortfolioEvaluationRequestDeserializationSchema;
import org.slaq.slaqworx.panoptes.pipeline.serializer.PortfolioEvaluationResultSerializationSchema;
import org.slaq.slaqworx.panoptes.pipeline.serializer.TradeEvaluationRequestDeserializationSchema;
import org.slaq.slaqworx.panoptes.pipeline.serializer.TradeEvaluationResultSerializationSchema;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;

@Factory
public class PanoptesPipelineConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PanoptesPipelineConfig.class);

    public static final String PORTFOLIO_EVALUATION_REQUEST_SOURCE =
            "portfolioEvaluationRequestSource";
    public static final String PORTFOLIO_EVALUATION_RESULT_SINK = "portfolioEvaluationResultSink";
    public static final String TRADE_EVALUATION_REQUEST_SOURCE = "tradeEvaluationRequestSource";
    public static final String TRADE_EVALUATION_RESULT_SINK = "tradeEvaluationResultSink";

    @Property(name = "kafka") private Properties kafkaProperties;
    @Property(
            name = "kafka.portfolio-request-topic") private String portfolioEvaluationRequestTopic;
    @Property(name = "kafka.portfolio-result-topic") private String portfolioEvaluationResultTopic;
    @Property(name = "kafka.trade-request-topic") private String tradeEvaluationRequestTopic;
    @Property(name = "kafka.trade-result-topic") private String tradeEvaluationResultTopic;

    protected PanoptesPipelineConfig() {
        // nothing to do
    }

    /**
     * Configures and provides the Flink execution environment.
     *
     * @return a {@code StreamExecutionEnvironment}
     */
    @Singleton
    protected StreamExecutionEnvironment executionEnvironment() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        LOGGER.info("using global parallelism {}", env.getParallelism());

        // since we deal with immutable (or effectively immutable) objects, we can reduce Flink's
        // copying; this doesn't do much for larger machines but does help in more
        // resource-constrained environments
        env.getConfig().enableObjectReuse();

        return env;
    }

    /**
     * Configures and provides a {@code SourceFunction} to source portfolio evaluation requests from
     * Kafka.
     *
     * @return a {@code SourceFunction}
     */
    @Singleton
    @Named(PORTFOLIO_EVALUATION_REQUEST_SOURCE)
    protected SourceFunction<PortfolioEvaluationRequest> portfolioEvaluationRequestSource() {
        LOGGER.info("using {} as portfolioEvaluationRequest topic",
                portfolioEvaluationRequestTopic);

        FlinkKafkaConsumer<PortfolioEvaluationRequest> consumer =
                new FlinkKafkaConsumer<>(portfolioEvaluationRequestTopic,
                        new PortfolioEvaluationRequestDeserializationSchema(), kafkaProperties);

        return consumer;
    }

    /**
     * Configures and provides a {@code SinkFunction} to publish portfolio evaluation results to
     * Kafka.
     *
     * @return a {@code SinkFunction}
     */
    @Singleton
    @Named(PORTFOLIO_EVALUATION_RESULT_SINK)
    protected SinkFunction<EvaluationResult> portfolioEvaluationResultSink() {
        LOGGER.info("using {} as portfolioEvaluationResult topic", portfolioEvaluationResultTopic);

        FlinkKafkaProducer<EvaluationResult> producer =
                new FlinkKafkaProducer<>(tradeEvaluationResultTopic,
                        new PortfolioEvaluationResultSerializationSchema(
                                portfolioEvaluationResultTopic),
                        kafkaProperties, Semantic.AT_LEAST_ONCE);
        producer.setWriteTimestampToKafka(true);

        return producer;
    }

    /**
     * Configures and provides a {@code SourceFunction} to source trade evaluation requests from
     * Kafka.
     *
     * @return a {@code SourceFunction}
     */
    @Singleton
    @Named(TRADE_EVALUATION_REQUEST_SOURCE)
    protected SourceFunction<TradeEvaluationRequest> tradeEvaluationRequestSource() {
        LOGGER.info("using {} as tradeEvaluationRequest topic", tradeEvaluationRequestTopic);

        FlinkKafkaConsumer<TradeEvaluationRequest> consumer =
                new FlinkKafkaConsumer<>(tradeEvaluationRequestTopic,
                        new TradeEvaluationRequestDeserializationSchema(), kafkaProperties);

        return consumer;
    }

    /**
     * Configures and provides a {@code SinkFunction} to publish trade evaluation results to Kafka.
     *
     * @return a {@code SinkFunction}
     */
    @Singleton
    @Named(TRADE_EVALUATION_RESULT_SINK)
    protected SinkFunction<TradeEvaluationResult> tradeEvaluationResultSink() {
        LOGGER.info("using {} as tradeEvaluationResult topic", tradeEvaluationResultTopic);

        FlinkKafkaProducer<TradeEvaluationResult> producer =
                new FlinkKafkaProducer<>(tradeEvaluationResultTopic,
                        new TradeEvaluationResultSerializationSchema(tradeEvaluationResultTopic),
                        kafkaProperties, Semantic.AT_LEAST_ONCE);
        producer.setWriteTimestampToKafka(true);

        return producer;
    }
}
