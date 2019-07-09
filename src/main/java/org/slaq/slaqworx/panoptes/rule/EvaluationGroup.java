package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.Position;

@FunctionalInterface
public interface EvaluationGroup {
    public String group(Position position);
}
