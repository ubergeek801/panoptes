package org.slaq.slaqworx.panoptes.rule;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.RatingNotch;
import org.slaq.slaqworx.panoptes.asset.RatingScale;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.calc.WeightedAveragePositionCalculator;
import org.slaq.slaqworx.panoptes.trade.Trade;
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

    private static final Random random = new Random(0);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DecimalFormat usdFormatter = new DecimalFormat("#,##0.00");
    private static final RatingScale pimcoRatingScale;

    static {
        ArrayList<RatingNotch> notches = new ArrayList<>();
        notches.add(new RatingNotch("AAA", 97));
        notches.add(new RatingNotch("AA1", 94));
        notches.add(new RatingNotch("AA2", 91));
        notches.add(new RatingNotch("AA3", 88));
        notches.add(new RatingNotch("A1", 85));
        notches.add(new RatingNotch("A2", 82));
        notches.add(new RatingNotch("A3", 79));
        notches.add(new RatingNotch("BBB1", 76));
        notches.add(new RatingNotch("BBB2", 73));
        notches.add(new RatingNotch("BBB3", 70));
        notches.add(new RatingNotch("BB1", 67));
        notches.add(new RatingNotch("BB2", 64));
        notches.add(new RatingNotch("BB3", 61));
        notches.add(new RatingNotch("B1", 58));
        notches.add(new RatingNotch("B2", 55));
        notches.add(new RatingNotch("B3", 52));
        notches.add(new RatingNotch("CCC1", 49));
        notches.add(new RatingNotch("CCC2", 46));
        notches.add(new RatingNotch("CCC3", 43));
        notches.add(new RatingNotch("CC", 40));
        notches.add(new RatingNotch("C", 37));
        notches.add(new RatingNotch("D", 0));
        pimcoRatingScale = new RatingScale(notches, 100);
    }

    private static final String EMAD_CONSTITUENTS_FILE = "PIMCO_EMAD_Constituents_07-02-2019.tsv";
    private static final String GLAD_CONSTITUENTS_FILE = "PIMCO_GLAD_Constituents_07-02-2019.tsv";
    private static final String ILAD_CONSTITUENTS_FILE = "PIMCO_ILAD_Constituents_07-02-2019.tsv";
    private static final String PGOV_CONSTITUENTS_FILE = "PIMCO_PGOV_Constituents_07-02-2019.tsv";

    @Test
    public void evaluateRules() throws Exception {
        // Allow a few more threads than processors for the ForkJoinPool common pool, which seems to
        // help throughput a bit. Doing this at runtime instead of through a -D parameter allows us
        // to set this dynamically, hopefully before the common pool initializes.
        int parallelism = (int)(Runtime.getRuntime().availableProcessors() * 1.5);
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
                String.valueOf(parallelism));
        assertEquals("attempt to set parallelism failed", parallelism,
                ForkJoinPool.commonPool().getParallelism());

        HashMap<String, Security> cusipSecurityMap = new HashMap<>();

        Portfolio emadBenchmark =
                loadPimcoBenchmark("EMAD", EMAD_CONSTITUENTS_FILE, cusipSecurityMap);
        Portfolio gladBenchmark =
                loadPimcoBenchmark("GLAD", GLAD_CONSTITUENTS_FILE, cusipSecurityMap);
        Portfolio iladBenchmark =
                loadPimcoBenchmark("ILAD", ILAD_CONSTITUENTS_FILE, cusipSecurityMap);
        Portfolio pgovBenchmark =
                loadPimcoBenchmark("PGOV", PGOV_CONSTITUENTS_FILE, cusipSecurityMap);
        LOG.info("loaded {} distinct securities", cusipSecurityMap.size());

        Portfolio[] benchmarks = new Portfolio[] { null, emadBenchmark, gladBenchmark,
                iladBenchmark, pgovBenchmark };

        ArrayList<Security> securityList = new ArrayList<>(cusipSecurityMap.values());
        HashSet<Portfolio> portfolios = new HashSet<>(2000);
        int totalNumPositions = 0;
        for (int i = 1; i <= 1000; i++) {
            Set<Position> positions = generatePositions(securityList);
            Set<Rule> rules = generateRules();
            Portfolio portfolio =
                    new Portfolio("test" + i, positions, benchmarks[random.nextInt(5)], rules);
            portfolios.add(portfolio);
            totalNumPositions += portfolio.size();
        }
        LOG.info("created {} test portfolios averaging {} positions", portfolios.size(),
                totalNumPositions / portfolios.size());

        RuleEvaluator evaluator = new RuleEvaluator();
        long startTime = System.currentTimeMillis();
        portfolios.forEach(p -> evaluator.evaluate(p, new EvaluationContext()));
        LOG.info("processed {} portfolios in {} ms", portfolios.size(),
                System.currentTimeMillis() - startTime);

        for (int i = 1; i <= 8; i *= 2) {
            ArrayList<Position> positions = new ArrayList<>();
            Security security1 = cusipSecurityMap.values().iterator().next();
            Position position1 = new Position(1000000, security1);
            positions.add(position1);
            TradeEvaluator tradeEvaluator = new TradeEvaluator();
            ArrayList<Transaction> transactions = new ArrayList<>();
            portfolios.stream().limit(i * 100).forEach(portfolio -> {
                Transaction transaction = new Transaction(portfolio, positions);
                transactions.add(transaction);
            });
            Trade trade = new Trade(transactions);

            startTime = System.currentTimeMillis();
            tradeEvaluator.evaluate(trade);
            LOG.info("processed trade with {} allocations in {} ms",
                    trade.getTransactions().count(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Generates a random set of Positions from the given Securities.
     *
     * @param securities
     *            a Collection from which to source Securities
     * @return a new Set of random Positions
     */
    protected Set<Position> generatePositions(ArrayList<Security> securities) {
        ArrayList<Security> securitiesCopy = new ArrayList<>(securities);
        int numPositions = 3000 + random.nextInt(2001);
        HashSet<Position> positions = new HashSet<>(numPositions * 2);
        for (int i = 0; i < numPositions; i++) {
            double amount =
                    (long)((1000 + (Math.pow(10, 3 + random.nextInt(6)) * random.nextDouble()))
                            * 100) / 100d;
            Security security = securitiesCopy.remove(random.nextInt(securitiesCopy.size()));
            positions.add(new Position(amount, security));
        }

        return positions;
    }

    protected Set<Rule> generateRules() {
        HashSet<Rule> rules = new HashSet<>(400);

        for (int i = 1; i <= 200; i++) {
            Predicate<Position> filter = null;
            SecurityAttribute<Double> compareAttribute = null;
            switch (random.nextInt(6)) {
            case 0:
                filter = p -> "USD"
                        .equals(p.getSecurity().getAttributeValue(SecurityAttribute.currency));
                break;
            case 1:
                filter = p -> "BRL"
                        .equals(p.getSecurity().getAttributeValue(SecurityAttribute.currency));
                break;
            case 2:
                filter = p -> p.getSecurity().getAttributeValue(SecurityAttribute.duration) > 3;
                break;
            case 3:
                filter = p -> "Emerging Markets"
                        .equals(p.getSecurity().getAttributeValue(SecurityAttribute.region));
                break;
            case 4:
                compareAttribute = SecurityAttribute.ratingValue;
                break;
            default:
                compareAttribute = SecurityAttribute.duration;
            }

            EvaluationGroupClassifier groupClassifier;
            switch (random.nextInt(9)) {
            case 0:
                groupClassifier = new SecurityAttributeGroupClassifier(SecurityAttribute.currency);
                break;
            case 1:
                // description is a proxy for issuer
                groupClassifier =
                        new SecurityAttributeGroupClassifier(SecurityAttribute.description);
                break;
            case 2:
                groupClassifier = new SecurityAttributeGroupClassifier(SecurityAttribute.region);
                break;
            case 3:
                groupClassifier = new SecurityAttributeGroupClassifier(SecurityAttribute.country);
                break;
            case 4:
                groupClassifier =
                        new TopNSecurityAttributeAggregator(SecurityAttribute.currency, 5);
                break;
            case 5:
                groupClassifier =
                        new TopNSecurityAttributeAggregator(SecurityAttribute.description, 5);
                break;
            case 6:
                groupClassifier = new TopNSecurityAttributeAggregator(SecurityAttribute.region, 5);
                break;
            case 7:
                groupClassifier = new TopNSecurityAttributeAggregator(SecurityAttribute.country, 5);
                break;
            default:
                groupClassifier = null;
            }

            if (filter != null) {
                rules.add(new ConcentrationRule(null, "randomly generated rule " + i, filter, 0.8,
                        1.2, groupClassifier));
            } else if (compareAttribute != null) {
                rules.add(new WeightedAverageRule(null, "randomly generated rule " + i, null,
                        compareAttribute, 0.8, 1.2, groupClassifier));
            }
        }

        return rules;
    }

    protected Portfolio loadPimcoBenchmark(String benchmarkName, String sourceFile,
            Map<String, Security> cusipSecurityMap) throws IOException {
        HashSet<Position> positions = new HashSet<>();
        double totalAmount = 0;
        try (BufferedReader constituentReader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(sourceFile)))) {
            // throw away the header row
            String row = constituentReader.readLine();
            while ((row = constituentReader.readLine()) != null) {
                String[] values = row.split("\\t");
                // As of Date not used
                int column = 0;
                String cusip = values[++column];
                String isin = values[++column];
                String description = values[++column];
                // Ticker not used
                ++column;
                String country = values[++column];
                String region = values[++column];
                // only GLAD has sector
                String sector;
                if (GLAD_CONSTITUENTS_FILE.contentEquals(sourceFile)) {
                    sector = values[++column];
                } else {
                    sector = null;
                }
                String currency = values[++column];
                BigDecimal coupon =
                        new BigDecimal(values[++column]).setScale(2, RoundingMode.HALF_UP);
                LocalDate maturityDate = LocalDate.parse(values[++column], dateFormatter);
                // Face Value Local not used
                ++column;
                // Face Value USD not used
                ++column;
                // Market Value Local not used
                ++column;
                BigDecimal marketValueUsd =
                        new BigDecimal(values[++column]).setScale(2, RoundingMode.HALF_UP);
                // Weight not used
                ++column;
                String ratingSymbol = values[++column];
                BigDecimal yield =
                        new BigDecimal(values[++column]).setScale(2, RoundingMode.HALF_UP);
                BigDecimal duration =
                        new BigDecimal(values[++column]).setScale(2, RoundingMode.HALF_UP);

                Security security = cusipSecurityMap.computeIfAbsent(cusip, c -> {
                    Map<SecurityAttribute<?>, ? super Object> attributes = new HashMap<>();
                    attributes.put(SecurityAttribute.isin, isin);
                    attributes.put(SecurityAttribute.description, description);
                    attributes.put(SecurityAttribute.country, country);
                    attributes.put(SecurityAttribute.region, region);
                    attributes.put(SecurityAttribute.sector, sector);
                    attributes.put(SecurityAttribute.currency, currency);
                    attributes.put(SecurityAttribute.coupon, coupon);
                    attributes.put(SecurityAttribute.maturityDate, maturityDate);
                    attributes.put(SecurityAttribute.ratingSymbol, ratingSymbol);
                    attributes.put(SecurityAttribute.ratingValue,
                            pimcoRatingScale.getRatingNotch(ratingSymbol).getMiddle());
                    attributes.put(SecurityAttribute.yield, yield);
                    attributes.put(SecurityAttribute.duration, duration.doubleValue());

                    return new Security(cusip, attributes);
                });

                positions.add(new Position(marketValueUsd.doubleValue(), security));
                totalAmount += marketValueUsd.doubleValue();
            }
        }

        WeightedAveragePositionCalculator averageRatingCalc =
                new WeightedAveragePositionCalculator(SecurityAttribute.ratingValue);

        Portfolio benchmark = new Portfolio(benchmarkName, positions);
        String averageRating =
                pimcoRatingScale.getRatingNotch(averageRatingCalc.calculate(benchmark)).getSymbol();
        LOG.info("loaded {} positions for {} benchmark (total amount {}, avg rating {})",
                positions.size(), benchmarkName, usdFormatter.format(totalAmount), averageRating);

        return benchmark;
    }
}
