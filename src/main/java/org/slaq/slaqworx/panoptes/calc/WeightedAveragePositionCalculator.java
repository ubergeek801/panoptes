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
 * WeightedAveragePositionCalculator is a PositionCalculator that determines the weighted average of
 * a Position collection.
 *
 * @author jeremy
 */
public class WeightedAveragePositionCalculator extends PositionCalculator<Double> {
    private static class AmountAccumulator {
        DoubleAdder weightedAmount = new DoubleAdder();
        DoubleAdder amount = new DoubleAdder();
    }

    /**
     * WeightedAveragePositionCollector is a Collector that operates on a Stream<Position> to
     * calculate a weighted average of the given attribute.
     *
     * @author jeremy
     */
    private class WeightedAveragePositionCollector
            implements Collector<PositionEvaluationContext, AmountAccumulator, Double> {
        /**
         * Creates a new WeightedAveragePositionCollector.
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
                        p.getSecurity(c.getEvaluationContext().getSecurityProvider())
                                .getAttributeValue(getCalculationAttribute());
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
            // combine (sum) two accumulators into one
            return (a1, a2) -> {
                a1.weightedAmount.add(a2.weightedAmount.doubleValue());
                a1.amount.add(a2.amount.doubleValue());
                return a1;
            };
        }

        @Override
        public Function<AmountAccumulator, Double> finisher() {
            // calculate the weighted average
            return a -> a.weightedAmount.doubleValue() / a.amount.doubleValue();
        }

        @Override
        public Supplier<AmountAccumulator> supplier() {
            return AmountAccumulator::new;
        }
    }

    /**
     * Creates a new WeightedAveragePositionCalculator which calculates on the given attribute.
     *
     * @param securityProvider
     *            the SecurityProvider from which to obtain Security information
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
