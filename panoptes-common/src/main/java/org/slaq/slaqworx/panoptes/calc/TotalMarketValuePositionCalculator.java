package org.slaq.slaqworx.panoptes.calc;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;

/**
 * A {@code PositionCalculator} that determines the sum of market values of a {@code Position}
 * collection.
 *
 * @author jeremy
 */
public class TotalMarketValuePositionCalculator extends PositionCalculator<Void> {
    /**
     * Creates a new {@code TotalMarketValuePositionCalculator}.
     */
    public TotalMarketValuePositionCalculator() {
        super(null);
    }

    @Override
    public double calculate(Stream<PositionEvaluationContext> positions) {
        return positions.collect(Collectors
                .summingDouble(c -> c.getPosition().getMarketValue(c.getEvaluationContext())));
    }
}
