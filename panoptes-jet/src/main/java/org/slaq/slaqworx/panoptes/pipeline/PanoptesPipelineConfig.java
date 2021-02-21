package org.slaq.slaqworx.panoptes.pipeline;

import java.util.Properties;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.StreamSource;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.cache.PortfolioSummarizer;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleSummary;
import org.slaq.slaqworx.panoptes.serializer.kafka.EvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.serializer.kafka.PortfolioEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.kafka.PortfolioEventSerializer;
import org.slaq.slaqworx.panoptes.serializer.kafka.PortfolioKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.kafka.RuleKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.kafka.SecurityKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.kafka.SecuritySerializer;
import org.slaq.slaqworx.panoptes.serializer.kafka.TradeEvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.serializer.kafka.TradeKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.kafka.TradeSerializer;
import org.slaq.slaqworx.panoptes.trade.RoomEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * A Micronaut {@code Factory} which configures and provides various beans to the
 * {@code ApplicationContext}.
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

    @Property(name = "kafka") private Properties kafkaProperties;
    @Property(name = "kafka-topic.benchmark-topic") private String benchmarkTopic;
    @Property(name = "kafka-topic.portfolio-topic") private String portfolioTopic;
    @Property(
            name = "kafka-topic.portfolio-request-topic") private String portfolioEvaluationRequestTopic;
    @Property(
            name = "kafka-topic.portfolio-result-topic") private String portfolioEvaluationResultTopic;
    @Property(name = "kafka-topic.security-topic") private String securityTopic;
    @Property(name = "kafka-topic.trade-topic") private String tradeTopic;
    @Property(name = "kafka-topic.trade-result-topic") private String tradeEvaluationResultTopic;

    /**
     * Creates a new {@code PanoptesPipelineConfig}. Restricted because this class is managed by
     * Micronaut.
     */
    protected PanoptesPipelineConfig() {
        // nothing to do
    }

    /**
     * Configures and provides a {@code StreamSource} to source benchmark data from Kafka.
     *
     * @return a {@code StreamSource}
     */
    @Singleton
    @Named(BENCHMARK_SOURCE)
    protected StreamSource<PortfolioEvent> benchmarkSource() {
        LOG.info("using {} as benchmark topic", benchmarkTopic);

        Properties benchmarkSourceProperties = new Properties();
        benchmarkSourceProperties.putAll(kafkaProperties);
        benchmarkSourceProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        benchmarkSourceProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                PortfolioKeySerializer.class.getCanonicalName());
        benchmarkSourceProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                PortfolioEventSerializer.class.getCanonicalName());

        return KafkaSources.kafka(benchmarkSourceProperties,
                ConsumerRecord<PortfolioKey, PortfolioEvent>::value, benchmarkTopic);
    }

    /**
     * Configures and provides a {@code JobConfig} suitable for executing the Panoptes pipeline.
     *
     * @return a {@code JobConfig}
     */
    @Singleton
    protected JobConfig jobConfig() {
        JobConfig jobConfig = new JobConfig();

        jobConfig.registerSerializer(EvaluationContext.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.EvaluationContextSerializer.class);
        jobConfig.registerSerializer(Portfolio.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.PortfolioSerializer.class);
        jobConfig.registerSerializer(PortfolioEvent.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.PortfolioEventSerializer.class);
        jobConfig.registerSerializer(PortfolioKey.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.PortfolioKeySerializer.class);
        jobConfig.registerSerializer(PortfolioSummarizer.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.PortfolioSummarizerSerializer.class);
        jobConfig.registerSerializer(PortfolioSummary.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.PortfolioSummarySerializer.class);
        jobConfig.registerSerializer(PositionKey.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.PositionKeySerializer.class);
        jobConfig.registerSerializer(Position.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.PositionSerializer.class);
        jobConfig.registerSerializer(RoomEvaluationRequest.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.RoomEvaluationRequestSerializer.class);
        jobConfig.registerSerializer(RuleKey.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.RuleKeySerializer.class);
        jobConfig.registerSerializer(ConfigurableRule.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.RuleSerializer.class);
        jobConfig.registerSerializer(RuleSummary.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.RuleSummarySerializer.class);
        jobConfig.registerSerializer(SecurityKey.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.SecurityKeySerializer.class);
        jobConfig.registerSerializer(Security.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.SecuritySerializer.class);
        jobConfig.registerSerializer(TradeEvaluationRequest.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.TradeEvaluationRequestSerializer.class);
        jobConfig.registerSerializer(TradeKey.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.TradeKeySerializer.class);
        jobConfig.registerSerializer(Trade.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.TradeSerializer.class);
        jobConfig.registerSerializer(TransactionKey.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.TransactionKeySerializer.class);
        jobConfig.registerSerializer(Transaction.class,
                org.slaq.slaqworx.panoptes.serializer.hazelcast.TransactionSerializer.class);

        return jobConfig;
    }

    /**
     * Configures and provides a {@code StreamSource} to source portfolio evaluation requests from
     * Kafka.
     *
     * @param assetCacheProvider
     *            a {@code Provider} of an {@code AssetCache}
     * @return a {@code StreamSource}
     */
    @Singleton
    @Named(PORTFOLIO_EVALUATION_REQUEST_SOURCE)
    protected StreamSource<PortfolioEvaluationRequest>
            portfolioEvaluationRequestSource(Provider<AssetCache> assetCacheProvider) {
        LOG.info("using {} as portfolioEvaluationRequest topic", portfolioEvaluationRequestTopic);

        Properties portfolioEvaluationRequestSourceProperties = new Properties();
        portfolioEvaluationRequestSourceProperties.putAll(kafkaProperties);
        portfolioEvaluationRequestSourceProperties
                .setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        portfolioEvaluationRequestSourceProperties.setProperty(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                PortfolioKeySerializer.class.getCanonicalName());
        portfolioEvaluationRequestSourceProperties.setProperty(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                PortfolioEvaluationRequestSerializer.class.getCanonicalName());

        return KafkaSources.kafka(portfolioEvaluationRequestSourceProperties,
                ConsumerRecord<PortfolioKey, PortfolioEvaluationRequest>::value,
                portfolioEvaluationRequestTopic);
    }

    /**
     * Configures and provides a {@code Sink} to publish portfolio evaluation results to Kafka.
     *
     * @return a {@code Sink}
     */
    @Singleton
    @Named(PORTFOLIO_EVALUATION_RESULT_SINK)
    protected Sink<EvaluationResult> portfolioEvaluationResultSink() {
        LOG.info("using {} as portfolioEvaluationResult topic", portfolioEvaluationResultTopic);

        Properties portfolioEvaluationResultSinkProperties = new Properties();
        portfolioEvaluationResultSinkProperties.putAll(kafkaProperties);
        portfolioEvaluationResultSinkProperties.setProperty(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                RuleKeySerializer.class.getCanonicalName());
        portfolioEvaluationResultSinkProperties.setProperty(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                EvaluationResultSerializer.class.getCanonicalName());

        return KafkaSinks.kafka(portfolioEvaluationResultSinkProperties,
                portfolioEvaluationResultTopic, EvaluationResult::getRuleKey, r -> r);
    }

    /**
     * Configures and provides a {@code StreamSource} to source portfolio data from Kafka.
     *
     * @return a {@code StreamSource}
     */
    @Singleton
    @Named(PORTFOLIO_SOURCE)
    protected StreamSource<PortfolioEvent> portfolioSource() {
        LOG.info("using {} as portfolio topic", portfolioTopic);

        Properties portfolioSourceProperties = new Properties();
        portfolioSourceProperties.putAll(kafkaProperties);
        portfolioSourceProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        portfolioSourceProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                PortfolioKeySerializer.class.getCanonicalName());
        portfolioSourceProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                PortfolioEventSerializer.class.getCanonicalName());

        return KafkaSources.kafka(portfolioSourceProperties,
                ConsumerRecord<PortfolioKey, PortfolioEvent>::value, portfolioTopic);
    }

    /**
     * Configures and provides a {@code StreamSource} to source security data from Kafka.
     *
     * @return a {@code StreamSource}
     */
    @Singleton
    @Named(SECURITY_SOURCE)
    protected StreamSource<Security> securitySource() {
        LOG.info("using {} as security topic", securityTopic);

        LOG.info("using base properties {}", kafkaProperties);

        Properties securitySourceProperties = new Properties();
        securitySourceProperties.putAll(kafkaProperties);
        securitySourceProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        securitySourceProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                SecurityKeySerializer.class.getCanonicalName());
        securitySourceProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                SecuritySerializer.class.getCanonicalName());

        return KafkaSources.kafka(securitySourceProperties,
                ConsumerRecord<SecurityKey, Security>::value, securityTopic);
    }

    /**
     * Configures and provides a {@code Sink} to publish trade evaluation results to Kafka.
     *
     * @return a {@code Sink}
     */
    @Singleton
    @Named(TRADE_EVALUATION_RESULT_SINK)
    protected Sink<TradeEvaluationResult> tradeEvaluationResultSink() {
        LOG.info("using {} as tradeEvaluationResult topic", tradeEvaluationResultTopic);

        Properties tradeEvaluationResultSinkProperties = new Properties();
        tradeEvaluationResultSinkProperties.putAll(kafkaProperties);
        tradeEvaluationResultSinkProperties.setProperty(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                TradeEvaluationResultSerializer.class.getCanonicalName());

        return KafkaSinks.kafka(tradeEvaluationResultSinkProperties, tradeEvaluationResultTopic,
                r -> null, r -> r);
    }

    /**
     * Configures and provides a {@code StreamSource} to source trades from Kafka.
     *
     * @return a {@code StreamSource}
     */
    @Singleton
    @Named(TRADE_SOURCE)
    protected StreamSource<Trade> tradeSource() {
        LOG.info("using {} as trade topic", tradeTopic);

        Properties tradeSourceProperties = new Properties();
        tradeSourceProperties.putAll(kafkaProperties);
        tradeSourceProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        tradeSourceProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                TradeKeySerializer.class.getCanonicalName());
        tradeSourceProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                TradeSerializer.class.getCanonicalName());

        return KafkaSources.kafka(tradeSourceProperties, ConsumerRecord<TradeKey, Trade>::value,
                tradeTopic);
    }
}
