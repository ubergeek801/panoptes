package org.slaq.slaqworx.panoptes.evaluator;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.data.DummyPortfolioMapLoader;
import org.slaq.slaqworx.panoptes.data.PimcoBenchmarkDataSource;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluator;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * RuleEvaluatorPerformanceTest performs randomized rule evaluation tests and captures some
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
     * Creates a new RuleEvaluatorPerformanceTest.
     */
    public RuleEvaluatorPerformanceTest() {
        // nothing to do
    }

    /**
     * Creates random Portfolios (consisting of random Positions and Rules) and runs benchmarks
     * against them.
     *
     * @throws Exception
     *             if an error occurs while running the test
     */
    public void run() throws Exception {
        PimcoBenchmarkDataSource dataSource = PimcoBenchmarkDataSource.getInstance();

        // perform evaluation on each Portfolio
        LocalPortfolioEvaluator evaluator = new LocalPortfolioEvaluator();
        DummyPortfolioMapLoader mapLoader = new DummyPortfolioMapLoader();
        long portfolioStartTime = System.currentTimeMillis();
        ArrayList<Portfolio> portfolios = new ArrayList<>();
        for (PortfolioKey key : mapLoader.loadAllKeys()) {
            Portfolio benchmark = dataSource.getBenchmark(key);
            if (benchmark != null) {
                // the Portfolio is a benchmark; skip it
                continue;
            }

            Portfolio portfolio = mapLoader.load(key);
            portfolios.add(portfolio);
            evaluator.evaluate(portfolio, new EvaluationContext(dataSource, dataSource, mapLoader));
        }
        long portfolioEndTime = System.currentTimeMillis();

        // perform evaluation on synthetic Trades with 100, 200, 400 and 800 allocations
        ArrayList<Long> tradeStartTimes = new ArrayList<>();
        ArrayList<Long> tradeEndTimes = new ArrayList<>();
        ArrayList<Long> allocationCounts = new ArrayList<>();
        ArrayList<Long> evaluationGroupCounts = new ArrayList<>();
        for (int i = 1; i <= 8; i *= 2) {
            ArrayList<Position> positions = new ArrayList<>();
            Security security1 = dataSource.getSecurityMap().values().iterator().next();
            Position position1 = new Position(1_000_000, security1);
            positions.add(position1);
            TradeEvaluator tradeEvaluator = new TradeEvaluator(dataSource, dataSource, mapLoader);
            ArrayList<Transaction> transactions = new ArrayList<>();
            portfolios.stream().limit(i * 62).forEach(portfolio -> {
                Transaction transaction = new Transaction(portfolio, positions);
                transactions.add(transaction);
            });
            Trade trade = new Trade(transactions);

            tradeStartTimes.add(System.currentTimeMillis());
            TradeEvaluationResult result = tradeEvaluator.evaluate(trade);
            long numEvaluationGroups = result.getImpacts().values().parallelStream()
                    .collect(Collectors.summingLong(m -> m.size()));
            tradeEndTimes.add(System.currentTimeMillis());
            allocationCounts.add(trade.getTransactions().count());
            evaluationGroupCounts.add(numEvaluationGroups);
        }

        // log the timing results
        LOG.info("processed {} portfolios in {} ms", portfolios.size(),
                portfolioEndTime - portfolioStartTime);
        for (int i = 0; i < tradeStartTimes.size(); i++) {
            LOG.info("processed trade with {} allocations using {} evaluations in {} ms",
                    allocationCounts.get(i), evaluationGroupCounts.get(i),
                    tradeEndTimes.get(i) - tradeStartTimes.get(i));
        }
    }
}
