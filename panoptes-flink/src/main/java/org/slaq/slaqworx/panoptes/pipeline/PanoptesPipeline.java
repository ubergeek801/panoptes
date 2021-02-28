package org.slaq.slaqworx.panoptes.pipeline;

import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Flink pipeline which realizes the Panoptes event processing logic.
 *
 * @author jeremy
 */
@Singleton
public class PanoptesPipeline {
  public static final TypeInformation<SecurityKey> SECURITY_KEY_TYPE_INFO =
      TypeInformation.of(new TypeHint<>() {
        // trivial
      });
  public static final TypeInformation<Security> SECURITY_TYPE_INFO =
      TypeInformation.of(new TypeHint<>() {
        // trivial
      });
  public static final MapStateDescriptor<SecurityKey, Security> SECURITY_STATE_DESCRIPTOR =
      new MapStateDescriptor<>("securityBroadcast", SECURITY_KEY_TYPE_INFO, SECURITY_TYPE_INFO);
  private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipeline.class);
  private final SourceFunction<PortfolioEvent> benchmarkKafkaSource;
  private final SourceFunction<PortfolioEvent> portfolioKafkaSource;
  private final SourceFunction<PortfolioEvaluationRequest> portfolioRequestSource;
  private final SinkFunction<EvaluationResult> portfolioResultSink;
  private final SourceFunction<Security> securityKafkaSource;
  private final SourceFunction<Trade> tradeKafkaSource;
  private final SinkFunction<TradeEvaluationResult> tradeResultSink;

  private final StreamExecutionEnvironment env;

  /**
   * Creates a new {@link PanoptesPipeline} using the given resources. Restricted because this class
   * should be instantiated through Micronaut.
   *
   * @param benchmarkKafkaSource
   *     a {@link SourceFunction} producing benchmark-related events
   * @param portfolioKafkaSource
   *     a {@link SourceFunction} producing portfolio-related events
   * @param portfolioRequestSource
   *     a {@link SourceFunction} producing portfolio evaluation requests
   * @param portfolioResultSink
   *     a {@link SinkFunction} consuming portfolio evaluation results
   * @param securityKafkaSource
   *     a {@link SourceFunction} producing security-related events
   * @param tradeSource
   *     a {@link SourceFunction} producing trade evaluation requests
   * @param tradeResultSink
   *     a {@link SinkFunction} consuming trade evaluation results
   * @param flinkEnvironment
   *     the Flink execution environment to use
   */
  protected PanoptesPipeline(@Named(PanoptesPipelineConfig.BENCHMARK_SOURCE)
      SourceFunction<PortfolioEvent> benchmarkKafkaSource,
      @Named(PanoptesPipelineConfig.PORTFOLIO_SOURCE)
          SourceFunction<PortfolioEvent> portfolioKafkaSource,
      @Named(PanoptesPipelineConfig.PORTFOLIO_EVALUATION_REQUEST_SOURCE)
          SourceFunction<PortfolioEvaluationRequest> portfolioRequestSource,
      @Named(PanoptesPipelineConfig.PORTFOLIO_EVALUATION_RESULT_SINK)
          SinkFunction<EvaluationResult> portfolioResultSink,
      @Named(PanoptesPipelineConfig.SECURITY_SOURCE) SourceFunction<Security> securityKafkaSource,
      @Named(PanoptesPipelineConfig.TRADE_SOURCE) SourceFunction<Trade> tradeSource,
      @Named(PanoptesPipelineConfig.TRADE_EVALUATION_RESULT_SINK)
          SinkFunction<TradeEvaluationResult> tradeResultSink,
      StreamExecutionEnvironment flinkEnvironment) {
    this.benchmarkKafkaSource = benchmarkKafkaSource;
    this.portfolioKafkaSource = portfolioKafkaSource;
    this.portfolioRequestSource = portfolioRequestSource;
    this.portfolioResultSink = portfolioResultSink;
    this.securityKafkaSource = securityKafkaSource;
    tradeKafkaSource = tradeSource;
    this.tradeResultSink = tradeResultSink;
    env = flinkEnvironment;
  }

  /**
   * Creates the Panoptes application pipeline.
   *
   * @param args
   *     the arguments with which to initialize the pipeline
   *
   * @throws Exception
   *     if the pipeline could not be created
   */
  public void create(String... args) throws Exception {
    LOG.info("initializing pipeline");

    // obtain securities from Kafka and broadcast
    SingleOutputStreamOperator<Security> securitySource =
        env.addSource(securityKafkaSource).name("securitySource").uid("securitySource");
    BroadcastStream<Security> securityStream = securitySource.broadcast(SECURITY_STATE_DESCRIPTOR);

    // obtain trades from Kafka
    SingleOutputStreamOperator<Trade> tradeSource =
        env.addSource(tradeKafkaSource).name("tradeSource").uid("tradeSource");
    // split each trade into its constituent transactions
    KeyedStream<PortfolioEvent, PortfolioKey> transactionStream =
        tradeSource.flatMap(new TradeSplitter()).name("tradeTransactions").uid("tradeTransactions")
            .keyBy(PortfolioEvent::getPortfolioKey);

    // obtain portfolio (event)s from Kafka, union with transaction events, connect with
    // securities and feed into a portfolio rule evaluator
    SingleOutputStreamOperator<PortfolioEvent> portfolioSource =
        env.addSource(portfolioKafkaSource).name("portfolioSource").uid("portfolioSource");
    SingleOutputStreamOperator<RuleEvaluationResult> portfolioResultStream =
        portfolioSource.union(transactionStream).keyBy(PortfolioEvent::getPortfolioKey)
            .connect(securityStream).process(new PortfolioRuleEvaluator())
            .name("portfolioEvaluator").uid("portfolioEvaluator");

    // obtain benchmarks from Kafka, union them with the portfolio stream, and feed into a
    // benchmark rule evaluator (this evaluator only evaluates benchmarks, but collects rules
    // from the non-benchmark portfolios)
    DataStream<PortfolioEvent> benchmarkSource =
        env.addSource(benchmarkKafkaSource).name("benchmarkSource").uid("benchmarkSource")
            .union(portfolioSource);
    SingleOutputStreamOperator<RuleEvaluationResult> benchmarkResultStream = benchmarkSource
        .keyBy(p -> p.getBenchmarkKey() != null ? p.getBenchmarkKey() : p.getPortfolioKey())
        .connect(securityStream).process(new BenchmarkRuleEvaluator()).name("benchmarkEvaluator")
        .uid("benchmarkEvaluator");

    // feed the rule evaluation results (keyed by benchmark ID + rule ID) and benchmark
    // evaluation results (keyed by portfolio ID, which *is* the benchmark ID for a benchmark, +
    // rule ID) into a benchmark comparator
    SingleOutputStreamOperator<RuleEvaluationResult> resultStream =
        portfolioResultStream.keyBy(RuleEvaluationResult::getBenchmarkEvaluationKey)
            .connect(benchmarkResultStream.keyBy(RuleEvaluationResult::getBenchmarkEvaluationKey))
            .process(new BenchmarkComparator()).name("benchmarkComparator")
            .uid("benchmarkComparator");

    resultStream.addSink(new EvaluationResultPublisher()).name("evaluationResultSink")
        .uid("evaluationResultSink");

    LOG.info("initialized pipeline");

    env.execute("PanoptesPipeline");
  }
}
