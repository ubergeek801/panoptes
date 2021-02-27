package org.slaq.slaqworx.panoptes;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.apache.commons.lang3.tuple.Pair;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.LocalPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.offline.DummyPortfolioMapLoader;
import org.slaq.slaqworx.panoptes.offline.PimcoBenchmarkDataSource;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext.EvaluationMode;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.LocalTradeEvaluator;
import org.slaq.slaqworx.panoptes.trade.TaxLot;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluator;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panoptes is a prototype system for investment portfolio compliance assurance. This class is the
 * entry point of the application.
 *
 * @author jeremy
 */
@Singleton
@Context
@Requires(notEnv = Environment.TEST)
public class Panoptes implements ApplicationEventListener<ApplicationStartupEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(Panoptes.class);

  /**
   * The entry point for the Panoptes application.
   *
   * @param args
   *     the program arguments
   */
  public static void main(String[] args) {
    Micronaut.run(Panoptes.class, args);
  }

  /**
   * Creates a new instance of the Panoptes application.
   */
  protected Panoptes() {
    // nothing to do
  }

  @Override
  public void onApplicationEvent(ApplicationStartupEvent event) {
    try {
      @SuppressWarnings("resource") ApplicationContext applicationContext =
          event.getSource().getApplicationContext();
      AssetCache assetCache = applicationContext.getBean(AssetCache.class);

      if (applicationContext.getEnvironment().getActiveNames().contains("offline")) {
        initializeCache(assetCache);
      }

      int numSecurities = assetCache.getSecurityCache().size();
      LOG.info("{} Securities in cache", numSecurities);

      int numPositions = assetCache.getPositionCache().size();
      LOG.info("{} Positions in cache", numPositions);

      int numRules = assetCache.getRuleCache().size();
      LOG.info("{} Rules in cache", numRules);

      int numPortfolios = assetCache.getPortfolioCache().size();
      LOG.info("{} Portfolios in cache", numPortfolios);

      LOG.info("Panoptes cluster node ready");

      if (System.getProperty("perftest") != null) {
        runPerformanceTest(assetCache);
      }
    } catch (Exception e) {
      // FIXME throw a better exception
      throw new RuntimeException("could not initialize Panoptes", e);
    }
  }

  /**
   * Initializes the cache for offline mode from dummy data, in much the same way as {@code
   * PimcoBenchmarkDatabaseLoader} initializes the persistent store.
   *
   * @param assetCache
   *     the cache to be initialized
   *
   * @throws IOException
   *     if source data could not be read
   */
  protected void initializeCache(AssetCache assetCache) throws IOException {
    PimcoBenchmarkDataSource pimcoDataSource = PimcoBenchmarkDataSource.getInstance();

    LOG.info("initializing {} Securities", pimcoDataSource.getSecurityMap().size());
    assetCache.getSecurityCache().putAll(pimcoDataSource.getSecurityMap());

    DummyPortfolioMapLoader mapLoader = new DummyPortfolioMapLoader(500);
    ArrayList<Portfolio> portfolios = new ArrayList<>();
    for (PortfolioKey key : mapLoader.loadAllKeys()) {
      Portfolio portfolio = mapLoader.load(key);
      portfolios.add(portfolio);
    }

    portfolios.stream().forEach(pf -> {
      LOG.info("initializing {} Rules for Portfolio \"{}\"", pf.getRules().count(),
          pf.getName());
      pf.getRules()
          .forEach(r -> assetCache.getRuleCache().set(r.getKey(), (ConfigurableRule) r));
    });

    portfolios.stream().forEach(pf -> {
      LOG.info("initializing {} Positions for Portfolio \"{}\"", pf.getPositions().count(),
          pf.getName());
      pf.getPositions().forEach(p -> assetCache.getPositionCache().set(p.getKey(), p));
    });

    // initialize the benchmarks first
    LOG.info("initializing 4 benchmark Portfolios");
    assetCache.getPortfolioCache().set(PimcoBenchmarkDataSource.EMAD_KEY,
        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.EMAD_KEY));
    assetCache.getPortfolioCache().set(PimcoBenchmarkDataSource.GLAD_KEY,
        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.GLAD_KEY));
    assetCache.getPortfolioCache().set(PimcoBenchmarkDataSource.ILAD_KEY,
        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.ILAD_KEY));
    assetCache.getPortfolioCache().set(PimcoBenchmarkDataSource.PGOV_KEY,
        pimcoDataSource.getPortfolio(PimcoBenchmarkDataSource.PGOV_KEY));

    portfolios.stream().filter(p -> p.getKey().getId().length() != 4).forEach(p -> {
      LOG.info("initializing Portfolio {}", p.getKey());
      assetCache.getPortfolioCache().set(p.getKey(), p);
    });

    LOG.info("completed cache initialization");
  }

  /**
   * Executes a performance test which performs evaluations on all Portfolios, as well as a number
   * of multi-allocation Trades.
   *
   * @param assetCache
   *     the {@code AssetCache} from which to obtain {@code Portfolio} data
   *
   * @throws ExecutionException
   *     if evaluations could not be executed
   * @throws InterruptedException
   *     if an interruption occurs during evaluation
   */
  protected void runPerformanceTest(AssetCache assetCache)
      throws ExecutionException, InterruptedException {
    // perform evaluation on each Portfolio
    LocalPortfolioEvaluator evaluator = new LocalPortfolioEvaluator(assetCache);
    ExecutorService evaluationExecutor =
        Executors.newWorkStealingPool(ForkJoinPool.getCommonPoolParallelism());
    long portfolioStartTime;
    long portfolioEndTime;
    long numPortfolioRuleEvalutions = 0;
    Collection<PortfolioKey> portfolioKeys = assetCache.getPortfolioCache().keySet();
    int numPortfolios = portfolioKeys.size();
    try {
      ExecutorCompletionService<
          Pair<PortfolioKey, Map<RuleKey, EvaluationResult>>> completionService =
          new ExecutorCompletionService<>(evaluationExecutor);

      portfolioStartTime = System.currentTimeMillis();
      portfolioKeys.forEach(key -> {
        completionService.submit(
            () -> Pair.of(key, evaluator.evaluate(key, new EvaluationContext()).get()));
      });
      // wait for all of the evaluations to complete
      for (int i = 0; i < numPortfolios; i++) {
        Pair<PortfolioKey, Map<RuleKey, EvaluationResult>> result =
            completionService.take().get();
        numPortfolioRuleEvalutions += result.getRight().size();
      }
      portfolioEndTime = System.currentTimeMillis();
    } finally {
      evaluationExecutor.shutdown();
    }

    // perform evaluation on synthetic Trades with 62, 124, 248 and 496 allocations
    ArrayList<Long> tradeStartTimes = new ArrayList<>();
    ArrayList<Long> tradeEndTimes = new ArrayList<>();
    ArrayList<Integer> allocationCounts = new ArrayList<>();
    ArrayList<Long> evaluationGroupCounts = new ArrayList<>();
    SecurityKey security1Key = new SecurityKey("US594918AM64"); // pretty arbitrary
    LocalDate tradeDate = LocalDate.now();
    for (int i = 1; i <= 8; i *= 2) {
      ArrayList<TaxLot> taxLots = new ArrayList<>();
      TaxLot allocation1 = new TaxLot(100_000, security1Key);
      taxLots.add(allocation1);
      TradeEvaluator tradeEvaluator = new LocalTradeEvaluator(
          new LocalPortfolioEvaluator(assetCache), assetCache, assetCache);
      HashMap<PortfolioKey, Transaction> transactions = new HashMap<>();
      portfolioKeys.stream().limit(i * 62).forEach(key -> {
        Transaction transaction = new Transaction(key, taxLots);
        transactions.put(key, transaction);
      });
      Trade trade = new Trade(tradeDate, tradeDate, transactions);

      tradeStartTimes.add(System.currentTimeMillis());
      TradeEvaluationResult result = tradeEvaluator
          .evaluate(trade, new EvaluationContext(EvaluationMode.FULL_EVALUATION)).get();
      long numEvaluationGroups = result.getImpacts().values().stream()
          .collect(Collectors.summingLong(Map::size));
      tradeEndTimes.add(System.currentTimeMillis());
      allocationCounts.add(trade.getTransactions().size());
      evaluationGroupCounts.add(numEvaluationGroups);
    }

    // log the timing results
    LOG.info("processed {} Portfolios using {} Rule evaluations in {} ms", numPortfolios,
        numPortfolioRuleEvalutions, portfolioEndTime - portfolioStartTime);
    for (int i = 0; i < tradeStartTimes.size(); i++) {
      LOG.info("processed Trade with {} allocations producing {} evaluation groups in {} ms",
          allocationCounts.get(i), evaluationGroupCounts.get(i),
          tradeEndTimes.get(i) - tradeStartTimes.get(i));
    }
  }
}
