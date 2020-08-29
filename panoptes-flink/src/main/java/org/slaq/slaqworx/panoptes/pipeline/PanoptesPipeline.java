package org.slaq.slaqworx.panoptes.pipeline;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
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
    private final SourceFunction<PortfolioEvaluationRequest> portfolioRequestSource;
    private final SinkFunction<EvaluationResult> portfolioResultSink;
    private final SourceFunction<TradeEvaluationRequest> tradeRequestSource;
    private final SinkFunction<TradeEvaluationResult> tradeResultSink;
    private final StreamExecutionEnvironment env;

    protected PanoptesPipeline(
            @Named(PanoptesPipelineConfig.PORTFOLIO_EVALUATION_REQUEST_SOURCE) SourceFunction<
                    PortfolioEvaluationRequest> portfolioRequestSource,
            @Named(PanoptesPipelineConfig.PORTFOLIO_EVALUATION_RESULT_SINK) SinkFunction<
                    EvaluationResult> portfolioResultSink,
            @Named(PanoptesPipelineConfig.TRADE_EVALUATION_REQUEST_SOURCE) SourceFunction<
                    TradeEvaluationRequest> tradeRequestSource,
            @Named(PanoptesPipelineConfig.TRADE_EVALUATION_RESULT_SINK) SinkFunction<
                    TradeEvaluationResult> tradeResultSink,
            StreamExecutionEnvironment flinkEnvironment) {
        this.portfolioRequestSource = portfolioRequestSource;
        this.portfolioResultSink = portfolioResultSink;
        this.tradeRequestSource = tradeRequestSource;
        this.tradeResultSink = tradeResultSink;
        env = flinkEnvironment;
    }

    public void execute(String... args) throws Exception {
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

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

        env.execute("PanoptesPipeline");
    }
}
