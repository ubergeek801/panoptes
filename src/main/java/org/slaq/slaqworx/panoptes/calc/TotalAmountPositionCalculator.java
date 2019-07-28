package org.slaq.slaqworx.panoptes.calc;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * TotalAmountPositionCalculator is a PositionCalculator that determines the sum of amounts of a
 * Position collection.
 *
 * @author jeremy
 */
public class TotalAmountPositionCalculator extends PositionCalculator<Double> {
    /**
     * Creates a new TotalAmountPositionCalculator.
     */
    public TotalAmountPositionCalculator() {
        super(null);
    }

    @Override
    public double calc(PositionSupplier positions, Predicate<? super Position> positionFilter,
            EvaluationContext evaluationContext) {
        return positions.getPositions().filter(positionFilter)
                .collect(Collectors.summingDouble(p -> p.getAmount()));
    }
}
