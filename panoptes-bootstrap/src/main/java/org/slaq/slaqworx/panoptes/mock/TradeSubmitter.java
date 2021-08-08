package org.slaq.slaqworx.panoptes.mock;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.offline.PimcoBenchmarkDataSource;
import org.slaq.slaqworx.panoptes.trade.TaxLot;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A standalone application which publishes random trade events to Kafka.
 *
 * @author jeremy
 */
@Singleton
@Requires(env = {"trade-submit"})
public class TradeSubmitter {
  private static final Logger LOG = LoggerFactory.getLogger(TradeSubmitter.class);
  private final KafkaProducer kafkaProducer;

  /**
   * Creates a {@link TradeSubmitter} that publishes using the given {@link KafkaProducer}.
   *
   * @param kafkaProducer
   *     the {@link KafkaProducer} with which to publish events to Kafka
   */
  protected TradeSubmitter(KafkaProducer kafkaProducer) {
    this.kafkaProducer = kafkaProducer;
  }

  /**
   * Executes the {@link TradeSubmitter} application.
   *
   * @param args
   *     the program arguments (unused)
   *
   * @throws Exception
   *     if any error occurs
   */
  public static void main(String[] args) throws Exception {
    try (ApplicationContext appContext = Micronaut.build(args).mainClass(TradeSubmitter.class)
        .environments("trade-submit", "offline").args(args).build().start()) {
      TradeSubmitter submitter = appContext.getBean(TradeSubmitter.class);
      submitter.submitTrades();
    }
  }

  /**
   * Publishes random trades to Kafka.
   *
   * @throws IOException
   *     if the data could not be read
   */
  public void submitTrades() throws IOException {
    // generated keys must match those in DummyPortfolioMapLoader
    HashSet<PortfolioKey> portfolioKeys = new HashSet<>(900);
    for (int i = 1; i <= 900; i++) {
      portfolioKeys.add(new PortfolioKey("test" + i, 1));
    }

    ArrayList<Security> securities =
        new ArrayList<>(PimcoBenchmarkDataSource.getInstance().getSecurityMap().values());

    int numTrades = 1000;
    Random random = new Random(0);
    LOG.info("publishing {} trades", numTrades);

    for (int i = 0; i < numTrades; i++) {
      LocalDate tradeDate = LocalDate.now();
      LocalDate settlementDate = tradeDate.plusDays(3);
      Map<PortfolioKey, Transaction> transactions = new HashMap<>();

      // create between 1 and 300 transactions per trade
      int numTransactions = 1 + (int) (random.nextDouble() * 300);
      ArrayList<PortfolioKey> unusedPortfolioKeys = new ArrayList<>(portfolioKeys);
      for (int j = 0; j < numTransactions; j++) {
        // don't use a portfolio key more than once
        PortfolioKey portfolioKey =
            unusedPortfolioKeys.remove(random.nextInt(unusedPortfolioKeys.size()));

        // pick a security, any security
        SecurityKey securityKey = securities.get(random.nextInt(securities.size())).getKey();

        // generate an amount in the approximate range of 100.00 ~ 10_000.00
        double amount =
            100 + (long) (Math.pow(10, 2 + random.nextInt(3)) * random.nextDouble() * 100) / 100d;

        TaxLot allocation = new TaxLot(amount * 100, securityKey);
        Transaction transaction = new Transaction(portfolioKey, List.of(allocation));
        transactions.put(portfolioKey, transaction);
      }

      Trade trade = new Trade(tradeDate, settlementDate, transactions);

      kafkaProducer.publishTrade(trade.getKey(), trade);
      LOG.info("published trade {} with {} allocations", trade.getKey(),
          trade.getAllocationCount());
    }

    LOG.info("published {} trades", numTrades);
  }
}
