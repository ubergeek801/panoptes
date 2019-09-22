package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.Security;

/**
 * An {@code EligibilityRule} stipulates which {@code Securities} can be held by a
 * {@code Portfolio}, using some algorithm which is independent of the {@code Portfolio}'s holdings
 * (and which typically considers only the {@code Security} itself).
 *
 * @author jeremy
 */
public abstract class EligibilityRule extends GenericRule {
    /**
     * Creates a new {@code EligibilityRule} with the given parameters.
     *
     * @param key
     *            the unique key of this rule, or {@code null} to generate one
     * @param description
     *            the rule description
     */
    protected EligibilityRule(RuleKey key, String description) {
        super(key, description);
    }

    /**
     * Indicates whether the given {@code Security} is eligible for investment according to this
     * rule.
     *
     * @param security
     *            the {@code Security} to test for eligibility
     * @return {@code true} if the {@code Security} is eligible for investment, {@code false}
     *         otherwise
     */
    public abstract boolean isEligible(Security security);

    @Override
    protected EvaluationResult eval(PositionSupplier portfolioPositions,
            PositionSupplier benchmarkPositions, EvaluationContext evaluationContext) {
        boolean isEligible =
                portfolioPositions.getPositions().allMatch(p -> isEligible(p.getSecurity()));

        return new EvaluationResult(isEligible);
    }
}
