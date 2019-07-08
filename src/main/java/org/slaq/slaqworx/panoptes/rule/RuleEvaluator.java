package org.slaq.slaqworx.panoptes.rule;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * RuleEvaluator is responsible for the process of evaluating a set of Rules against some Portfolio.
 *
 * @author jeremy
 */
public class RuleEvaluator {
	public RuleEvaluator() {
		// nothing to do
	}

	public Map<Rule, Boolean> evaluate(Portfolio portfolio) {
		return evaluate(portfolio.getRules(), portfolio, portfolio.getBenchmark());
	}

	public Map<Rule, Boolean> evaluate(Portfolio portfolio, Portfolio benchmark) {
		return evaluate(portfolio.getRules(), portfolio, benchmark);
	}

	public Map<Rule, Boolean> evaluate(Stream<Rule> rules, Portfolio portfolio) {
		return evaluate(rules, portfolio, portfolio.getBenchmark());
	}

	public Map<Rule, Boolean> evaluate(Stream<Rule> rules, Portfolio portfolio,
			Portfolio benchmark) {
		return rules.parallel()
				.collect(Collectors.toMap(r -> r, r -> r.evaluate(portfolio, benchmark)));
	}
}
