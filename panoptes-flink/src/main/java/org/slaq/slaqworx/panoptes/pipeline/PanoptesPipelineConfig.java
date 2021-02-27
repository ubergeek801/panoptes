package org.slaq.slaqworx.panoptes.pipeline;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import java.util.Properties;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer.Semantic;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.pipeline.serializer.EvaluationResultSerializationSchema;
import org.slaq.slaqworx.panoptes.pipeline.serializer.PortfolioEvaluationRequestDeserializationSchema;
import org.slaq.slaqworx.panoptes.pipeline.serializer.PortfolioEventDeserializationSchema;
import org.slaq.slaqworx.panoptes.pipeline.serializer.SecurityDeserializationSchema;
import org.slaq.slaqworx.panoptes.pipeline.serializer.TradeDeserializationSchema;
import org.slaq.slaqworx.panoptes.pipeline.serializer.TradeEvaluationResultSerializationSchema;
import org.slaq.slaqworx.panoptes.pipeline.serializer.kryo.EvaluationResultKryoSerializer;
import org.slaq.slaqworx.panoptes.pipeline.serializer.kryo.PortfolioEventKryoSerializer;
import org.slaq.slaqworx.panoptes.pipeline.serializer.kryo.PortfolioKeyKryoSerializer;
import org.slaq.slaqworx.panoptes.pipeline.serializer.kryo.PortfolioKryoSerializer;
import org.slaq.slaqworx.panoptes.pipeline.serializer.kryo.PortfolioRuleKeyKryoSerializer;
import org.slaq.slaqworx.panoptes.pipeline.serializer.kryo.PortfolioSummaryKryoSerializer;
import org.slaq.slaqworx.panoptes.pipeline.serializer.kryo.RuleEvaluationResultKryoSerializer;
import org.slaq.slaqworx.panoptes.pipeline.serializer.kryo.RuleKeyKryoSerializer;
import org.slaq.slaqworx.panoptes.pipeline.serializer.kryo.RuleKryoSerializer;
import org.slaq.slaqworx.panoptes.pipeline.serializer.kryo.SecurityKeyKryoSerializer;
import org.slaq.slaqworx.panoptes.pipeline.serializer.kryo.SecurityKryoSerializer;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.MarketValueRule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Micronaut {@code Factory} which configures and provides various beans to the {@code
 * ApplicationContext}.
 *
 * @author jeremy
 */
@Factory
public class PanoptesPipelineConfig {
  private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipelineConfig.class);

  public static final String BENCHMARK_SOURCE = "benchmarkSource";
  public static final String PORTFOLIO_SOURCE = "portfolioSource";
  public static final String PORTFOLIO_EVALUATION_REQUEST_SOURCE =
      "portfolioEvaluationRequestSource";
  public static final String PORTFOLIO_EVALUATION_RESULT_SINK = "portfolioEvaluationResultSink";
  public static final String SECURITY_SOURCE = "securitySource";
  public static final String TRADE_SOURCE = "tradeSource";
  public static final String TRADE_EVALUATION_RESULT_SINK = "tradeEvaluationResultSink";

  @Property(name = "kafka")
  private Properties kafkaProperties;
  @Property(name = "kafka-topic.benchmark-topic")
  private String benchmarkTopic;
  @Property(name = "kafka-topic.portfolio-topic")
  private String portfolioTopic;
  @Property(
      name = "kafka-topic.portfolio-request-topic")
  private String portfolioEvaluationRequestTopic;
  @Property(
      name = "kafka-topic.portfolio-result-topic")
  private String portfolioEvaluationResultTopic;
  @Property(name = "kafka-topic.security-topic")
  private String securityTopic;
  @Property(name = "kafka-topic.trade-topic")
  private String tradeTopic;
  @Property(name = "kafka-topic.trade-result-topic")
  private String tradeEvaluationResultTopic;

  /**
   * Creates a new {@code PanoptesPipelineConfig}. Restricted because this class is managed by
   * Micronaut.
   */
  protected PanoptesPipelineConfig() {
    // nothing to do
  }

  /**
   * Configures and provides a {@code SourceFunction} to source benchmark data from Kafka.
   *
   * @return a {@code SourceFunction}
   */
  @Singleton
  @Named(BENCHMARK_SOURCE)
  protected SourceFunction<PortfolioEvent> benchmarkSource() {
    LOG.info("using {} as benchmark topic", benchmarkTopic);

    FlinkKafkaConsumer<PortfolioEvent> consumer = new FlinkKafkaConsumer<>(benchmarkTopic,
        new PortfolioEventDeserializationSchema(), kafkaProperties);
    consumer.setStartFromEarliest();

    return consumer;
  }

  /**
   * Configures and provides the Flink execution environment.
   *
   * @return a {@code StreamExecutionEnvironment}
   */
  @Singleton
  protected StreamExecutionEnvironment executionEnvironment() {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    LOG.info("using global parallelism {}", env.getParallelism());

    // since we deal with immutable (or effectively immutable) objects, we can reduce Flink's
    // copying; this doesn't do much for larger machines but does help in more
    // resource-constrained environments
    env.getConfig().enableObjectReuse();

    env.disableOperatorChaining();

    // in Flink, Protobuf serialization is supported via custom serializers registered with
    // Flink; we need these for any (top-level) classes passed between Flink operators, classes
    // used in operator state, etc.
    env.getConfig().registerTypeWithKryoSerializer(EvaluationResult.class,
        EvaluationResultKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(Portfolio.class,
        PortfolioKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(PortfolioEvent.class,
        PortfolioEventKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(PortfolioKey.class,
        PortfolioKeyKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(PortfolioRuleKey.class,
        PortfolioRuleKeyKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(PortfolioSummary.class,
        PortfolioSummaryKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(RuleEvaluationResult.class,
        RuleEvaluationResultKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(RuleKey.class, RuleKeyKryoSerializer.class);
    // theoretically we should just be able to register a Rule (or maybe ConfigurableRule)
    // serializer, but Flink/Kryo aren't happy unless we register the concrete rule classes
    env.getConfig().registerTypeWithKryoSerializer(ConcentrationRule.class,
        RuleKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(MarketValueRule.class,
        RuleKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(WeightedAverageRule.class,
        RuleKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(Security.class,
        SecurityKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(SecurityKey.class,
        SecurityKeyKryoSerializer.class);

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
    LOG.info("using {} as portfolioEvaluationRequest topic", portfolioEvaluationRequestTopic);

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
    LOG.info("using {} as portfolioEvaluationResult topic", portfolioEvaluationResultTopic);

    FlinkKafkaProducer<EvaluationResult> producer =
        new FlinkKafkaProducer<>(portfolioEvaluationResultTopic,
            new EvaluationResultSerializationSchema(portfolioEvaluationResultTopic),
            kafkaProperties, Semantic.AT_LEAST_ONCE);
    producer.setWriteTimestampToKafka(true);

    return producer;
  }

  /**
   * Configures and provides a {@code SourceFunction} to source portfolio data from Kafka.
   *
   * @return a {@code SourceFunction}
   */
  @Singleton
  @Named(PORTFOLIO_SOURCE)
  protected SourceFunction<PortfolioEvent> portfolioSource() {
    LOG.info("using {} as portfolio topic", portfolioTopic);

    FlinkKafkaConsumer<PortfolioEvent> consumer = new FlinkKafkaConsumer<>(portfolioTopic,
        new PortfolioEventDeserializationSchema(), kafkaProperties);
    consumer.setStartFromEarliest();

    return consumer;
  }

  /**
   * Configures and provides a {@code SourceFunction} to source security data from Kafka.
   *
   * @return a {@code SourceFunction}
   */
  @Singleton
  @Named(SECURITY_SOURCE)
  protected SourceFunction<Security> securitySource() {
    LOG.info("using {} as security topic", securityTopic);

    FlinkKafkaConsumer<Security> consumer = new FlinkKafkaConsumer<>(securityTopic,
        new SecurityDeserializationSchema(), kafkaProperties);
    consumer.setStartFromEarliest();

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
    LOG.info("using {} as tradeEvaluationResult topic", tradeEvaluationResultTopic);

    FlinkKafkaProducer<TradeEvaluationResult> producer =
        new FlinkKafkaProducer<>(tradeEvaluationResultTopic,
            new TradeEvaluationResultSerializationSchema(tradeEvaluationResultTopic),
            kafkaProperties, Semantic.AT_LEAST_ONCE);
    producer.setWriteTimestampToKafka(true);

    return producer;
  }

  /**
   * Configures and provides a {@code SourceFunction} to source trades from Kafka.
   *
   * @return a {@code SourceFunction}
   */
  @Singleton
  @Named(TRADE_SOURCE)
  protected SourceFunction<Trade> tradeSource() {
    LOG.info("using {} as trade topic", tradeTopic);

    FlinkKafkaConsumer<Trade> consumer = new FlinkKafkaConsumer<>(tradeTopic,
        new TradeDeserializationSchema(), kafkaProperties);

    return consumer;
  }
}
