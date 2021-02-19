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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.serializer.PortfolioEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer;
import org.slaq.slaqworx.panoptes.serializer.SecuritySerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeSerializer;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;

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

        Properties benchmarkSourceProperties = new Properties(kafkaProperties);
        benchmarkSourceProperties.setProperty("auto.offset.reset", "earliest");

        return KafkaSources.kafka(benchmarkSourceProperties,
                (r -> new PortfolioEventSerializer().read((byte[])r.value())), benchmarkTopic);
    }

    /**
     * Configures and provides the Jet job configuration.
     *
     * @return a {@code JobConfig}
     */
    @Singleton
    protected JobConfig jobConfig() {
        JobConfig jobConfig = new JobConfig();

        // register serializers here if necessary

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

        Properties portfolioEvaluationRequestSourceProperties = new Properties(kafkaProperties);
        portfolioEvaluationRequestSourceProperties.setProperty("auto.offset.reset", "earliest");

        return KafkaSources.kafka(portfolioEvaluationRequestSourceProperties,
                (r -> new PortfolioEvaluationRequestSerializer(assetCacheProvider)
                        .read((byte[])r.value())),
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

        Properties portfolioEvaluationResultSinkProperties = new Properties(kafkaProperties);

        return KafkaSinks.kafka(portfolioEvaluationResultSinkProperties,
                portfolioEvaluationResultTopic, EvaluationResult::getKey, r -> r);
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

        Properties portfolioSourceProperties = new Properties(kafkaProperties);
        portfolioSourceProperties.setProperty("auto.offset.reset", "earliest");

        return KafkaSources.kafka(portfolioSourceProperties,
                (r -> new PortfolioEventSerializer().read((byte[])r.value())), portfolioTopic);
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

        Properties securitySourceProperties = new Properties(kafkaProperties);
        securitySourceProperties.setProperty("auto.offset.reset", "earliest");

        return KafkaSources.kafka(securitySourceProperties,
                (r -> new SecuritySerializer().read((byte[])r.value())), securityTopic);
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

        Properties tradeEvaluationResultSinkProperties = new Properties(kafkaProperties);

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

        Properties tradeSourceProperties = new Properties(kafkaProperties);
        tradeSourceProperties.setProperty("auto.offset.reset", "earliest");

        return KafkaSources.kafka(tradeSourceProperties,
                (r -> new TradeSerializer().read((byte[])r.value())), tradeTopic);
    }
}
