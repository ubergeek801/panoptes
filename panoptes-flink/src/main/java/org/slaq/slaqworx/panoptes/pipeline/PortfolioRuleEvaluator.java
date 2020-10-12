package org.slaq.slaqworx.panoptes.pipeline;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A process function which collects security and portfolio position data and evaluates portfolio
 * compliance using the portfolio-supplied rules.
 *
 * @author jeremy
 */
public class PortfolioRuleEvaluator extends RuleEvaluator {
    private static final long serialVersionUID = 1L;

    public PortfolioRuleEvaluator() {
        super("portfolio");
    }

    @Override
    protected boolean checkPortfolio(Portfolio portfolio) {
        // track all portfolios we encounter
        return true;
    }

    @Override
    protected Iterable<Rule> getEffectiveRules(Portfolio portfolio) {
        // simply use the portfolio's rules
        return portfolio.getRules()::iterator;
    }
}
