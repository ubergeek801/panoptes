package org.slaq.slaqworx.panoptes.pipeline;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.evaluator.RuleEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;

@Singleton
public class PanoptesPipeline {
    private static final Logger LOGGER = LoggerFactory.getLogger(PanoptesPipeline.class);

    private static final TypeInformation<SecurityKey> SECURITY_KEY_TYPE_INFO =
            TypeInformation.of(new TypeHint<SecurityKey>() {
                // trivial
            });
    private static final TypeInformation<Security> SECURITY_TYPE_INFO =
            TypeInformation.of(new TypeHint<Security>() {
                // trivial
            });

    private final SourceFunction<Portfolio> benchmarkSource;
    private final SourceFunction<Portfolio> portfolioSource;
    private final SourceFunction<PortfolioEvaluationRequest> portfolioRequestSource;
    private final SinkFunction<EvaluationResult> portfolioResultSink;
    private final SourceFunction<Security> securitySource;
    private final SourceFunction<TradeEvaluationRequest> tradeRequestSource;
    private final SinkFunction<TradeEvaluationResult> tradeResultSink;

    private final StreamExecutionEnvironment env;

    protected PanoptesPipeline(
            @Named(PanoptesPipelineConfig.BENCHMARK_SOURCE) SourceFunction<
                    Portfolio> benchmarkSource,
            @Named(PanoptesPipelineConfig.PORTFOLIO_SOURCE) SourceFunction<
                    Portfolio> portfolioSource,
            @Named(PanoptesPipelineConfig.PORTFOLIO_EVALUATION_REQUEST_SOURCE) SourceFunction<
                    PortfolioEvaluationRequest> portfolioRequestSource,
            @Named(PanoptesPipelineConfig.PORTFOLIO_EVALUATION_RESULT_SINK) SinkFunction<
                    EvaluationResult> portfolioResultSink,
            @Named(PanoptesPipelineConfig.SECURITY_SOURCE) SourceFunction<Security> securitySource,
            @Named(PanoptesPipelineConfig.TRADE_EVALUATION_REQUEST_SOURCE) SourceFunction<
                    TradeEvaluationRequest> tradeRequestSource,
            @Named(PanoptesPipelineConfig.TRADE_EVALUATION_RESULT_SINK) SinkFunction<
                    TradeEvaluationResult> tradeResultSink,
            StreamExecutionEnvironment flinkEnvironment) {
        this.benchmarkSource = benchmarkSource;
        this.portfolioSource = portfolioSource;
        this.portfolioRequestSource = portfolioRequestSource;
        this.portfolioResultSink = portfolioResultSink;
        this.securitySource = securitySource;
        this.tradeRequestSource = tradeRequestSource;
        this.tradeResultSink = tradeResultSink;
        env = flinkEnvironment;
    }

    public void execute(String... args) throws Exception {
        LOGGER.info("initializing pipeline");

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        MapStateDescriptor<SecurityKey, Security> securityStateDescriptor =
                new MapStateDescriptor<>("SecurityBroadcastState", SECURITY_KEY_TYPE_INFO,
                        SECURITY_TYPE_INFO);
        env.addSource(securitySource).broadcast(securityStateDescriptor);

        env.addSource(tradeRequestSource).map(TradeEvaluationRequest::call)
                .addSink(tradeResultSink);
        env.addSource(portfolioRequestSource).flatMap((request, out) -> {
            Portfolio p = PanoptesApp.getAssetCache(args).getPortfolio(request.getPortfolioKey());
            p.getRules().forEach(rule -> out
                    .collect(new PortfolioRuleKey(request.getPortfolioKey(), rule.getKey())));
        }, TypeInformation.of(PortfolioRuleKey.class)).map(pr -> {
            AssetCache assetCache = PanoptesApp.getAssetCache(args);
            Portfolio p = assetCache.getPortfolio(pr.getPortfolioKey());
            Rule rule = assetCache.getRule(pr.getRuleKey());
            EvaluationContext context = new EvaluationContext(assetCache, assetCache);
            return new RuleEvaluator(rule, p, p.getBenchmark(assetCache), context).call();
        }).addSink(portfolioResultSink);

        LOGGER.info("initialized pipeline");

        env.execute("PanoptesPipeline");

        LOGGER.info("pipeline terminated");
    }
}
