package org.slaq.slaqworx.panoptes.rule;

import java.util.UUID;

import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * A Rule is a testable assertion against a set of Positions (typically supplied by a Portfolio). A
 * collection of Rules is typically used to assure compliance with the investment guidelines of a
 * customer account.
 *
 * @author jeremy
 */
public abstract class Rule {
	private final String id;
	private final String description;
	private final Double lowerLimit;
	private final Double upperLimit;

	public Rule(String description) {
		this(null, description, null, null);
	}

	public Rule(String description, Double lowerLimit, Double upperLimit) {
		this(null, description, lowerLimit, upperLimit);
	}

	public Rule(String id, String description, Double lowerLimit, Double upperLimit) {
		this.id = (id == null ? UUID.randomUUID().toString() : id);
		this.description = description;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
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
		Rule other = (Rule)obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	public boolean evaluate(Portfolio portfolio, Portfolio benchmark) {
		try {
			double value = eval(portfolio, benchmark);

			if (lowerLimit != null && (value != Double.NaN && value < lowerLimit)) {
				return false;
			}

			if (upperLimit != null && (value == Double.NaN || value > upperLimit)) {
				return false;
			}

			return true;
		} catch (Exception e) {
			// for now, any unexpected exception results in failure
			return false;
		}
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	protected abstract double eval(Portfolio portfolio, Portfolio benchmark);
}
