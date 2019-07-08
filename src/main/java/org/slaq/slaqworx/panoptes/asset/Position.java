package org.slaq.slaqworx.panoptes.asset;

import java.util.UUID;

/**
 * A Position is a holding of some amount of a particular Security by some Portfolio.
 *
 * @author jeremy
 */
public class Position {
	private final String id;
	private final double amount;
	private final Security security;

	public Position(double amount, Security security) {
		this(UUID.randomUUID().toString(), amount, security);
	}

	public Position(String id, double amount, Security security) {
		this.id = id;
		this.amount = amount;
		this.security = security;
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
		Position other = (Position)obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	public double getAmount() {
		return amount;
	}

	public String getId() {
		return id;
	}

	public Security getSecurity() {
		return security;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
}
