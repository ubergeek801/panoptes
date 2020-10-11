package org.slaq.slaqworx.panoptes.pipeline;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
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
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.rule.Rule;
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
    public static final TypeInformation<
            Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>> PORTFOLIO_RESULT_TYPE_INFO =
                    TypeInformation.of(new TypeHint<
                            Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>>() {
                        // trivial
                    });

    public static final MapStateDescriptor<SecurityKey, Security> SECURITY_STATE_DESCRIPTOR =
            new MapStateDescriptor<>("securityBroadcast", SECURITY_KEY_TYPE_INFO,
                    SECURITY_TYPE_INFO);
    public static final MapStateDescriptor<PortfolioRuleKey,
            EvaluationResult> BENCHMARK_RESULT_STATE_DESCRIPTOR = new MapStateDescriptor<>(
                    "benchmarkResultBroadcast", PortfolioRuleKey.class, EvaluationResult.class);

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

        // obtain portfolios from Kafka and feed into a rule evaluator
        SingleOutputStreamOperator<Portfolio> portfolioSource =
                env.addSource(portfolioKafkaSource).name("portfolioSource").uid("portfolioSource");
        SingleOutputStreamOperator<
                Tuple4<PortfolioKey, PortfolioKey, Rule, EvaluationResult>> portfolioResultStream =
                        portfolioSource.keyBy(Portfolio::getKey).connect(securityStream)
                                .process(new PortfolioRuleEvaluator()).name("portfolioEvaluator")
                                .uid("portfolioEvaluator").returns(PORTFOLIO_RESULT_TYPE_INFO);

        // obtain benchmarks from Kafka, merge them with the portfolio stream (filtered to include
        // only portfolios that specify benchmarks), and feed into a benchmark rule evaluator
        DataStream<Portfolio> benchmarkSource =
                env.addSource(benchmarkKafkaSource).name("benchmarkSource").uid("benchmarkSource")
                        .union(portfolioSource.filter(p -> p.getBenchmarkKey() != null)
                                .name("portfoliosWithBenchmarks").uid("portfoliosWithBenchmarks"));
        BroadcastStream<Tuple2<PortfolioKey, EvaluationResult>> benchmarkResultStream =
                benchmarkSource
                        .keyBy(p -> p.getBenchmarkKey() != null ? p.getBenchmarkKey() : p.getKey())
                        .connect(securityStream).process(new BenchmarkRuleEvaluator())
                        .name("benchmarkEvaluator").uid("benchmarkEvaluator")
                        .returns(PORTFOLIO_RESULT_TYPE_INFO).map(t -> Tuple2.of(t.f0, t.f3))
                        .returns(TypeInformation
                                .of(new TypeHint<Tuple2<PortfolioKey, EvaluationResult>>() {
                                    // trivial
                                }))
                        .name("benchmarkResultMapper").uid("benchmarkResultMapper")
                        .broadcast(BENCHMARK_RESULT_STATE_DESCRIPTOR);
        // feed the rule evaluation results into a benchmark comparator
        SingleOutputStreamOperator<EvaluationResult> resultStream = portfolioResultStream
                .keyBy(t -> t.f0).connect(benchmarkResultStream).process(new BenchmarkComparator())
                .name("benchmarkComparator").uid("benchmarkComparator");

        resultStream.addSink(new EvaluationResultPublisher()).name("evaluationResultSink")
                .uid("evaluationResultSink");

        LOG.info("initialized pipeline");

        env.execute("PanoptesPipeline");
    }
}
