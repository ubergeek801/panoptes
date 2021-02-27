package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.SinkBuilder;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.event.SecurityUpdateEvent;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a Hazelcast Jet pipeline which realizes the Panoptes event processing logic.
 *
 * @author jeremy
 */
@Singleton
public class PanoptesPipeline {
  private static final Logger LOG = LoggerFactory.getLogger(PanoptesPipeline.class);

  private final Pipeline pipeline;

  /**
   * Creates a new {@code PanoptesPipeline} using the given resources. Restricted because this
   * class
   * should be instantiated through Micronaut.
   *
   * @param benchmarkKafkaSource
   *     a {@code StreamSource} producing benchmark-related events
   * @param portfolioKafkaSource
   *     a {@code StreamSource} producing portfolio-related events
   * @param portfolioRequestSource
   *     a {@code StreamSource} producing portfolio evaluation requests
   * @param portfolioResultSink
   *     a {@code Sink} consuming portfolio evaluation results
   * @param securityKafkaSource
   *     a {@code StreamSource} producing security-related events
   * @param tradeKafkaSource
   *     a {@code StreamSource} producing trade evaluation requests
   * @param tradeResultSink
   *     a {@code Sink} consuming trade evaluation results
   */
  protected PanoptesPipeline(
      @Named(PanoptesPipelineConfig.BENCHMARK_SOURCE) StreamSource<
          PortfolioEvent> benchmarkKafkaSource,
      @Named(PanoptesPipelineConfig.PORTFOLIO_SOURCE) StreamSource<
          PortfolioEvent> portfolioKafkaSource,
      @Named(PanoptesPipelineConfig.PORTFOLIO_EVALUATION_REQUEST_SOURCE) StreamSource<
          PortfolioEvaluationRequest> portfolioRequestSource,
      @Named(PanoptesPipelineConfig.PORTFOLIO_EVALUATION_RESULT_SINK) Sink<
          RuleEvaluationResult> portfolioResultSink,
      @Named(PanoptesPipelineConfig.SECURITY_SOURCE) StreamSource<
          Security> securityKafkaSource,
      @Named(PanoptesPipelineConfig.TRADE_SOURCE) StreamSource<Trade> tradeKafkaSource,
      @Named(PanoptesPipelineConfig.TRADE_EVALUATION_RESULT_SINK) Sink<
          TradeEvaluationResult> tradeResultSink) {
    LOG.info("initializing pipeline");

    pipeline = Pipeline.create();

    // obtain securities from Kafka and put to the Security map, look up portfolios holding each
    // security and emit a HeldSecurityEvent for each
    StreamStage<Security> securitySource = pipeline.readFrom(securityKafkaSource)
        .withIngestionTimestamps().setName("securitySource");
    StreamStage<SecurityUpdateEvent> securityUpdateStream =
        securitySource.flatMap(new SecurityBroadcaster()).setName("securityUpdates");

    // obtain trades from Kafka and split each into its constituent transactions
    StreamStage<Trade> tradeSource = pipeline.readFrom(tradeKafkaSource)
        .withIngestionTimestamps().setName("tradeSource");
    StreamStage<PortfolioEvent> transactionStream =
        tradeSource.flatMap(new TradeSplitter()).setName("tradeTransactions");

    // obtain portfolio (event)s from Kafka, merge with holding portfolio events, merge with
    // transaction events, and feed into a portfolio rule evaluator
    StreamStage<PortfolioEvent> portfolioSource = pipeline.readFrom(portfolioKafkaSource)
        .withIngestionTimestamps().setName("portfolioSource");
    PortfolioRuleEvaluator portfolioRuleEvaluator = new PortfolioRuleEvaluator();
    StreamStage<RuleEvaluationResult> portfolioResultStream =
        portfolioSource.merge(securityUpdateStream).merge(transactionStream)
            .groupingKey(PortfolioEvent::getPortfolioKey)
            .flatMapStateful(portfolioRuleEvaluator, portfolioRuleEvaluator)
            .setName("portfolioEvaluator");

    // obtain benchmarks from Kafka, merge them with the portfolio stream, merge with holding
    // portfolio events, and feed into a benchmark rule evaluator (this evaluator only evaluates
    // benchmarks, but collects rules from the non-benchmark portfolios)
    StreamStage<PortfolioEvent> benchmarkSource = pipeline.readFrom(benchmarkKafkaSource)
        .withIngestionTimestamps().setName("benchmarkSource").merge(portfolioSource);
    BenchmarkRuleEvaluator benchmarkRuleEvaluator = new BenchmarkRuleEvaluator();
    StreamStage<RuleEvaluationResult> benchmarkResultStream =
        benchmarkSource.merge(securityUpdateStream)
            .groupingKey(p -> p.getBenchmarkKey() != null ? p.getBenchmarkKey()
                : p.getPortfolioKey())
            .flatMapStateful(benchmarkRuleEvaluator, benchmarkRuleEvaluator)
            .setName("benchmarkEvaluator");

    // feed the rule evaluation results (keyed by benchmark ID + rule ID) and benchmark
    // evaluation results (keyed by portfolio ID, which *is* the benchmark ID for a benchmark, +
    // rule ID) into a benchmark comparator
    BenchmarkComparator benchmarkComparator = new BenchmarkComparator();
    StreamStage<RuleEvaluationResult> resultStream =
        portfolioResultStream.merge(benchmarkResultStream)
            .groupingKey(RuleEvaluationResult::getBenchmarkEvaluationKey)
            .flatMapStateful(benchmarkComparator, benchmarkComparator)
            .setName("benchmarkComparator");

    resultStream.writeTo(portfolioResultSink);
    resultStream.writeTo(SinkBuilder.sinkBuilder("evaluationResultSink", c -> c)
        .receiveFn(new EvaluationResultPublisher()).build());

    LOG.info("initialized pipeline");
  }

  /**
   * Obtains the Jet {@code Pipeline} corresponding to this {@code PanoptesPipeline}.
   *
   * @return a Jet {@code Pipeline}
   */
  public Pipeline getJetPipeline() {
    return pipeline;
  }
}
