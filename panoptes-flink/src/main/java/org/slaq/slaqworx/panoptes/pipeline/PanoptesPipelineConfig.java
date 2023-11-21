package org.slaq.slaqworx.panoptes.pipeline;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Properties;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
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
 * A Micronaut {@link Factory} which configures and provides various beans to the {@link
 * ApplicationContext}.
 *
 * @author jeremy
 */
@Factory
public class PanoptesPipelineConfig {
  public static final String BENCHMARK_SOURCE = "benchmarkSource";
  public static final String PORTFOLIO_SOURCE = "portfolioSource";
  public static final String PORTFOLIO_EVALUATION_REQUEST_SOURCE =
      "portfolioEvaluationRequestSource";
  public static final String PORTFOLIO_EVALUATION_RESULT_SINK = "portfolioEvaluationResultSink";
  public static final String SECURITY_SOURCE = "securitySource";
  public static final String TRADE_SOURCE = "tradeSource";
  public static final String TRADE_EVALUATION_RESULT_SINK = "tradeEvaluationResultSink";

  private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipelineConfig.class);

  @Property(name = "kafka")
  private Properties kafkaProperties;

  @Property(name = "kafka-topic.benchmark-topic")
  private String benchmarkTopic;

  @Property(name = "kafka-topic.portfolio-topic")
  private String portfolioTopic;

  @Property(name = "kafka-topic.portfolio-request-topic")
  private String portfolioEvaluationRequestTopic;

  @Property(name = "kafka-topic.portfolio-result-topic")
  private String portfolioEvaluationResultTopic;

  @Property(name = "kafka-topic.security-topic")
  private String securityTopic;

  @Property(name = "kafka-topic.trade-topic")
  private String tradeTopic;

  @Property(name = "kafka-topic.trade-result-topic")
  private String tradeEvaluationResultTopic;

  /**
   * Creates a new {@link PanoptesPipelineConfig}. Restricted because this class is managed by
   * Micronaut.
   */
  protected PanoptesPipelineConfig() {
    // nothing to do
  }

  /**
   * Configures and provides a {@link KafkaSource} to source benchmark data from Kafka.
   *
   * @return a {@link KafkaSource}
   */
  @Singleton
  @Named(BENCHMARK_SOURCE)
  protected KafkaSource<PortfolioEvent> benchmarkSource() {
    LOG.info("using {} as benchmark topic", benchmarkTopic);

    return KafkaSource.<PortfolioEvent>builder()
        .setTopics(benchmarkTopic)
        .setDeserializer(new PortfolioEventDeserializationSchema())
        .setProperties(kafkaProperties)
        .setStartingOffsets(OffsetsInitializer.earliest())
        .build();
  }

  /**
   * Configures and provides the Flink execution environment.
   *
   * @return a {@link StreamExecutionEnvironment}
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
    env.getConfig()
        .registerTypeWithKryoSerializer(
            EvaluationResult.class, EvaluationResultKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(Portfolio.class, PortfolioKryoSerializer.class);
    env.getConfig()
        .registerTypeWithKryoSerializer(PortfolioEvent.class, PortfolioEventKryoSerializer.class);
    env.getConfig()
        .registerTypeWithKryoSerializer(PortfolioKey.class, PortfolioKeyKryoSerializer.class);
    env.getConfig()
        .registerTypeWithKryoSerializer(
            PortfolioRuleKey.class, PortfolioRuleKeyKryoSerializer.class);
    env.getConfig()
        .registerTypeWithKryoSerializer(
            PortfolioSummary.class, PortfolioSummaryKryoSerializer.class);
    env.getConfig()
        .registerTypeWithKryoSerializer(
            RuleEvaluationResult.class, RuleEvaluationResultKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(RuleKey.class, RuleKeyKryoSerializer.class);
    // theoretically we should just be able to register a Rule (or maybe ConfigurableRule)
    // serializer, but Flink/Kryo aren't happy unless we register the concrete rule classes
    env.getConfig()
        .registerTypeWithKryoSerializer(ConcentrationRule.class, RuleKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(MarketValueRule.class, RuleKryoSerializer.class);
    env.getConfig()
        .registerTypeWithKryoSerializer(WeightedAverageRule.class, RuleKryoSerializer.class);
    env.getConfig().registerTypeWithKryoSerializer(Security.class, SecurityKryoSerializer.class);
    env.getConfig()
        .registerTypeWithKryoSerializer(SecurityKey.class, SecurityKeyKryoSerializer.class);

    return env;
  }

  /**
   * Configures and provides a {@link KafkaSource} to source portfolio evaluation requests from
   * Kafka.
   *
   * @return a {@link KafkaSource}
   */
  @Singleton
  @Named(PORTFOLIO_EVALUATION_REQUEST_SOURCE)
  protected KafkaSource<PortfolioEvaluationRequest> portfolioEvaluationRequestSource() {
    LOG.info("using {} as portfolioEvaluationRequest topic", portfolioEvaluationRequestTopic);

    return KafkaSource.<PortfolioEvaluationRequest>builder()
        .setTopics(portfolioEvaluationRequestTopic)
        .setDeserializer(new PortfolioEvaluationRequestDeserializationSchema())
        .setProperties(kafkaProperties)
        .build();
  }

  /**
   * Configures and provides a {@link KafkaSink} to publish portfolio evaluation results to Kafka.
   *
   * @return a {@link KafkaSink}
   */
  @Singleton
  @Named(PORTFOLIO_EVALUATION_RESULT_SINK)
  protected KafkaSink<EvaluationResult> portfolioEvaluationResultSink() {
    LOG.info("using {} as portfolioEvaluationResult topic", portfolioEvaluationResultTopic);

    return KafkaSink.<EvaluationResult>builder()
        .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
        .setKafkaProducerConfig(kafkaProperties)
        .setRecordSerializer(
            new EvaluationResultSerializationSchema(portfolioEvaluationResultTopic))
        .build();
  }

  /**
   * Configures and provides a {@link KafkaSource} to source portfolio data from Kafka.
   *
   * @return a {@link KafkaSource}
   */
  @Singleton
  @Named(PORTFOLIO_SOURCE)
  protected KafkaSource<PortfolioEvent> portfolioSource() {
    LOG.info("using {} as portfolio topic", portfolioTopic);

    return KafkaSource.<PortfolioEvent>builder()
        .setTopics(portfolioTopic)
        .setDeserializer(new PortfolioEventDeserializationSchema())
        .setProperties(kafkaProperties)
        .setStartingOffsets(OffsetsInitializer.earliest())
        .build();
  }

  /**
   * Configures and provides a {@link KafkaSource} to source security data from Kafka.
   *
   * @return a {@link KafkaSource}
   */
  @Singleton
  @Named(SECURITY_SOURCE)
  protected KafkaSource<Security> securitySource() {
    LOG.info("using {} as security topic", securityTopic);

    return KafkaSource.<Security>builder()
        .setTopics(securityTopic)
        .setDeserializer(new SecurityDeserializationSchema())
        .setProperties(kafkaProperties)
        .setStartingOffsets(OffsetsInitializer.earliest())
        .build();
  }

  /**
   * Configures and provides a {@link KafkaSink} to publish trade evaluation results to Kafka.
   *
   * @return a {@link KafkaSink}
   */
  @Singleton
  @Named(TRADE_EVALUATION_RESULT_SINK)
  protected KafkaSink<TradeEvaluationResult> tradeEvaluationResultSink() {
    LOG.info("using {} as tradeEvaluationResult topic", tradeEvaluationResultTopic);

    return KafkaSink.<TradeEvaluationResult>builder()
        .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
        .setKafkaProducerConfig(kafkaProperties)
        .setRecordSerializer(
            new TradeEvaluationResultSerializationSchema(tradeEvaluationResultTopic))
        .build();
  }

  /**
   * Configures and provides a {@link KafkaSource} to source trades from Kafka.
   *
   * @return a {@link KafkaSource}
   */
  @Singleton
  @Named(TRADE_SOURCE)
  protected KafkaSource<Trade> tradeSource() {
    LOG.info("using {} as trade topic", tradeTopic);

    return KafkaSource.<Trade>builder()
        .setTopics(tradeTopic)
        .setDeserializer(new TradeDeserializationSchema())
        .setProperties(kafkaProperties)
        .build();
  }
}
