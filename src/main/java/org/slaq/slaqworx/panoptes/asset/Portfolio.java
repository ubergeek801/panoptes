package org.slaq.slaqworx.panoptes.asset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.slaq.slaqworx.panoptes.calc.TotalAmountPositionCalculator;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A Portfolio is a set of Positions held by some entity, which may be (for example) a customer
 * account, a hypothetical model, or something more abstract such as a benchmark.
 *
 * @author jeremy
 */
public class Portfolio {
	private final String id;
	private final Portfolio benchmark;
	// keeping positions in contiguous memory improves calculation performance by 20%
	private final ArrayList<Position> positions;
	private final HashSet<Rule> rules;
	private final double totalAmount;

	public Portfolio(String id, Set<Position> positions) {
		this(id, positions, null, Collections.emptySet());
	}

	public Portfolio(String id, Set<Position> positions, Portfolio benchmark, Set<Rule> rules) {
		this.id = id;
		this.benchmark = benchmark;
		this.positions = new ArrayList<>(positions);
		this.rules = new HashSet<>(rules);

		totalAmount = new TotalAmountPositionCalculator().calculate(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Portfolio other = (Portfolio)obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	public Portfolio getBenchmark() {
		return benchmark;
	}

	public String getId() {
		return id;
	}

	public Stream<Position> getPositions() {
		return positions.stream();
	}

	public Stream<Rule> getRules() {
		return rules.stream();
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
}
