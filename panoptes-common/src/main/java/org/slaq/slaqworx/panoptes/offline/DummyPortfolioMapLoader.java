package org.slaq.slaqworx.panoptes.offline;

import com.hazelcast.map.MapStore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioPosition;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.GroovyPositionFilter;
import org.slaq.slaqworx.panoptes.rule.MarketValueRule;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;
import org.slaq.slaqworx.panoptes.rule.SecurityAttributeGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.TopNSecurityAttributeAggregator;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;
import org.slaq.slaqworx.panoptes.trade.TaxLot;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MapStore} that initializes the Hazelcast cache with random {@link Portfolio} data.
 *
 * @author jeremy
 */
public class DummyPortfolioMapLoader
    implements MapStore<PortfolioKey, Portfolio>, RuleProvider, PortfolioProvider {
  private static final Logger LOG = LoggerFactory.getLogger(DummyPortfolioMapLoader.class);

  private static final String PORTFOLIO_NAMES_FILE = "portfolionames.txt";

  private final int numPortfolios;
  private final Portfolio[] benchmarks;
  private final ArrayList<String> portfolioNames;
  private final HashMap<PortfolioKey, Portfolio> portfolioMap = new HashMap<>();
  private final HashMap<RuleKey, ConfigurableRule> ruleMap = new HashMap<>();
  private final HashMap<TradeKey, Trade> tradeMap = new HashMap<>();

  private final PimcoBenchmarkDataSource dataSource;

  private final GroovyPositionFilter anheuserBuschFilter =
      GroovyPositionFilter.of("s.issuer == 'Anheuser-Busch'");
  private final GroovyPositionFilter belowAA3Filter =
      GroovyPositionFilter.of("s.rating1Value < 88");
  private final GroovyPositionFilter belowInvestmentGradeFilter =
      GroovyPositionFilter.of("s.rating1Value < 70");
  private final GroovyPositionFilter emergingMarketsFilter =
      GroovyPositionFilter.of("s.region == 'Emerging Markets'");
  private final GroovyPositionFilter mbsFilter =
      GroovyPositionFilter.of("s.country == 'US' && s.sector == 'Securitized'");
  private final GroovyPositionFilter nonUsInternalBondFilter =
      GroovyPositionFilter.of("s.country != 'US' && s.sector == 'Internal Bond'");
  private final GroovyPositionFilter nonUsCurrencyForwardFilter =
      GroovyPositionFilter.of("s.country != 'US' && s.sector == 'Currency'");
  private final GroovyPositionFilter restrictedCurrencyFilter =
      GroovyPositionFilter.of("!['PLN', 'RON', 'RUB'].contains(s.currency)");
  private final SecurityAttributeGroupClassifier issuerClassifier =
      new SecurityAttributeGroupClassifier(SecurityAttribute.issuer);
  private final SecurityAttributeGroupClassifier sectorClassifier =
      new SecurityAttributeGroupClassifier(SecurityAttribute.sector);
  private final EvaluationGroupClassifier top5CurrencyClassifier =
      new TopNSecurityAttributeAggregator(SecurityAttribute.currency, 5);
  private final EvaluationGroupClassifier top20IssuerClassifier =
      new TopNSecurityAttributeAggregator(SecurityAttribute.issuer, 20);

  private final AtomicInteger portfolioIndex = new AtomicInteger();

  /**
   * Creates a new {@link DummyPortfolioMapLoader}.
   *
   * @param numPortfolios the number of portfolios to be created
   * @throws IOException if {@link Portfolio} data could not be loaded
   */
  public DummyPortfolioMapLoader(int numPortfolios) throws IOException {
    this.numPortfolios = numPortfolios;

    dataSource = PimcoBenchmarkDataSource.getInstance();

    benchmarks =
        new Portfolio[] {
          null,
          dataSource.getBenchmark(PimcoBenchmarkDataSource.EMAD_KEY),
          dataSource.getBenchmark(PimcoBenchmarkDataSource.GLAD_KEY),
          dataSource.getBenchmark(PimcoBenchmarkDataSource.ILAD_KEY),
          dataSource.getBenchmark(PimcoBenchmarkDataSource.PGOV_KEY)
        };

    portfolioNames = new ArrayList<>(1000);
    try (BufferedReader portfolioNameReader =
        new BufferedReader(
            new InputStreamReader(
                getClass()
                    .getClassLoader()
                    .getResourceAsStream(
                        PimcoBenchmarkDataSource.RESOURCE_PATH + PORTFOLIO_NAMES_FILE)))) {
      String portfolioName;
      while ((portfolioName = portfolioNameReader.readLine()) != null) {
        portfolioNames.add(portfolioName);
      }
    }
  }

  @Override
  public void delete(PortfolioKey key) {
    // nothing to do
  }

  @Override
  public void deleteAll(Collection<PortfolioKey> keys) {
    // nothing to do
  }

  @Override
  public Portfolio getPortfolio(@Nonnull PortfolioKey key) {
    return portfolioMap.get(key);
  }

  @Override
  public ConfigurableRule getRule(@Nonnull RuleKey key) {
    return ruleMap.get(key);
  }

  /**
   * Obtains the generated trades.
   *
   * @return a {@link Map} relating each {@link TradeKey} to its corresponding {@link Trade}
   */
  public Map<TradeKey, Trade> getGeneratedTrades() {
    return tradeMap;
  }

  @Override
  public Portfolio load(PortfolioKey key) {
    return portfolioMap.computeIfAbsent(
        key,
        p -> {
          // if the key corresponds to a benchmark, return the corresponding benchmark
          Portfolio benchmark = dataSource.getBenchmark(p);
          if (benchmark != null) {
            return benchmark;
          }

          Collection<Security> securities = dataSource.getSecurityMap().values();

          synchronized (tradeMap) {
            if (tradeMap.isEmpty()) {
              // generate Trades, from which TaxLots and Positions will ultimately be derived

              LOG.info("generating trades");

              Random random = new Random(0);
              ArrayList<Security> securitiesList = new ArrayList<>(securities);

              LocalDate lastTradeDate = LocalDate.of(2021, 7, 30);
              LocalDate tradeDate = LocalDate.of(2016, 1, 1);

              int totalAllocations = 0;

              while (!tradeDate.isAfter(lastTradeDate)) {
                // generate a random number of Trades involving a random number of Portfolios
                int numTrades = 20 + (int) (Math.abs(random.nextGaussian()) * 20);
                for (int i = 0; i < numTrades; i++) {
                  int numAllocations = 1 + (int) (Math.abs(random.nextGaussian()) * 80);
                  Map<PortfolioKey, Transaction> transactions = new HashMap<>(numAllocations * 2);
                  // try to make some Securities much more likely than others
                  int index = (int) (Math.pow(random.nextDouble(), 8) * securitiesList.size());
                  Security security = securitiesList.get(index);
                  for (int j = 0; j < numAllocations; j++) {
                    // don't care about duplication here; we'll just have fewer TaxLots in the Trade
                    int portfolioNum = 1 + random.nextInt(numPortfolios);
                    PortfolioKey portfolioKey = new PortfolioKey("test" + portfolioNum, 1);

                    // generate an amount in the approximate range of 100.00 ~ 10_000.00
                    double amount =
                        100
                            + (long)
                                    (Math.pow(10, 2 + random.nextInt(3))
                                        * random.nextDouble()
                                        * 100)
                                / 100d;
                    TaxLot taxLot = new TaxLot(amount, security.getKey());
                    Transaction transaction = new Transaction(portfolioKey, List.of(taxLot));
                    if (transactions.put(portfolioKey, transaction) == null) {
                      totalAllocations++;
                    }
                  }
                  Trade trade = new Trade(tradeDate, tradeDate.plusDays(5), transactions);
                  tradeMap.put(trade.getKey(), trade);
                }

                if (tradeDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
                  // advance to following Monday
                  tradeDate = tradeDate.plusDays(3);
                } else {
                  // advance to next day
                  tradeDate = tradeDate.plusDays(1);
                }
              }
              LOG.info("created {} trades with {} allocations", tradeMap.size(), totalAllocations);
            }
          }

          // derive positions for the current Portfolio from the previously generated Trades, first
          // grouping all TaxLots by their Security...
          Map<SecurityKey, List<TaxLot>> securityTaxLotsMap =
              tradeMap.values().stream()
                  .flatMap(
                      t -> {
                        Transaction transaction = t.getTransaction(p);
                        if (transaction == null) {
                          return Stream.empty();
                        }
                        return transaction.getPositions();
                      })
                  .collect(Collectors.groupingBy(TaxLot::getSecurityKey));
          // ...and then collecting the TaxLots of the same Security into a PortfolioPosition
          Set<PortfolioPosition> positions =
              securityTaxLotsMap.values().stream()
                  .map(PortfolioPosition::new)
                  .collect(Collectors.toSet());

          // generate random Portfolio details
          int seed;
          try {
            seed = Integer.parseInt(p.getId().substring(4));
          } catch (Exception e) {
            seed = 0;
          }
          Random random = new Random(seed);
          Portfolio portfolioBenchmark = benchmarks[random.nextInt(5)];
          Set<ConfigurableRule> rules = generateRules(random, portfolioBenchmark != null);
          Portfolio portfolio =
              new Portfolio(
                  p,
                  portfolioNames.get(portfolioIndex.getAndIncrement()),
                  positions,
                  portfolioBenchmark,
                  rules);
          LOG.info("created portfolio {} with {} positions", p, portfolio.size());

          return portfolio;
        });
  }

  @Override
  public Map<PortfolioKey, Portfolio> loadAll(Collection<PortfolioKey> keys) {
    LOG.info("loading portfolios for {} keys", keys.size());
    return keys.stream().collect(Collectors.toMap(k -> k, this::load));
  }

  @Override
  public Iterable<PortfolioKey> loadAllKeys() {
    LOG.info("loading all keys");

    // This Iterable produces NUM_PORTFOLIOS + 4 Portfolio IDs; the first four are the PIMCO
    // benchmarks and the remaining NUM_PORTFOLIOS are randomly-generated Portfolios
    return () ->
        new Iterator<>() {
          private int currentPosition = -4;

          @Override
          public boolean hasNext() {
            return currentPosition < numPortfolios;
          }

          @Override
          public PortfolioKey next() {
            return switch (++currentPosition) {
              case -3 -> PimcoBenchmarkDataSource.EMAD_KEY;
              case -2 -> PimcoBenchmarkDataSource.GLAD_KEY;
              case -1 -> PimcoBenchmarkDataSource.ILAD_KEY;
              case 0 -> PimcoBenchmarkDataSource.PGOV_KEY;
              default -> new PortfolioKey("test" + currentPosition, 1);
            };
          }
        };
  }

  @Override
  public void store(PortfolioKey key, Portfolio value) {
    // nothing to do
  }

  @Override
  public void storeAll(Map<PortfolioKey, Portfolio> map) {
    // nothing to do
  }

  /**
   * Generates a random-ish set of {@link Rule}s.
   *
   * @param random the random number generator to use
   * @param hasBenchmark {@code true} if the target {@link Portfolio} has an associated benchmark,
   *     {@code false} otherwise
   * @return a new {@link Set} of random {@link Rule}s
   */
  protected Set<ConfigurableRule> generateRules(Random random, boolean hasBenchmark) {
    HashSet<ConfigurableRule> rules = new HashSet<>(60);

    // with high probability, only investment-grade Securities will be permitted (disallow rating <
    // 70)
    double rand = random.nextDouble();
    if (rand < 0.9) {
      rules.add(
          new MarketValueRule(
              randomRuleKey(random),
              "Investment-Grade Only",
              belowInvestmentGradeFilter,
              null,
              0d));
    }

    // with high probability, for each issuer, a maximum concentration of Securities below AA3 (88)
    // will be set
    rand = random.nextDouble();
    if (rand < 0.5) {
      // allow some concentration
      rules.add(
          new ConcentrationRule(
              randomRuleKey(random),
              "<= 20% Concentration in < AA3 per Issuer",
              belowAA3Filter,
              null,
              0.2,
              issuerClassifier));
    } else if (rand < 0.9) {
      // allow less concentration
      rules.add(
          new ConcentrationRule(
              randomRuleKey(random),
              "<= 10% Concentration in < AA3 per Issuer",
              belowAA3Filter,
              null,
              0.1,
              issuerClassifier));
    }

    // with high probability, a minimum level of average quality will be required
    rand = random.nextDouble();
    if (hasBenchmark) {
      if (rand < 0.5) {
        // require average quality somewhat close to the benchmark
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Average Quality >= 85% of Benchmark",
                null,
                SecurityAttribute.rating1Value,
                0.85,
                null,
                null));
      } else if (rand < 0.8) {
        // require average quality somewhat closer to the benchmark
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Average Quality >= 90% of Benchmark",
                null,
                SecurityAttribute.rating1Value,
                0.9,
                null,
                null));
      }
    } else {
      if (rand < 0.5) {
        // require somewhat high average quality
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Average Quality >= A3",
                null,
                SecurityAttribute.rating1Value,
                79d,
                null,
                null));
      } else if (rand < 0.8) {
        // require somewhat higher average quality
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Average Quality >= A1",
                null,
                SecurityAttribute.rating1Value,
                85d,
                null,
                null));
      }
    }

    // with high probability, a lower limit on average yield will be set
    rand = random.nextDouble();
    if (hasBenchmark) {
      if (rand < 0.5) {
        // require yield somewhat close to benchmark
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Yield >= 90% of Benchmark",
                null,
                SecurityAttribute.yield,
                0.9,
                null,
                null));
      } else if (rand < 0.8) {
        // require yield somewhat closer to benchmark
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Yield >= 95% of Benchmark",
                null,
                SecurityAttribute.yield,
                0.95,
                null,
                null));
      }
    } else {
      if (rand < 0.5) {
        // require a modest yield
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Yield >= 4.0",
                null,
                SecurityAttribute.yield,
                4d,
                null,
                null));
      } else if (rand < 0.8) {
        // require a higher yield
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Yield >= 6.0",
                null,
                SecurityAttribute.yield,
                6d,
                null,
                null));
      }
    }

    // with high probability, the average days to maturity will be limited relative to the
    // benchmark, if applicable, otherwise to some fixed limit
    rand = random.nextDouble();
    if (hasBenchmark) {
      if (rand < 0.5) {
        // require average days to maturity somewhat close to the benchmark
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Average Days to Maturity within 20% of Benchmark",
                null,
                SecurityAttribute.maturityDate,
                0.8,
                1.2,
                null));
      } else if (rand < 0.8) {
        // require average days to maturity somewhat closer to the benchmark
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Average Days to Maturity within 10% of Benchmark",
                null,
                SecurityAttribute.maturityDate,
                0.9,
                1.1,
                null));
      }
    } else {
      if (rand < 0.5) {
        // require average days to maturity < 7 years
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Average Days to Maturity < 7 Years",
                null,
                SecurityAttribute.maturityDate,
                null,
                7 * 365d,
                null));
      } else if (rand < 0.8) {
        // require average days to maturity < 8 years
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Average Days to Maturity < 8 Years",
                null,
                SecurityAttribute.maturityDate,
                null,
                8 * 365d,
                null));
      }
    }

    // with moderate probability, an upper limit on average duration will be set
    rand = random.nextDouble();
    if (hasBenchmark) {
      if (rand < 0.2) {
        // require duration somewhat close to benchmark
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Duration within 20% of Benchmark",
                null,
                SecurityAttribute.duration,
                0.8,
                1.2,
                null));
      } else if (rand < 0.5) {
        // require duration somewhat closer to benchmark
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Duration within 10% of Benchmark",
                null,
                SecurityAttribute.duration,
                0.9,
                1.1,
                null));
      }
    } else {
      if (rand < 0.2) {
        // require a relatively low duration
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Duration < 3.0",
                null,
                SecurityAttribute.duration,
                null,
                3d,
                null));
      } else if (rand < 0.5) {
        // allow a somewhat higher duration
        rules.add(
            new WeightedAverageRule<>(
                randomRuleKey(random),
                "Duration < 5.0",
                null,
                SecurityAttribute.duration,
                null,
                5d,
                null));
      }
    }

    // with moderate probability, concentrations in sectors will be limited, either absolutely or
    // relative to the benchmark
    rand = random.nextDouble();
    if (hasBenchmark) {
      if (rand < 0.2) {
        // require somewhat close to the benchmark
        rules.add(
            new ConcentrationRule(
                randomRuleKey(random),
                "Concentration per Sector Within 20% of Benchmark",
                null,
                0.8,
                1.2,
                sectorClassifier));
      } else if (rand < 0.5) {
        // require somewhat closer to the benchmark
        rules.add(
            new ConcentrationRule(
                randomRuleKey(random),
                "Concentration per Sector Within 10% of Benchmark",
                null,
                0.9,
                1.1,
                sectorClassifier));
      }
    } else {
      if (rand < 0.2) {
        // impose a modest concentration limit
        rules.add(
            new ConcentrationRule(
                randomRuleKey(random),
                "Concentration per Sector < 10%",
                null,
                null,
                0.1,
                sectorClassifier));
      } else if (rand < 0.5) {
        // impose a stricter concentration limit
        rules.add(
            new ConcentrationRule(
                randomRuleKey(random),
                "Concentration per Sector < 5%",
                null,
                null,
                0.05,
                sectorClassifier));
      }
    }

    // with moderate probability, MBS will be limited or disallowed entirely
    rand = random.nextDouble();
    if (rand < 0.1) {
      // disallow MBS altogether
      rules.add(new MarketValueRule(randomRuleKey(random), "No MBS", mbsFilter, null, 0d));
    } else if (rand < 0.3) {
      // permit a limited concentration in MBS
      rules.add(
          new ConcentrationRule(
              randomRuleKey(random), "MBS <= 1% of Portfolio", mbsFilter, null, 0.01, null));
    } else if (rand < 0.5) {
      // permit a little more
      rules.add(
          new ConcentrationRule(
              randomRuleKey(random), "MBS <= 2% of Portfolio", mbsFilter, null, 0.02, null));
    }

    // with moderate probability, Emerging Markets will be limited or disallowed entirely
    rand = random.nextDouble();
    if (rand < 0.1) {
      // disallow Emerging Markets altogether
      rules.add(
          new MarketValueRule(
              randomRuleKey(random), "No Emerging Markets", emergingMarketsFilter, null, 0d));
    } else if (rand < 0.4) {
      if (hasBenchmark) {
        // permit Emerging Markets relative to the benchmark
        rules.add(
            new ConcentrationRule(
                randomRuleKey(random),
                "Emerging Markets <= 120% of Benchmark",
                emergingMarketsFilter,
                null,
                1.2,
                null));
      } else {
        // permit a limited concentration in Emerging Markets
        if (random.nextBoolean()) {
          // permit a little
          rules.add(
              new ConcentrationRule(
                  randomRuleKey(random),
                  "Emerging Markets <= 10% of Portfolio",
                  emergingMarketsFilter,
                  null,
                  0.1,
                  null));
        } else {
          // permit a little more
          rules.add(
              new ConcentrationRule(
                  randomRuleKey(random),
                  "Emerging Markets <= 20% of Portfolio",
                  emergingMarketsFilter,
                  null,
                  0.2,
                  null));
        }
      }
    }

    // with moderate probability, for Portfolios with a benchmark, concentrations in the top 20
    // issuers will be restricted relative to the benchmark
    rand = random.nextDouble();
    if (hasBenchmark) {
      if (rand < 0.2) {
        rules.add(
            new ConcentrationRule(
                randomRuleKey(random),
                "Top 20 Issuer Concentrations Within 20% of Benchmark",
                null,
                0.8,
                1.2,
                top20IssuerClassifier));
      } else if (rand < 0.4) {
        rules.add(
            new ConcentrationRule(
                randomRuleKey(random),
                "Top 20 Issuer Concentrations Within 30% of Benchmark",
                null,
                0.7,
                1.3,
                top20IssuerClassifier));
      }
    }

    // with moderate probability, for Portfolios with a benchmark, concentrations in the top 5
    // currencies will be restricted relative to the benchmark
    rand = random.nextDouble();
    if (hasBenchmark) {
      if (rand < 0.2) {
        rules.add(
            new ConcentrationRule(
                randomRuleKey(random),
                "Top 5 Currency Concentrations Within 20% of Benchmark",
                null,
                0.8,
                1.2,
                top5CurrencyClassifier));
      } else if (rand < 0.4) {
        rules.add(
            new ConcentrationRule(
                randomRuleKey(random),
                "Top 5 Currency Concentrations Within 30% of Benchmark",
                null,
                0.7,
                1.3,
                top5CurrencyClassifier));
      }
    }

    // with moderate probability, issues in certain currencies will be disallowed
    rand = random.nextDouble();
    if (rand < 0.5) {
      rules.add(
          new MarketValueRule(
              randomRuleKey(random),
              "No PLN-, RON- or RUB-Denominated Issues",
              restrictedCurrencyFilter,
              null,
              0d));
    }

    // with moderate probability, non-US internal bonds will be disallowed
    rand = random.nextDouble();
    if (rand < 0.5) {
      rules.add(
          new MarketValueRule(
              randomRuleKey(random),
              "No Non-US Internal Bonds",
              nonUsInternalBondFilter,
              null,
              0d));
    }

    // with low probability, non-US currency forwards will be disallowed
    rand = random.nextDouble();
    if (rand < 0.3) {
      rules.add(
          new MarketValueRule(
              randomRuleKey(random),
              "No Non-US Currency Forwards",
              nonUsCurrencyForwardFilter,
              null,
              0d));
    }

    // with low probability, Anheuser-Busch issues will be disallowed
    rand = random.nextDouble();
    if (rand < 0.2) {
      rules.add(
          new MarketValueRule(
              randomRuleKey(random), "No Anheuser-Busch Issues", anheuserBuschFilter, null, 0d));
    }

    // add at least two and as many as five eligibility list rules
    int numEligibilityRules = 2 + random.nextInt(4);
    for (int i = 0; i < numEligibilityRules; i++) {
      int listTypeIndex = random.nextInt(3);
      String listType =
          switch (listTypeIndex) {
            case 0 -> "Country";
            case 1 -> "Issuer";
            default -> "Security";
          };
      // generated list names are e.g. country1, country2, ...; issuer1, issuer2, ...; etc.
      String listName = listType.toLowerCase() + (1 + random.nextInt(100));

      GroovyPositionFilter eligibilityFilter =
          GroovyPositionFilter.of("!s.in" + listType + "List" + "(\"" + listName + "\")");
      rules.add(
          new MarketValueRule(
              randomRuleKey(random),
              listType + " Restriction List " + listName,
              eligibilityFilter,
              null,
              0d));
    }

    ruleMap.putAll(rules.stream().collect(Collectors.toMap(ConfigurableRule::getKey, r -> r)));
    return rules;
  }

  /**
   * Generates a random rule key using the given random number generator.
   *
   * @param random the random number generator with which to generate a key
   * @return a randomly generated {@link RuleKey}
   */
  @Nonnull
  protected RuleKey randomRuleKey(Random random) {
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    String string = Base64.getEncoder().encodeToString(bytes);

    return new RuleKey(string);
  }
}
