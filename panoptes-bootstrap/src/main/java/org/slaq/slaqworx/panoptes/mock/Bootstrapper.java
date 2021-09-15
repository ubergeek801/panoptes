package org.slaq.slaqworx.panoptes.mock;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.slaq.slaqworx.panoptes.asset.EligibilityList;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.offline.DummyPortfolioMapLoader;
import org.slaq.slaqworx.panoptes.offline.PimcoBenchmarkDataSource;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EligibilityListMsg.ListType;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * "Bootstraps" a Kafka-aware Panoptes instance by publishing the "seed" data (e.g. start-of-day
 * portfolio positions) to the appropriate Kafka topics.
 *
 * @author jeremy
 */
@Singleton
@Requires(env = {"bootstrap"})
public class Bootstrapper {
  private static final Logger LOG = LoggerFactory.getLogger(Bootstrapper.class);
  private static final Random random = new Random(0);

  private final KafkaProducer kafkaProducer;

  /**
   * Creates a {@link Bootstrapper} that publishes using the given {@link KafkaProducer}.
   *
   * @param kafkaProducer
   *     the {@link KafkaProducer} with which to publish events to Kafka
   */
  protected Bootstrapper(KafkaProducer kafkaProducer) {
    this.kafkaProducer = kafkaProducer;
  }

  /**
   * Executes the {@link Bootstrapper} application.
   *
   * @param args
   *     the program arguments (unused)
   *
   * @throws Exception
   *     if any error occurs
   */
  public static void main(String[] args) throws Exception {
    try (ApplicationContext appContext = Micronaut.build(args).mainClass(Bootstrapper.class)
        .environments("bootstrap", "offline").args(args).build().start()) {
      Bootstrapper bootstrapper = appContext.getBean(Bootstrapper.class);
      bootstrapper.bootstrap();
    }
  }

  /**
   * Bootstraps the appropriate seed data via Kafka.
   *
   * @throws IOException
   *     if the data could not be read
   */
  public void bootstrap() throws IOException {
    bootstrapEligibilityLists();
    bootstrapSecurities();
    bootstrapBenchmarks();
    DummyPortfolioMapLoader portfolioMapLoader = bootstrapPortfolios();
    bootstrapTrades(portfolioMapLoader);
  }

  /**
   * Bootstraps the seed benchmark data.
   *
   * @throws IOException
   *     if the data could not be read
   */
  protected void bootstrapBenchmarks() throws IOException {
    // simply publish all known benchmarks to Kafka
    Map<PortfolioKey, Portfolio> benchmarks =
        PimcoBenchmarkDataSource.getInstance().getBenchmarkMap();

    LOG.info("publishing {} benchmarks", benchmarks.size());
    benchmarks.entrySet().parallelStream().forEach(
        (e -> kafkaProducer.publishBenchmarkEvent(e.getKey(),
            new PortfolioDataEvent(e.getValue()))));
    LOG.info("published benchmarks");
  }

  /**
   * Bootstraps the seed eligibility list data.
   *
   * @throws IOException
   *     if the data could not be read
   */
  protected void bootstrapEligibilityLists() throws IOException {
    LOG.info("generating eligibility lists");
    // collect distinct values of various attributes
    Map<SecurityKey, Security> securities = PimcoBenchmarkDataSource.getInstance().getSecurityMap();
    List<String> countries =
        securities.values().stream().map(s -> s.getAttributeValue(SecurityAttribute.country, false))
            .filter(Objects::nonNull).distinct().collect(Collectors.toCollection(ArrayList::new));
    LOG.info("{} distinct countries", countries.size());
    List<String> cusips =
        securities.values().stream().map(s -> s.getAttributeValue(SecurityAttribute.cusip, false))
            .filter(Objects::nonNull).distinct().collect(Collectors.toCollection(ArrayList::new));
    LOG.info("{} distinct CUSIPs", cusips.size());
    List<String> issuers =
        securities.values().stream().map(s -> s.getAttributeValue(SecurityAttribute.issuer, false))
            .filter(Objects::nonNull).distinct().collect(Collectors.toCollection(ArrayList::new));
    LOG.info("{} distinct issuers", issuers.size());

    // generate some random eligibility lists
    ArrayList<EligibilityList> eligibilityLists = new ArrayList<>(300);
    for (int i = 1; i <= 100; i++) {
      Set<String> countryList = new HashSet<>(30);
      int numCountries = 10 + random.nextInt(21);
      for (int j = 0; j < numCountries; j++) {
        countryList.add(countries.get(random.nextInt(countries.size())));
      }
      eligibilityLists.add(
          new EligibilityList("country" + i, ListType.COUNTRY, "generated country list " + i,
              countryList));

      Set<String> cusipList = new HashSet<>(1000);
      int numCusips = 20 + random.nextInt(981);
      for (int j = 0; j < numCusips; j++) {
        cusipList.add(cusips.get(random.nextInt(cusips.size())));
      }
      eligibilityLists.add(
          new EligibilityList("cusip" + i, ListType.SECURITY, "generated security list " + i,
              cusipList));

      Set<String> issuerList = new HashSet<>(200);
      int numIssuers = 20 + random.nextInt(181);
      for (int j = 0; j < numIssuers; j++) {
        issuerList.add(issuers.get(random.nextInt(issuers.size())));
      }
      eligibilityLists.add(
          new EligibilityList("issuer" + i, ListType.ISSUER, "generated issuer list " + i,
              issuerList));
    }

    LOG.info("publishing {} eligibility lists", eligibilityLists.size());
    eligibilityLists.parallelStream()
        .forEach(l -> kafkaProducer.publishEligibilityList(l.name(), l));
    LOG.info("published eligibility lists");
  }

  /**
   * Bootstraps the seed portfolio data.
   *
   * @return the {@link DummyPortfolioMapLoader} used to bootstrap the portfolios
   *
   * @throws IOException
   *     if the data could not be read
   */
  protected DummyPortfolioMapLoader bootstrapPortfolios() throws IOException {
    // generate the portfolios
    LOG.info("generating portfolios");
    DummyPortfolioMapLoader mapLoader = new DummyPortfolioMapLoader(800);
    ArrayList<Portfolio> portfolios = new ArrayList<>();
    for (PortfolioKey key : mapLoader.loadAllKeys()) {
      Portfolio portfolio = mapLoader.load(key);
      // skip "abstract" portfolios (e.g. benchmarks) since we deal with them elsewhere
      if (!portfolio.isAbstract()) {
        portfolios.add(portfolio);
      }
    }

    LOG.info("publishing {} portfolios", portfolios.size());
    portfolios.parallelStream()
        .forEach(p -> kafkaProducer.publishPortfolioEvent(p.getKey(), new PortfolioDataEvent(p)));
    LOG.info("published portfolios");

    return mapLoader;
  }

  /**
   * Bootstraps the seed security data.
   *
   * @throws IOException
   *     if the data could not be read
   */
  protected void bootstrapSecurities() throws IOException {
    // simply publish all known securities to Kafka
    Map<SecurityKey, Security> securities = PimcoBenchmarkDataSource.getInstance().getSecurityMap();

    LOG.info("publishing {} securities", securities.size());
    securities.values().parallelStream().forEach(s -> kafkaProducer.publishSecurity(s.getKey(), s));
    LOG.info("published securities");
  }

  /**
   * Bootstraps the seed trade data.
   *
   * @param portfolioMapLoader
   *     the {@link DummyPortfolioMapLoader} which was previously used to bootstrap portfolios
   */
  protected void bootstrapTrades(DummyPortfolioMapLoader portfolioMapLoader) {
    // simply publish all known trades to Kafka
    Map<TradeKey, Trade> tradeMap = portfolioMapLoader.getGeneratedTrades();

    LOG.info("publishing {} trades", tradeMap.size());
    tradeMap.entrySet().parallelStream()
        .forEach(e -> kafkaProducer.publishTrade(e.getKey(), e.getValue()));
    LOG.info("published trades");
  }
}
