package org.slaq.slaqworx.panoptes.calc;

import java.util.Set;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * WeightedAveragePositionCalculator is a PositionCalculator that determines the weighted average of
 * a Position collection. It relies on the Positions already having had their weighted amounts
 * pre-calculated (by the associated Portfolio or some other means).
 *
 * @author jeremy
 */
public class WeightedAveragePositionCalculator extends PositionCalculator<Double> {
	private static class AmountAccumulator {
		DoubleAdder weightedAmount = new DoubleAdder();
		DoubleAdder amount = new DoubleAdder();
	}

	private class WeightedAveragePositionCollector
			implements Collector<Position, AmountAccumulator, Double> {
		@Override
		public BiConsumer<AmountAccumulator, Position> accumulator() {
			return (a, p) -> {
				Double attributeValue = p.getSecurity().getAttributeValue(getCompareAttribute());
				if (attributeValue == null) {
					return;
				}
				a.weightedAmount.add(p.getAmount() * attributeValue);
				a.amount.add(p.getAmount());
			};
		}

		@Override
		public Set<Characteristics> characteristics() {
			return Set.of(Characteristics.CONCURRENT, Characteristics.UNORDERED);
		}

		@Override
		public BinaryOperator<AmountAccumulator> combiner() {
			return (a1, a2) -> {
				a1.weightedAmount.add(a2.weightedAmount.doubleValue());
				a1.amount.add(a2.amount.doubleValue());
				return a1;
			};
		}

		@Override
		public Function<AmountAccumulator, Double> finisher() {
			return a -> a.weightedAmount.doubleValue() / a.amount.doubleValue();
		}

		@Override
		public Supplier<AmountAccumulator> supplier() {
			return AmountAccumulator::new;
		}
	}

	public WeightedAveragePositionCalculator(SecurityAttribute<Double> compareAttribute) {
		super(compareAttribute);
	}

	@Override
	public double calc(Portfolio portfolio, Predicate<? super Position> positionFilter) {
		return portfolio.getPositions().filter(positionFilter)
				.collect(new WeightedAveragePositionCollector());
	}
}
