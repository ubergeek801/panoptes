package org.slaq.slaqworx.panoptes.data;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.TestUtil;
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
 * PimcoBenchmarkDataSource provides access to the data obtained from the PIMCO benchmark
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

    protected static final PortfolioKey EMAD_KEY = new PortfolioKey("EMAD", 1);
    protected static final PortfolioKey GLAD_KEY = new PortfolioKey("GLAD", 1);
    protected static final PortfolioKey ILAD_KEY = new PortfolioKey("ILAD", 1);
    protected static final PortfolioKey PGOV_KEY = new PortfolioKey("PGOV", 1);

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
     * Obtains the singleton instance of the PimcoBenchmarkDataSource.
     *
     * @return the PimcoBenchmarkDataSource instance
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
     * Creates a new PimcoBenchmarkDataSource. Restricted to enforce singleton semantics.
     *
     * @throws IOException
     *             if the data could not be read
     */
    private PimcoBenchmarkDataSource() throws IOException {
        // load the PIMCO benchmarks, which are also a source of Security information

        Portfolio emadBenchmark = loadPimcoBenchmark(EMAD_KEY, EMAD_CONSTITUENTS_FILE, securityMap);
        Portfolio gladBenchmark = loadPimcoBenchmark(GLAD_KEY, GLAD_CONSTITUENTS_FILE, securityMap);
        Portfolio iladBenchmark = loadPimcoBenchmark(ILAD_KEY, ILAD_CONSTITUENTS_FILE, securityMap);
        Portfolio pgovBenchmark = loadPimcoBenchmark(PGOV_KEY, PGOV_CONSTITUENTS_FILE, securityMap);
        benchmarkMap.put(EMAD_KEY, emadBenchmark);
        benchmarkMap.put(GLAD_KEY, gladBenchmark);
        benchmarkMap.put(ILAD_KEY, iladBenchmark);
        benchmarkMap.put(PGOV_KEY, pgovBenchmark);
        LOG.info("loaded {} distinct securities", securityMap.size());
    }

    /**
     * Obtains the benchmark with the given ID.
     *
     * @param benchmarkKey
     *            the key of the benchmark to be obtained
     * @return the benchmark with the given ID, or null if it does not exist
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
    public Security getSecurity(SecurityKey key) {
        return securityMap.get(key);
    }

    /**
     * Obtains a Map mapping security ID to its corresponding Security.
     *
     * @return a Map of security ID to Security
     */
    public Map<SecurityKey, Security> getSecurityMap() {
        return securityMap;
    }

    /**
     * Loads data from the given PIMCO constituents file (converted to tab-separated values) and
     * creates a new Portfolio with the data.
     *
     * @param benchmarkKey
     *            the benchmark key
     * @param sourceFile
     *            the name of the source file (on the classpath)
     * @param securityMap
     *            a Map of key to Security in which to cache loaded Securities
     * @return a Portfolio consisting of the Positions loaded from the file
     * @throws IOException
     *             if the file could not be read
     */
    protected Portfolio loadPimcoBenchmark(PortfolioKey benchmarkKey, String sourceFile,
            Map<SecurityKey, Security> securityMap) throws IOException {
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

                Map<SecurityAttribute<?>, ? super Object> attributes = new HashMap<>();
                attributes.put(TestUtil.cusip, cusip);
                attributes.put(TestUtil.isin, isin);
                attributes.put(TestUtil.description, description);
                attributes.put(TestUtil.country, country);
                attributes.put(TestUtil.region, region);
                attributes.put(TestUtil.sector, sector);
                attributes.put(TestUtil.currency, currency);
                attributes.put(TestUtil.coupon, coupon);
                attributes.put(TestUtil.maturityDate, maturityDate);
                attributes.put(TestUtil.ratingSymbol, ratingSymbol);
                attributes.put(TestUtil.ratingValue,
                        pimcoRatingScale.getRatingNotch(ratingSymbol).getMiddle());
                attributes.put(TestUtil.yield, yield);
                attributes.put(TestUtil.duration, duration.doubleValue());
                Security security = new Security(attributes);
                securityMap.put(security.getKey(), security);

                positions.add(new Position(marketValueUsd.doubleValue(), security));
                totalAmount += marketValueUsd.doubleValue();
            }
        }

        Portfolio benchmark =
                new Portfolio(benchmarkKey, benchmarkKey.getId() + " Benchmark", positions);

        // average rating is kind of interesting, so let's calculate it
        WeightedAveragePositionCalculator averageRatingCalc =
                new WeightedAveragePositionCalculator(TestUtil.ratingValue);
        String averageRating = pimcoRatingScale.getRatingNotch(
                averageRatingCalc.calculate(benchmark, new EvaluationContext(this, this, null)))
                .getSymbol();
        LOG.info("loaded {} positions for {} benchmark (total amount {}, avg rating {})",
                positions.size(), benchmarkKey, usdFormatter.format(totalAmount), averageRating);

        return benchmark;
    }
}
