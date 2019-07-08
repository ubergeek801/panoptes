package org.slaq.slaqworx.panoptes.rule;

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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Test;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.RatingNotch;
import org.slaq.slaqworx.panoptes.asset.RatingScale;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.calc.WeightedAveragePositionCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RuleEvaluatorPerformanceTest performs randomized rule evaluation tests and captures some
 * benchmarking metrics, using publicly available benchmark constituent data for certain PIMCO
 * benchmarks.
 *
 * @author jeremy
 */
public class RuleEvaluatorPerformanceTest {
	private static final Logger LOG = LoggerFactory.getLogger(RuleEvaluatorPerformanceTest.class);

	private static final Random random = new Random();
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

		Portfolio[] benchmarks =
				new Portfolio[] { emadBenchmark, gladBenchmark, iladBenchmark, pgovBenchmark };

		HashSet<Portfolio> portfolios = new HashSet<>(1000);
		for (int i = 1; i <= 1000; i++) {
			Set<Position> positions = generatePositions(gladBenchmark);
			Set<Rule> rules = generateRules();
			Portfolio portfolio =
					new Portfolio("test" + i, positions, benchmarks[random.nextInt(4)], rules);
			portfolios.add(portfolio);
		}
		LOG.info("created {} test portfolios", portfolios.size());

		RuleEvaluator evaluator = new RuleEvaluator();
		long startTime = System.currentTimeMillis();
		portfolios.forEach(p -> evaluator.evaluate(p));
		LOG.info("processed portfolios in {} ms", System.currentTimeMillis() - startTime);
	}

	/**
	 * Generates a quasi-random set of Positions from the given Portfolio by selecting a random
	 * subset and fuzzing the amounts.
	 *
	 * @param portfolio
	 *            a Portfolio from which to source Positions
	 * @return a new Set of quasi-random Positions
	 */
	protected Set<Position> generatePositions(Portfolio portfolio) {
		return portfolio.getPositions().filter(p -> Math.random() < 0.4).map(p -> {
			return new Position(p.getAmount() * (0.5 + Math.random()), p.getSecurity());
		}).collect(Collectors.toUnmodifiableSet());
	}

	protected Set<Rule> generateRules() {
		HashSet<Rule> rules = new HashSet<>(200);

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
			case 4:
				compareAttribute = SecurityAttribute.ratingValue;
			default:
				compareAttribute = SecurityAttribute.duration;
			}

			if (filter != null) {
				rules.add(new ConcentrationRule(null, "randomly generated rule " + i, filter, 0.8,
						1.2));
			} else if (compareAttribute != null) {
				rules.add(new ValueRule(null, "randomly generated rule " + i, null,
						compareAttribute, 0.8, 1.2));
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
		LOG.info("loaded {} positions for EMAD benchmark (total amount {}, avg rating {})",
				positions.size(), usdFormatter.format(totalAmount), averageRating);

		return benchmark;
	}
}
