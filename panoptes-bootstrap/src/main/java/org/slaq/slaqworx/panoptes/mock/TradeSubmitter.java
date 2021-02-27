package org.slaq.slaqworx.panoptes.mock;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.inject.Singleton;
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
@Context
@Requires(env = {"trade-submit"})
public class TradeSubmitter implements ApplicationEventListener<StartupEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(TradeSubmitter.class);

  /**
   * Executes the {@code TradeSubmitter} application.
   *
   * @param args
   *     the program arguments (unused)
   */
  public static void main(String[] args) {
    try (ApplicationContext appContext = Micronaut.build(args).mainClass(TradeSubmitter.class)
        .environments("trade-submit", "offline").args(args).start()) {
      // nothing else to do
    }
  }

  private final KafkaProducer kafkaProducer;

  /**
   * Creates a {@code TradeSubmitter} that publishes using the given {@code KafkaProducer}.
   *
   * @param kafkaProducer
   *     the {@code KafkaProducer} with which to publish events to Kafka
   */
  protected TradeSubmitter(KafkaProducer kafkaProducer) {
    this.kafkaProducer = kafkaProducer;
  }

  @Override
  public void onApplicationEvent(StartupEvent event) {
    TradeSubmitter submitter = event.getSource().getBean(TradeSubmitter.class);
    try {
      submitter.submitTrades();
    } catch (Exception e) {
      // FIXME throw a better exception
      throw new RuntimeException("could not perform bootstrap", e);
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
    HashSet<PortfolioKey> portfolioKeys = new HashSet<>(600);
    for (int i = 1; i <= 600; i++) {
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
        PortfolioKey portfolioKey = unusedPortfolioKeys
            .remove((int) (random.nextDouble() * unusedPortfolioKeys.size()));

        // pick a security, any security
        SecurityKey securityKey =
            securities.get((int) (random.nextDouble() * securities.size())).getKey();

        // generate an amount in the approximate range of 100.00 ~ 10_000.00
        double amount = 100
            + (long) (Math.pow(10, 2 + random.nextInt(3)) * random.nextDouble() * 100)
            / 100d;

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
