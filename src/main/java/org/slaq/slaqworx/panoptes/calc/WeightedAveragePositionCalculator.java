package org.slaq.slaqworx.panoptes.calc;

import java.util.Set;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;

/**
 * {@code WeightedAveragePositionCalculator} is a {@code PositionCalculator} that determines the
 * weighted average of a {@code Position} collection with respect to some {@code SecurityAttribute}.
 *
 * @author jeremy
 */
public class WeightedAveragePositionCalculator extends PositionCalculator<Double> {
    /**
     * {@code AccountAccumulator} accumulates the weighted and raw sums from the visited
     * {@code Position}s.
     */
    private static class AmountAccumulator {
        DoubleAdder weightedMarketValue = new DoubleAdder();
        DoubleAdder marketValue = new DoubleAdder();
    }

    /**
     * {@code WeightedAveragePositionCollector} is a {@code Collector} that operates on a
     * {@code Stream} of {@code Position}s to calculate a weighted average of the given attribute.
     *
     * @author jeremy
     */
    private class WeightedAveragePositionCollector
            implements Collector<PositionEvaluationContext, AmountAccumulator, Double> {
        /**
         * Creates a new {@code WeightedAveragePositionCollector}.
         */
        public WeightedAveragePositionCollector() {
            // nothing to do
        }

        @Override
        public BiConsumer<AmountAccumulator, PositionEvaluationContext> accumulator() {
            // accumulate the Position's amount and weighted amount
            return (a, c) -> {
                Position p = c.getPosition();
                Double attributeValue =
                        p.getSecurity().getAttributeValue(getCalculationAttribute());
                if (attributeValue == null) {
                    return;
                }
                a.weightedMarketValue.add(p.getMarketValue() * attributeValue);
                a.marketValue.add(p.getMarketValue());
            };
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.CONCURRENT, Characteristics.UNORDERED);
        }

        @Override
        public BinaryOperator<AmountAccumulator> combiner() {
            // combine (sum) two accumulators into one
            return (a1, a2) -> {
                a1.weightedMarketValue.add(a2.weightedMarketValue.doubleValue());
                a1.marketValue.add(a2.marketValue.doubleValue());
                return a1;
            };
        }

        @Override
        public Function<AmountAccumulator, Double> finisher() {
            // calculate the weighted average
            return a -> a.weightedMarketValue.doubleValue() / a.marketValue.doubleValue();
        }

        @Override
        public Supplier<AmountAccumulator> supplier() {
            return AmountAccumulator::new;
        }
    }

    /**
     * Creates a new {@code WeightedAveragePositionCalculator} which calculates on the given
     * attribute.
     *
     * @param calculationAttribute
     *            the attribute on which to calculate
     */
    public WeightedAveragePositionCalculator(SecurityAttribute<Double> calculationAttribute) {
        super(calculationAttribute);
    }

    @Override
    public double calc(PositionSupplier positions,
            Predicate<PositionEvaluationContext> positionFilter,
            EvaluationContext evaluationContext) {
        return positions.getPositionsWithContext(evaluationContext).filter(positionFilter)
                .collect(new WeightedAveragePositionCollector());
    }
}
