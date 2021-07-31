package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.SinkBuilder;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.stream.Collectors;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioEvaluationInput;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.util.Keyed;
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
   * Creates a new {@link PanoptesPipeline} using the given resources. Restricted because this class
   * should be instantiated through Micronaut.
   *
   * @param benchmarkKafkaSource
   *     a {@link StreamSource} producing benchmark-related events
   * @param portfolioKafkaSource
   *     a {@link StreamSource} producing portfolio-related events
   * @param portfolioRequestSource
   *     a {@link StreamSource} producing portfolio evaluation requests
   * @param portfolioResultSink
   *     a {@link Sink} consuming portfolio evaluation results
   * @param securityKafkaSource
   *     a {@link StreamSource} producing security-related events
   * @param tradeKafkaSource
   *     a {@link StreamSource} producing trade evaluation requests
   * @param tradeResultSink
   *     a {@link Sink} consuming trade evaluation results
   */
  protected PanoptesPipeline(@Named(PanoptesPipelineConfiguration.BENCHMARK_SOURCE)
      StreamSource<PortfolioEvent> benchmarkKafkaSource,
      @Named(PanoptesPipelineConfiguration.PORTFOLIO_SOURCE)
          StreamSource<PortfolioEvent> portfolioKafkaSource, /*
      @Named(PanoptesPipelineConfig.PORTFOLIO_EVALUATION_REQUEST_SOURCE)
          StreamSource<PortfolioEvaluationRequest> portfolioRequestSource, */
      @Named(PanoptesPipelineConfiguration.PORTFOLIO_EVALUATION_RESULT_SINK)
          Sink<RuleEvaluationResult> portfolioResultSink,
      @Named(PanoptesPipelineConfiguration.SECURITY_SOURCE)
          StreamSource<Security> securityKafkaSource
      /*,
      @Named(PanoptesPipelineConfig.TRADE_SOURCE) StreamSource<Trade> tradeKafkaSource,
      @Named(PanoptesPipelineConfig.TRADE_EVALUATION_RESULT_SINK)
          Sink<TradeEvaluationResult> tradeResultSink */) {
    LOG.info("initializing pipeline");

    pipeline = Pipeline.create();

    // obtain trade sequence information from the source ringbuffer
    /*
    StreamSource<TradeKey> tradeSequenceSource =
        RingbufferSource.buildRingbufferSource("tradeSequenceRingbufferSource", "tradeSequencing");
    StreamStage<Trade> trades = pipeline.readFrom(tradeSequenceSource).withIngestionTimestamps()
        .setName("tradeSequenceSource")
        .mapUsingIMap("FIXME_MAP_NAME", tk -> tk, (TradeKey tk, Trade t) -> t)
        .setName("sequencedTrades");
     */

    // obtain trades from Kafka and split each into its constituent transactions
    /*
    StreamStage<Trade> tradeSource =
        pipeline.readFrom(tradeKafkaSource).withIngestionTimestamps().setName("tradeSource");
    StreamStage<PortfolioEvent> transactionStream =
        tradeSource.flatMap(new TradeSplitter()).setName("tradeTransactions");
     */

    // obtain portfolio (event)s from Kafka and feed into a rule extractor; also merge with
    // benchmark events and transaction events; the combined stream will be fed into a portfolio
    // readiness evaluator
    StreamStage<PortfolioEvent> portfolioSource =
        pipeline.readFrom(portfolioKafkaSource).withIngestionTimestamps()
            .setName("portfolioSource");
    StreamStage<PortfolioEvent> benchmarkSource =
        pipeline.readFrom(benchmarkKafkaSource).withIngestionTimestamps()
            .setName("benchmarkSource");
    StreamStage<Tuple3<EvaluationSource, PortfolioKey, Rule>> portfolioRules =
        portfolioSource.flatMap(new RuleExtractor()).setName("ruleExtractor");
    StreamStage<PortfolioEvent> combinedPortfolios =
        benchmarkSource.merge(portfolioSource).setName("portfolioDataCombiner").filter(e -> {
          if (e instanceof PortfolioDataEvent pde) {
            AssetCache assetCache = PanoptesApp.getAssetCache();
            assetCache.getPortfolioCache().set(pde.getPortfolioKey(), pde.portfolio());
            assetCache.getRuleCache().setAll(pde.portfolio().getRules()
                .collect(Collectors.toMap(Keyed::getKey, r -> (ConfigurableRule) r)));
            assetCache.getPositionCache().setAll(
                pde.portfolio().getPositions().collect(Collectors.toMap(Keyed::getKey, p -> p)));
          }
          return true;
        }).setName("portfolioMapStore");
    StreamStage<PortfolioEvent> combinedPortfolioEvents =
        combinedPortfolios /* .merge(transactionStream).setName("portfolioEventCombiner") */;

    // obtain securities from Kafka and put to the Security map; these will be broadcast to each
    // portfolio/benchmark readiness evaluator
    StreamStage<Security> securitySource =
        pipeline.readFrom(securityKafkaSource).withIngestionTimestamps().setName("securitySource")
            .filter(s -> {
              PanoptesApp.getAssetCache().getSecurityCache().set(s.getKey(), s);
              return true;
            }).setName("securityMapStore");

    PortfolioReadinessEvaluator portfolioReadinessEvaluator = new PortfolioReadinessEvaluator();
    StreamStage<PortfolioKey> evaluationInputStream =
        combinedPortfolioEvents.groupingKey(PortfolioEvent::getPortfolioKey)
            .broadcastJoin(portfolioReadinessEvaluator,
                portfolioReadinessEvaluator.portfolioEventHandler(), securitySource,
                portfolioReadinessEvaluator.securityHandler())
            .setName("portfolioReadinessEvaluator");

    // feed the evaluation inputs (extracted rules and readiness events) into a rule evaluator
    EvaluationRequestGenerator evaluationRequestor = new EvaluationRequestGenerator();
    StreamStage<PortfolioEvaluationInput> portfolioRequestStream =
        evaluationInputStream.groupingKey(k -> k)
            .incrementalJoin(evaluationRequestor, evaluationRequestor.evaluationEventHandler(),
                portfolioRules.groupingKey(Tuple3::f1), evaluationRequestor.ruleEventHandler())
            .setName("evaluationRequestGenerator");

    StreamStage<RuleEvaluationResult> portfolioResultStream =
        portfolioRequestStream.flatMapUsingService(PortfolioEvaluationService.serviceFactory(),
                PortfolioEvaluationService::evaluate)
            .setLocalParallelism(Runtime.getRuntime().availableProcessors() - 1)
            .setName("portfolioEvaluator");

    // feed the rule evaluation results (keyed by benchmark ID + rule ID) and benchmark evaluation
    // results (keyed by portfolio ID, which *is* the benchmark ID for a benchmark, + rule ID) into
    // a benchmark comparator
    BenchmarkComparator benchmarkComparator = new BenchmarkComparator();
    StreamStage<RuleEvaluationResult> resultStream =
        portfolioResultStream.groupingKey(BenchmarkComparator.keyExtractor())
            .flatMapStateful(benchmarkComparator, benchmarkComparator)
            .setName("benchmarkComparator");

    // resultStream.writeTo(portfolioResultSink);
    resultStream.writeTo(SinkBuilder.sinkBuilder("evaluationResultSink", c -> c)
            .receiveFn(new EvaluationResultPublisher()).build())
        .setLocalParallelism(Runtime.getRuntime().availableProcessors());

    LOG.info("initialized pipeline");
  }

  /**
   * Obtains the Jet {@link Pipeline} corresponding to this {@link PanoptesPipeline}.
   *
   * @return a Jet {@link Pipeline}
   */
  public Pipeline getJetPipeline() {
    return pipeline;
  }
}
