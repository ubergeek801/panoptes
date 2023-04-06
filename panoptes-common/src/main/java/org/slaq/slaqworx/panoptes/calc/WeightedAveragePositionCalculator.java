package org.slaq.slaqworx.panoptes.calc;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;

/**
 * A {@link PositionCalculator} that determines the weighted average of a {@link Position}
 * collection with respect to some {@link SecurityAttribute}.
 *
 * @param <T> the type on which the calculator operates
 * @author jeremy
 */
public class WeightedAveragePositionCalculator<T> extends PositionCalculator<T> {
  /**
   * Creates a new {@link WeightedAveragePositionCalculator} which calculates on the given
   * attribute.
   *
   * @param calculationAttribute the attribute on which to calculate
   */
  public WeightedAveragePositionCalculator(SecurityAttribute<T> calculationAttribute) {
    super(calculationAttribute);
  }

  @Override
  public double calculate(Stream<PositionEvaluationContext> positions) {
    return positions.collect(new WeightedAveragePositionCollector());
  }

  /** Accumulates the weighted and raw sums from the visited {@link Position}s. */
  private static class AmountAccumulator {
    double weightedAttributeValue = 0;
    double marketValue = 0;
  }

  /**
   * A {@link Collector} that operates on a {@link Stream} of {@link Position}s to calculate a
   * weighted average of the given attribute.
   *
   * @author jeremy
   */
  private class WeightedAveragePositionCollector
      implements Collector<PositionEvaluationContext, AmountAccumulator, Double> {
    /** Creates a new {@link WeightedAveragePositionCollector}. */
    public WeightedAveragePositionCollector() {
      // nothing to do
    }

    @Override
    public BiConsumer<AmountAccumulator, PositionEvaluationContext> accumulator() {
      // accumulate the Position's weighted attribute value and market value
      return (a, c) -> {
        Position p = c.getPosition();
        EvaluationContext context = c.getEvaluationContext();
        Double attributeValue =
            getValue(p.getAttributeValue(getCalculationAttribute(), context), context);
        if (attributeValue == null) {
          // FIXME this is probably not appropriate
          return;
        }
        double positionMarketValue = p.getMarketValue(c.getEvaluationContext());
        a.weightedAttributeValue += positionMarketValue * attributeValue.doubleValue();
        a.marketValue += positionMarketValue;
      };
    }

    @Override
    public Set<Characteristics> characteristics() {
      return Set.of(Characteristics.UNORDERED);
    }

    @Override
    public BinaryOperator<AmountAccumulator> combiner() {
      // combine (sum) two accumulators into one
      return (a1, a2) -> {
        a1.weightedAttributeValue += a2.weightedAttributeValue;
        a1.marketValue += a2.marketValue;
        return a1;
      };
    }

    @Override
    public Function<AmountAccumulator, Double> finisher() {
      // calculate the weighted average
      return a -> a.weightedAttributeValue / a.marketValue;
    }

    @Override
    public Supplier<AmountAccumulator> supplier() {
      return AmountAccumulator::new;
    }
  }
}
