package org.slaq.slaqworx.panoptes.offline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.RatingNotch;
import org.slaq.slaqworx.panoptes.asset.RatingScale;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.calc.WeightedAveragePositionCalculator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * {@code PimcoBenchmarkDataSource} provides access to the data obtained from the PIMCO benchmark
 * constituent files.
 *
 * @author jeremy
 */
public class PimcoBenchmarkDataSource implements PortfolioProvider, SecurityProvider {
    private static final Logger LOG = LoggerFactory.getLogger(PimcoBenchmarkDataSource.class);

    private static final String EMAD_CONSTITUENTS_FILE = "PIMCO_EMAD_Constituents_07-02-2019.tsv";
    private static final String GLAD_CONSTITUENTS_FILE = "PIMCO_GLAD_Constituents_07-02-2019.tsv";
    private static final String ILAD_CONSTITUENTS_FILE = "PIMCO_ILAD_Constituents_07-02-2019.tsv";
    private static final String PGOV_CONSTITUENTS_FILE = "PIMCO_PGOV_Constituents_07-02-2019.tsv";

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DecimalFormat usdFormatter = new DecimalFormat("#,##0.00");
    private static final RatingScale pimcoRatingScale;
    private static final Random random = new Random(0);

    private static final BigDecimal MARKET_VALUE_MULTIPLIER = new BigDecimal("10000.00");

    public static final String RESOURCE_PATH = "offline/";

    public static final PortfolioKey EMAD_KEY = new PortfolioKey("EMAD", 1);
    public static final PortfolioKey GLAD_KEY = new PortfolioKey("GLAD", 1);
    public static final PortfolioKey ILAD_KEY = new PortfolioKey("ILAD", 1);
    public static final PortfolioKey PGOV_KEY = new PortfolioKey("PGOV", 1);

    static {
        // these rating symbols are used in the PIMCO benchmarks; the numeric equivalents are a
        // fabrication
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

    private static PimcoBenchmarkDataSource instance;

    /**
     * Obtains the singleton instance of the {@code PimcoBenchmarkDataSource}.
     *
     * @return the {@code PimcoBenchmarkDataSource} instance
     * @throws IOException
     *             if the data could not be read
     */
    public synchronized static PimcoBenchmarkDataSource getInstance() throws IOException {
        if (instance == null) {
            instance = new PimcoBenchmarkDataSource();
        }

        return instance;
    }

    private final HashMap<SecurityKey, Security> securityMap = new HashMap<>();
    private final HashMap<PortfolioKey, Portfolio> benchmarkMap = new HashMap<>();

    /**
     * Creates a new {@code PimcoBenchmarkDataSource}. Restricted to enforce singleton semantics.
     *
     * @throws IOException
     *             if the data could not be read
     */
    private PimcoBenchmarkDataSource() throws IOException {
        // load the PIMCO benchmarks, which are also a source of Security information

        loadPimcoBenchmark(GLAD_KEY, GLAD_CONSTITUENTS_FILE);
        loadPimcoBenchmark(EMAD_KEY, EMAD_CONSTITUENTS_FILE);
        loadPimcoBenchmark(ILAD_KEY, ILAD_CONSTITUENTS_FILE);
        loadPimcoBenchmark(PGOV_KEY, PGOV_CONSTITUENTS_FILE);
        LOG.info("loaded {} distinct securities", securityMap.size());
    }

    /**
     * Obtains the benchmark with the given ID.
     *
     * @param benchmarkKey
     *            the key of the benchmark to be obtained
     * @return the benchmark with the given ID, or {@code null} if it does not exist
     */
    public Portfolio getBenchmark(PortfolioKey benchmarkKey) {
        return benchmarkMap.get(benchmarkKey);
    }

    @Override
    public Portfolio getPortfolio(PortfolioKey key) {
        // we only know about benchmarks
        return getBenchmark(key);
    }

    @Override
    public Security getSecurity(SecurityKey key, EvaluationContext evaluationContext) {
        return securityMap.get(key);
    }

    /**
     * Obtains a {@code Map} mapping security ID to its corresponding {@code Security}.
     *
     * @return a {@code Map} of security ID to {@code Security}
     */
    public Map<SecurityKey, Security> getSecurityMap() {
        return securityMap;
    }

    /**
     * Calculates the price of a {@code Security} given the specified attributes, normalized to USD
     * 100.
     *
     * @return the calculated price
     */
    protected BigDecimal calculatePrice(LocalDate asOfDate, LocalDate maturityDate,
            BigDecimal effectiveYield) {
        return BigDecimal.valueOf(36500 / (365 + asOfDate.until(maturityDate, ChronoUnit.DAYS)
                * effectiveYield.doubleValue() / 100)).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Loads data from the given PIMCO constituents file (converted to tab-separated values) and
     * creates a new {@code Portfolio} with the data, populating the {@code benchmarkMap} with the
     * created {@code Portfolio} and the {@code securityMap} with the created {@code Securities}.
     *
     * @param benchmarkKey
     *            the benchmark key
     * @param sourceFile
     *            the name of the source file (on the classpath)
     * @throws IOException
     *             if the file could not be read
     */
    protected void loadPimcoBenchmark(PortfolioKey benchmarkKey, String sourceFile)
            throws IOException {
        HashSet<Position> positions = new HashSet<>();
        double portfolioMarketValue = 0;
        try (BufferedReader constituentReader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH + sourceFile)))) {
            // throw away the header row
            String row = constituentReader.readLine();
            while ((row = constituentReader.readLine()) != null) {
                String[] values = row.split("\\t");
                int column = 0;
                LocalDate asOfDate = LocalDate.parse(values[column++], dateFormatter);
                String cusip = values[column++];
                String isin = values[column++];
                String description = values[column++];
                // Ticker not used
                column++;
                String country = values[column++];
                String region = values[column++];
                // only GLAD has sector
                String sector;
                if (GLAD_CONSTITUENTS_FILE.contentEquals(sourceFile)) {
                    sector = values[column++];
                } else {
                    sector = null;
                }
                String currency = values[column++];
                BigDecimal coupon =
                        new BigDecimal(values[column++]).setScale(2, RoundingMode.HALF_UP);
                LocalDate maturityDate = LocalDate.parse(values[column++], dateFormatter);
                // Face Value Local not used
                column++;
                // Face Value USD not used
                column++;
                // Market Value Local not used
                column++;
                // multiply the market value so it is not tiny
                BigDecimal marketValueUsd = new BigDecimal(values[column++])
                        .multiply(MARKET_VALUE_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
                // Weight not used
                column++;
                String rating1Symbol = values[column++];
                BigDecimal yield =
                        new BigDecimal(values[column++]).setScale(2, RoundingMode.HALF_UP);
                BigDecimal duration =
                        new BigDecimal(values[column++]).setScale(2, RoundingMode.HALF_UP);

                Map<SecurityAttribute<?>, ? super Object> attributes = new HashMap<>();
                attributes.put(SecurityAttribute.cusip, cusip);
                attributes.put(SecurityAttribute.isin, isin);
                attributes.put(SecurityAttribute.description, description);
                attributes.put(SecurityAttribute.country, country);
                attributes.put(SecurityAttribute.region, region);
                attributes.put(SecurityAttribute.sector, sector);
                attributes.put(SecurityAttribute.currency, currency);
                attributes.put(SecurityAttribute.coupon, coupon);
                attributes.put(SecurityAttribute.maturityDate, maturityDate);
                attributes.put(SecurityAttribute.rating1Symbol, rating1Symbol);
                double rating1Value = pimcoRatingScale.getRatingNotch(rating1Symbol).getMiddle();
                attributes.put(SecurityAttribute.rating1Value, rating1Value);

                // manufacture rating2 and rating3 values
                if (random.nextDouble() < 0.8) {
                    String rating2Symbol = pimcoRatingScale
                            .getRatingNotch(rating1Value + random.nextGaussian() * 3).getSymbol();
                    attributes.put(SecurityAttribute.rating2Symbol, rating2Symbol);
                    attributes.put(SecurityAttribute.rating2Value,
                            pimcoRatingScale.getRatingNotch(rating2Symbol).getMiddle());
                }
                if (random.nextDouble() < 0.8) {
                    String rating3Symbol = pimcoRatingScale
                            .getRatingNotch(rating1Value - 1.5 + random.nextGaussian() * 3)
                            .getSymbol();
                    attributes.put(SecurityAttribute.rating3Symbol, rating3Symbol);
                    attributes.put(SecurityAttribute.rating3Value,
                            pimcoRatingScale.getRatingNotch(rating3Symbol).getMiddle());
                }

                attributes.put(SecurityAttribute.yield, yield);
                attributes.put(SecurityAttribute.duration, duration.doubleValue());

                // use the description as the issuer unless the sector is Currency, in which case
                // don't set the issuer
                if (!("Currency".equals(sector))) {
                    attributes.put(SecurityAttribute.issuer, description);
                }

                BigDecimal price = calculatePrice(asOfDate, maturityDate, yield);
                attributes.put(SecurityAttribute.price, price);
                Security security = securityMap.computeIfAbsent(new SecurityKey(isin),
                        i -> new Security(attributes));

                positions.add(new Position(
                        marketValueUsd.divide(price, RoundingMode.HALF_UP).doubleValue(),
                        security.getKey()));
                portfolioMarketValue += marketValueUsd.doubleValue();
            }
        }

        Portfolio benchmark =
                new Portfolio(benchmarkKey, benchmarkKey.getId() + " Benchmark", positions);
        benchmarkMap.put(benchmarkKey, benchmark);

        // average rating is kind of interesting, so let's calculate it
        WeightedAveragePositionCalculator<Double> averageRatingCalc =
                new WeightedAveragePositionCalculator<>(SecurityAttribute.rating1Value);
        String averageRating = pimcoRatingScale
                .getRatingNotch(averageRatingCalc
                        .calculate(benchmark.getPositionsWithContext(new EvaluationContext(this))))
                .getSymbol();
        LOG.info("loaded {} positions for {} benchmark (total amount {}, avg rating {})",
                positions.size(), benchmarkKey, usdFormatter.format(portfolioMarketValue),
                averageRating);
    }
}
