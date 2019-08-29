package org.slaq.slaqworx.panoptes.calc;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;

/**
 * {@code TotalMarketValuePositionCalculator} is a {@code PositionCalculator} that determines the
 * sum of market values of a {@code Position} collection.
 *
 * @author jeremy
 */
public class TotalMarketValuePositionCalculator extends PositionCalculator<Double> {
    /**
     * Creates a new {@code TotalMarketValuePositionCalculator}.
     */
    public TotalMarketValuePositionCalculator() {
        super(null);
    }

    @Override
    public double calc(PositionSupplier positions,
            Predicate<PositionEvaluationContext> positionFilter,
            EvaluationContext evaluationContext) {
        return positions.getPositionsWithContext(evaluationContext).filter(positionFilter)
                .collect(Collectors.summingDouble(c -> c.getPosition().getMarketValue()));
    }
}
