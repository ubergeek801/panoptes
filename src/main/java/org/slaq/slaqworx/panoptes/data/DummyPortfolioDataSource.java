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

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.RatingNotch;
import org.slaq.slaqworx.panoptes.asset.RatingScale;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.calc.WeightedAveragePositionCalculator;

/**
 * DummyPortfolioDataSource provides access to the data obtained from the PIMCO benchmark
 * constituent files.
 *
 * @author jeremy
 */
public class DummyPortfolioDataSource {
    private static final Logger LOG = LoggerFactory.getLogger(DummyPortfolioDataSource.class);

    private static final String EMAD_CONSTITUENTS_FILE = "PIMCO_EMAD_Constituents_07-02-2019.tsv";
    private static final String GLAD_CONSTITUENTS_FILE = "PIMCO_GLAD_Constituents_07-02-2019.tsv";
    private static final String ILAD_CONSTITUENTS_FILE = "PIMCO_ILAD_Constituents_07-02-2019.tsv";
    private static final String PGOV_CONSTITUENTS_FILE = "PIMCO_PGOV_Constituents_07-02-2019.tsv";

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DecimalFormat usdFormatter = new DecimalFormat("#,##0.00");
    private static final RatingScale pimcoRatingScale;

    protected static final String EMAD = "EMAD";
    protected static final String GLAD = "GLAD";
    protected static final String ILAD = "ILAD";
    protected static final String PGOV = "PGOV";

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

    private static DummyPortfolioDataSource instance;

    /**
     * Obtains the singleton instance of the DummyPortfolioDataSource.
     *
     * @return the DummyPortfolioDataSource instance
     * @throws IOException
     *             if the data could not be read
     */
    public synchronized static DummyPortfolioDataSource getInstance() throws IOException {
        if (instance == null) {
            instance = new DummyPortfolioDataSource();
        }

        return instance;
    }

    private final HashMap<String, Security> cusipSecurityMap = new HashMap<>();
    private final HashMap<String, Portfolio> benchmarkMap = new HashMap<>();

    /**
     * Creates a new DummyPortfolioDataSource. Restricted to enforce singleton semantics.
     *
     * @throws IOException
     *             if the data could not be read
     */
    private DummyPortfolioDataSource() throws IOException {
        // load the PIMCO benchmarks, which are also a source of Security information

        Portfolio emadBenchmark =
                loadPimcoBenchmark(EMAD, EMAD_CONSTITUENTS_FILE, cusipSecurityMap);
        Portfolio gladBenchmark =
                loadPimcoBenchmark(GLAD, GLAD_CONSTITUENTS_FILE, cusipSecurityMap);
        Portfolio iladBenchmark =
                loadPimcoBenchmark(ILAD, ILAD_CONSTITUENTS_FILE, cusipSecurityMap);
        Portfolio pgovBenchmark =
                loadPimcoBenchmark(PGOV, PGOV_CONSTITUENTS_FILE, cusipSecurityMap);
        benchmarkMap.put(emadBenchmark.getId(), emadBenchmark);
        benchmarkMap.put(gladBenchmark.getId(), gladBenchmark);
        benchmarkMap.put(iladBenchmark.getId(), iladBenchmark);
        benchmarkMap.put(pgovBenchmark.getId(), pgovBenchmark);
        LOG.info("loaded {} distinct securities", cusipSecurityMap.size());
    }

    /**
     * Obtains the benchmark with the given ID.
     *
     * @param benchmarkName
     *            the ID of the benchmark to be obtained
     * @return the benchmark with the given ID, or null if it does not exist
     */
    public Portfolio getBenchmark(String benchmarkName) {
        return benchmarkMap.get(benchmarkName);
    }

    /**
     * Obtains a Map mapping security ID to its corresponding Security.
     *
     * @return a Map of security ID to Security
     */
    public Map<String, Security> getCusipSecurityMap() {
        return cusipSecurityMap;
    }

    /**
     * Loads data from the given PIMCO constituents file (converted to tab-separated values) and
     * creates a new Portfolio with the data.
     *
     * @param benchmarkName
     *            the benchmark name/ID
     * @param sourceFile
     *            the name of the source file (on the classpath)
     * @param cusipSecurityMap
     *            a Map of CUSIP to Security in which to cache loaded Securities
     * @return a Portfolio consisting of the Positions loaded from the file
     * @throws IOException
     *             if the file could not be read
     */
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

        Portfolio benchmark = new Portfolio(benchmarkName, positions);

        // average rating is kind of interesting, so let's calculate it
        WeightedAveragePositionCalculator averageRatingCalc =
                new WeightedAveragePositionCalculator(SecurityAttribute.ratingValue);
        String averageRating =
                pimcoRatingScale.getRatingNotch(averageRatingCalc.calculate(benchmark)).getSymbol();
        LOG.info("loaded {} positions for {} benchmark (total amount {}, avg rating {})",
                positions.size(), benchmarkName, usdFormatter.format(totalAmount), averageRating);

        return benchmark;
    }
}
