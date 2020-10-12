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

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
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

    private final SourceFunction<PortfolioEvent> benchmarkKafkaSource;
    private final SourceFunction<PortfolioEvent> portfolioKafkaSource;
    private final SourceFunction<PortfolioEvaluationRequest> portfolioRequestSource;
    private final SinkFunction<EvaluationResult> portfolioResultSink;
    private final SourceFunction<Security> securityKafkaSource;
    private final SourceFunction<TradeEvaluationRequest> tradeRequestSource;
    private final SinkFunction<TradeEvaluationResult> tradeResultSink;

    private final StreamExecutionEnvironment env;

    protected PanoptesPipeline(
            @Named(PanoptesPipelineConfig.BENCHMARK_SOURCE) SourceFunction<
                    PortfolioEvent> benchmarkKafkaSource,
            @Named(PanoptesPipelineConfig.PORTFOLIO_SOURCE) SourceFunction<
                    PortfolioEvent> portfolioKafkaSource,
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

        // obtain portfolios from Kafka and feed into a rule evaluator
        SingleOutputStreamOperator<PortfolioEvent> portfolioSource =
                env.addSource(portfolioKafkaSource).name("portfolioSource").uid("portfolioSource");
        SingleOutputStreamOperator<RuleEvaluationResult> portfolioResultStream =
                portfolioSource.keyBy(PortfolioEvent::getPortfolioKey).connect(securityStream)
                        .process(new PortfolioRuleEvaluator()).name("portfolioEvaluator")
                        .uid("portfolioEvaluator");

        // obtain benchmarks from Kafka, merge them with the portfolio stream, and feed into a
        // benchmark rule evaluator
        DataStream<PortfolioEvent> benchmarkSource = env.addSource(benchmarkKafkaSource)
                .name("benchmarkSource").uid("benchmarkSource").union(portfolioSource);
        SingleOutputStreamOperator<RuleEvaluationResult> benchmarkResultStream = benchmarkSource
                .keyBy(p -> p.getBenchmarkKey() != null ? p.getBenchmarkKey() : p.getPortfolioKey())
                .connect(securityStream).process(new BenchmarkRuleEvaluator())
                .name("benchmarkEvaluator").uid("benchmarkEvaluator");

        // feed the rule evaluation results (keyed by benchmark ID) and benchmark evaluation results
        // (keyed by portfolio ID which *is* the benchmark ID for a benchmark) into a benchmark
        // comparator
        SingleOutputStreamOperator<EvaluationResult> resultStream =
                portfolioResultStream.keyBy(RuleEvaluationResult::getFlinkSafeBenchmarkKey)
                        .connect(benchmarkResultStream.keyBy(RuleEvaluationResult::getPortfolioKey))
                        .process(new BenchmarkComparator()).name("benchmarkComparator")
                        .uid("benchmarkComparator");

        resultStream.addSink(new EvaluationResultPublisher()).name("evaluationResultSink")
                .uid("evaluationResultSink");

        LOG.info("initialized pipeline");

        env.execute("PanoptesPipeline");
    }
}
