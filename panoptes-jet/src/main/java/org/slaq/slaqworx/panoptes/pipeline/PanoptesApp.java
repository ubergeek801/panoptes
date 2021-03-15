package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.cluster.Member;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.multimap.MultiMap;
import com.hazelcast.ringbuffer.Ringbuffer;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point of the Hazelcast Jet edition of Panoptes. The application configures a
 * Micronaut {@link ApplicationContext} and initializes the Jet pipeline.
 *
 * @author jeremy
 */
@Singleton
@Context
@Requires(notEnv = Environment.TEST)
public class PanoptesApp {
  private static final Logger LOG = LoggerFactory.getLogger(PanoptesApp.class);

  private static ApplicationContext globalAppContext;

  /**
   * Obtains the {@link ApplicationContext} singleton.
   *
   * @param args
   *     the program arguments with which to initialize the {@link ApplicationContext}; ignored (and
   *     may be empty) if the context has already been created
   *
   * @return the {@link ApplicationContext}
   */
  public static ApplicationContext getApplicationContext(String... args) {
    if (globalAppContext == null) {
      globalAppContext = createApplicationContext(args);
    }

    return globalAppContext;
  }

  /**
   * Obtains the {@link AssetCache} from the application context.
   *
   * @param args
   *     the program arguments with which to initialize the {@link ApplicationContext}; ignored (and
   *     may be empty) if the context has already been created
   *
   * @return the {@link AssetCache} singleton
   */
  public static AssetCache getAssetCache(String... args) {
    return getApplicationContext(args).getBean(AssetCache.class);
  }

  /**
   * Executes the Panoptes application.
   *
   * @param args
   *     the program arguments
   */
  public static void main(String[] args) {
    try (ApplicationContext appContext = createApplicationContext(args)) {
      globalAppContext = appContext;
      LOG.info("configuring PanoptesPipeline");

      JetConfig jetConfig = appContext.getBean(JetConfig.class);
      JetInstance jetInstance = Jet.newJetInstance(jetConfig);
      HazelcastInstance hazelcastInstance = jetInstance.getHazelcastInstance();
      hazelcastInstance.getConfig().addMultiMapConfig(
          new MultiMapConfig().setName("portfolioTrades")
              .setValueCollectionType(MultiMapConfig.ValueCollectionType.LIST));
      MultiMap<PortfolioKey, TradeKey> portfolioTradeMap =
          hazelcastInstance.getMultiMap("portfolioTrades");

      Random random = new Random();
      String hostname = System.getenv("NODENAME");
      long startTime = System.currentTimeMillis();
      for (int tradeId = 1; tradeId <= 100; tradeId++) {
        long tradeStartTime = System.currentTimeMillis();
        TradeKey tradeKey = new TradeKey(hostname + ":" + tradeId);
        TreeSet<PortfolioKey> portfolioKeys = new TreeSet<>();
        try {
          int numAllocations = 1 + Math.abs((int) (random.nextGaussian() * 100));
          for (int i = 0; i < numAllocations; i++) {
            portfolioKeys.add(new PortfolioKey(String.valueOf((int) (Math.random() * 2000)), 1));
          }
          portfolioKeys.forEach(portfolioTradeMap::lock);
          portfolioKeys.forEach(portfolioKey -> portfolioTradeMap.put(portfolioKey, tradeKey));
        } finally {
          portfolioKeys.forEach(portfolioTradeMap::unlock);
        }
        LOG.info("committed trade {} with {} allocations in {} ms", tradeKey, portfolioKeys.size(),
            System.currentTimeMillis() - tradeStartTime);
      }
      LOG.info("created trade ordering in {} ms", System.currentTimeMillis() - startTime);

      Ringbuffer<TradeKey> ringbuffer = hazelcastInstance.getRingbuffer("tradeOrdering");
      startTime = System.currentTimeMillis();
      for (int tradeId = 1; tradeId <= 100; tradeId++) {
        TradeKey tradeKey = new TradeKey(hostname + ":" + tradeId);
        ringbuffer.add(tradeKey);
      }
      LOG.info("added trades to Ringbuffer in {} ms", System.currentTimeMillis() - startTime);

      new Thread(() -> {
        try {
          Thread.sleep(30_000);
        } catch (InterruptedException e) {
          return;
        }

        // copy the distributed map into a local map, sorted by keys for display purposes only
        TreeMap<Integer, Collection<TradeKey>> mapCopy = new TreeMap<>();
        portfolioTradeMap.keySet().forEach(k -> {
          Collection<TradeKey> portfolioTrades = portfolioTradeMap.get(k);
          mapCopy.put(Integer.valueOf(k.getId()), portfolioTrades);
        });

        // while logging the portfolio/trade mappings, build a set of distinct trade keys
        HashSet<TradeKey> allTradeKeys = new HashSet<>();
        mapCopy.forEach((k, v) -> {
          LOG.info(k + " => " + v);
          allTradeKeys.addAll(v);
        });

        if (allTradeKeys.size() == 300) {
          LOG.info("found expected number of trades");
        } else {
          LOG.error("found unexpected number of trades: {}", allTradeKeys.size());
        }

        // initialize a map which will track, for each trade key, the trades which occur later
        HashMap<TradeKey, Set<TradeKey>> orderingMap = new HashMap<>();
        allTradeKeys.forEach(k -> orderingMap.put(k, new HashSet<>()));

        // for each portfolio, attempt to integrate its trade ordering into the total ordering, or
        // detect an inconsistency
        int[] numConsistentPortfolios = new int[1];
        mapCopy.forEach((portfolioKey, trades) -> {
          boolean isConsistent = true;
          ArrayList<TradeKey> portfolioTrades = new ArrayList<>(trades);
          for (int i = 0; i < portfolioTrades.size() - 1; i++) {
            TradeKey earlierTrade = portfolioTrades.get(i);
            Set<TradeKey> knownLaterTrades = orderingMap.get(earlierTrade);
            for (int j = i + 1; j < portfolioTrades.size(); j++) {
              TradeKey laterTrade = portfolioTrades.get(j);
              Set<TradeKey> laterLaterTrades = orderingMap.get(laterTrade);
              if (laterLaterTrades.contains(earlierTrade)) {
                LOG.error("inconsistency: in portfolio {}, trade {} precedes {}", portfolioKey,
                    earlierTrade, laterTrade);
                isConsistent = false;
              } else {
                knownLaterTrades.add(laterTrade);
              }
            }
          }
          if (isConsistent) {
            numConsistentPortfolios[0]++;
          }
        });

        LOG.info("found {} consistent portfolios", numConsistentPortfolios[0]);
      }).start();

      Member localMember = hazelcastInstance.getCluster().getLocalMember();
      if (!localMember
          .equals(hazelcastInstance.getPartitionService().getPartition("1").getOwner())) {
        LOG.info("not elected to start PanoptesPipeline");
        return;
      }

      LOG.info("elected to start PanoptesPipeline");
      /*
      PanoptesPipeline pipeline = appContext.getBean(PanoptesPipeline.class);
      JobConfig jobConfig = appContext.getBean(JobConfig.class);
      jetInstance.newJob(pipeline.getJetPipeline(), jobConfig).join();
       */
    }
  }

  /**
   * Creates the Micronaut {@link ApplicationContext}.
   *
   * @param args
   *     the program arguments with which to initialize the {@link ApplicationContext}
   *
   * @return the {@link ApplicationContext}
   */
  protected static ApplicationContext createApplicationContext(String... args) {
    return Micronaut.build(args).mainClass(PanoptesApp.class).environments(args).start();
  }
}
