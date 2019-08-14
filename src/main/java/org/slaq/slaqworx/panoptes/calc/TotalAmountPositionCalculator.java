package org.slaq.slaqworx.panoptes.calc;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;

/**
 * {@code TotalAmountPositionCalculator} is a {@code PositionCalculator} that determines the sum of
 * amounts of a {@code Position} collection.
 *
 * @author jeremy
 */
public class TotalAmountPositionCalculator extends PositionCalculator<Double> {
    /**
     * Creates a new {@code TotalAmountPositionCalculator}.
     */
    public TotalAmountPositionCalculator() {
        super(null);
    }

    @Override
    public double calc(PositionSupplier positions,
            Predicate<PositionEvaluationContext> positionFilter,
            EvaluationContext evaluationContext) {
        return positions.getPositionsWithContext(evaluationContext).filter(positionFilter)
                .collect(Collectors.summingDouble(c -> c.getPosition().getAmount()));
    }
}
