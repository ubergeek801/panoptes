package org.slaq.slaqworx.panoptes.pipeline;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;

@Singleton
public class PanoptesPipeline {
    private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipeline.class);

    public static final TypeInformation<SecurityKey> SECURITY_KEY_TYPE_INFO =
            TypeInformation.of(new TypeHint<SecurityKey>() {
                // trivial
            });
    public static final TypeInformation<Security> SECURITY_TYPE_INFO =
            TypeInformation.of(new TypeHint<Security>() {
                // trivial
            });
    public static final MapStateDescriptor<SecurityKey, Security> SECURITY_STATE_DESCRIPTOR =
            new MapStateDescriptor<>("securityBroadcast", SECURITY_KEY_TYPE_INFO,
                    SECURITY_TYPE_INFO);

    private final SourceFunction<Portfolio> benchmarkKafkaSource;
    private final SourceFunction<Portfolio> portfolioKafkaSource;
    private final SourceFunction<PortfolioEvaluationRequest> portfolioRequestSource;
    private final SinkFunction<EvaluationResult> portfolioResultSink;
    private final SourceFunction<Security> securityKafkaSource;
    private final SourceFunction<TradeEvaluationRequest> tradeRequestSource;
    private final SinkFunction<TradeEvaluationResult> tradeResultSink;

    private final StreamExecutionEnvironment env;

    protected PanoptesPipeline(
            @Named(PanoptesPipelineConfig.BENCHMARK_SOURCE) SourceFunction<
                    Portfolio> benchmarkKafkaSource,
            @Named(PanoptesPipelineConfig.PORTFOLIO_SOURCE) SourceFunction<
                    Portfolio> portfolioKafkaSource,
            @Named(PanoptesPipelineConfig.PORTFOLIO_EVALUATION_REQUEST_SOURCE) SourceFunction<
                    PortfolioEvaluationRequest> portfolioRequestSource,
            @Named(PanoptesPipelineConfig.PORTFOLIO_EVALUATION_RESULT_SINK) SinkFunction<
                    EvaluationResult> portfolioResultSink,
            @Named(PanoptesPipelineConfig.SECURITY_SOURCE) SourceFunction<
                    Security> securityKafkaSource,
            @Named(PanoptesPipelineConfig.TRADE_EVALUATION_REQUEST_SOURCE) SourceFunction<
                    TradeEvaluationRequest> tradeRequestSource,
            @Named(PanoptesPipelineConfig.TRADE_EVALUATION_RESULT_SINK) SinkFunction<
                    TradeEvaluationResult> tradeResultSink,
            StreamExecutionEnvironment flinkEnvironment) {
        this.benchmarkKafkaSource = benchmarkKafkaSource;
        this.portfolioKafkaSource = portfolioKafkaSource;
        this.portfolioRequestSource = portfolioRequestSource;
        this.portfolioResultSink = portfolioResultSink;
        this.securityKafkaSource = securityKafkaSource;
        this.tradeRequestSource = tradeRequestSource;
        this.tradeResultSink = tradeResultSink;
        env = flinkEnvironment;
    }

    public void execute(String... args) throws Exception {
        LOG.info("initializing pipeline");

        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        // obtain securities from Kafka and broadcast
        SingleOutputStreamOperator<Security> securitySource =
                env.addSource(securityKafkaSource).name("securitySource").uid("securitySource");
        BroadcastStream<Security> securityStream =
                securitySource.broadcast(SECURITY_STATE_DESCRIPTOR);

        // obtain portfolios and benchmarks from Kafka, and combine into a single stream
        SingleOutputStreamOperator<Portfolio> portfolioSource =
                env.addSource(portfolioKafkaSource).name("portfolioSource").uid("portfolioSource");
        SingleOutputStreamOperator<Portfolio> benchmarkSource =
                env.addSource(benchmarkKafkaSource).name("benchmarkSource").uid("benchmarkSource");
        DataStream<Portfolio> unifiedPortfolioStream = portfolioSource.union(benchmarkSource);

        // connect the portfolio stream to the security broadcast, and feed into a rule evaluator
        SingleOutputStreamOperator<EvaluationResult> resultStream =
                unifiedPortfolioStream.keyBy(Portfolio::getKey).connect(securityStream)
                        .process(new PortfolioRuleEvaluator()).name("portfolioEvaluator")
                        .uid("portfolioEvaluator");

        resultStream.addSink(new EvaluationResultPublisher()).name("evaluationResultSink")
                .uid("evaluationResultSink");

        LOG.info("initialized pipeline");

        env.execute("PanoptesPipeline");

        LOG.info("pipeline terminated");
    }
}
