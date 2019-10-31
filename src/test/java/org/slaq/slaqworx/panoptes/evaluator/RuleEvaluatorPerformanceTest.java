package org.slaq.slaqworx.panoptes.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.data.DummyPortfolioCacheLoader;
import org.slaq.slaqworx.panoptes.data.PimcoBenchmarkDataSource;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluator;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * {@code RuleEvaluatorPerformanceTest} performs randomized rule evaluation tests and captures some
 * benchmarking metrics, using publicly available benchmark constituent data for certain PIMCO
 * benchmarks.
 *
 * @author jeremy
 */
public class RuleEvaluatorPerformanceTest {
    private static final Logger LOG = LoggerFactory.getLogger(RuleEvaluatorPerformanceTest.class);

    public static void main(String[] args) throws Exception {
        new RuleEvaluatorPerformanceTest().run();
    }

    /**
     * Creates a new {@code RuleEvaluatorPerformanceTest}.
     */
    public RuleEvaluatorPerformanceTest() {
        // nothing to do
    }

    /**
     * Creates random {@code Portfolio}s (consisting of random {@code Position}s and {@code Rule}s)
     * and runs benchmarks against them.
     *
     * @throws Exception
     *             if an error occurs while running the test
     */
    public void run() throws Exception {
        PimcoBenchmarkDataSource dataSource = PimcoBenchmarkDataSource.getInstance();

        // initialize the Portfolios to be evaluated

        DummyPortfolioCacheLoader mapLoader = new DummyPortfolioCacheLoader();
        ArrayList<Portfolio> portfolios = new ArrayList<>();
        mapLoader.inputIterator().forEachRemaining(key -> {
            Portfolio benchmark = dataSource.getBenchmark(key);
            if (benchmark != null) {
                // the Portfolio is a benchmark; skip it
                return;
            }

            Portfolio portfolio = mapLoader.load(key);
            portfolios.add(portfolio);
        });

        // perform evaluation on each Portfolio

        LocalPortfolioEvaluator evaluator = new LocalPortfolioEvaluator(mapLoader);
        ExecutorService evaluationExecutor = Executors.newSingleThreadExecutor();
        long portfolioStartTime;
        long portfolioEndTime;
        long numPortfolioRuleEvalutions = 0;
        try {
            ExecutorCompletionService<Pair<PortfolioKey, Map<RuleKey, EvaluationResult>>> completionService =
                    new ExecutorCompletionService<>(evaluationExecutor);

            portfolioStartTime = System.currentTimeMillis();
            portfolios.forEach(p -> {
                completionService.submit(() -> new ImmutablePair<>(p.getKey(),
                        evaluator.evaluate(p, new EvaluationContext()).get()));
            });
            // wait for all of the evaluations to complete
            for (Portfolio portfolio : portfolios) {
                Pair<PortfolioKey, Map<RuleKey, EvaluationResult>> result =
                        completionService.take().get();
                numPortfolioRuleEvalutions += result.getRight().size();
            }
            portfolioEndTime = System.currentTimeMillis();
        } finally {
            evaluationExecutor.shutdown();
        }

        // perform evaluation on synthetic Trades with 100, 200, 400 and 800 allocations
        ArrayList<Long> tradeStartTimes = new ArrayList<>();
        ArrayList<Long> tradeEndTimes = new ArrayList<>();
        ArrayList<Integer> allocationCounts = new ArrayList<>();
        ArrayList<Long> evaluationGroupCounts = new ArrayList<>();
        for (int i = 1; i <= 8; i *= 2) {
            ArrayList<Position> positions = new ArrayList<>();
            Security security1 = dataSource.getSecurityMap().values().iterator().next();
            Position position1 = new Position(1_000_000, security1);
            positions.add(position1);
            TradeEvaluator tradeEvaluator =
                    new TradeEvaluator(new LocalPortfolioEvaluator(mapLoader), mapLoader);
            HashMap<PortfolioKey, Transaction> transactions = new HashMap<>();
            portfolios.stream().limit(i * 62).forEach(portfolio -> {
                Transaction transaction = new Transaction(portfolio.getKey(), positions);
                transactions.put(portfolio.getKey(), transaction);
            });
            Trade trade = new Trade(transactions);

            tradeStartTimes.add(System.currentTimeMillis());
            TradeEvaluationResult result =
                    tradeEvaluator.evaluate(trade, EvaluationMode.FULL_EVALUATION);
            long numEvaluationGroups = result.getImpacts().values().parallelStream()
                    .collect(Collectors.summingLong(m -> m.size()));
            tradeEndTimes.add(System.currentTimeMillis());
            allocationCounts.add(trade.getTransactions().size());
            evaluationGroupCounts.add(numEvaluationGroups);
        }

        // log the timing results
        LOG.info("processed {} Portfolios using {} Rule evaluations in {} ms", portfolios.size(),
                numPortfolioRuleEvalutions, portfolioEndTime - portfolioStartTime);
        for (int i = 0; i < tradeStartTimes.size(); i++) {
            LOG.info("processed Trade with {} allocations producing {} evaluation groups in {} ms",
                    allocationCounts.get(i), evaluationGroupCounts.get(i),
                    tradeEndTimes.get(i) - tradeStartTimes.get(i));
        }
    }
}
