package org.slaq.slaqworx.panoptes;

import java.util.HashSet;
import java.util.Map;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * TestUtil provides common utilities to support Panoptes testing.
 *
 * @author jeremy
 */
public class TestUtil {
	public static final double EPSILON = 0.00001;

	public static final SecurityAttribute<Double> moovyRating =
			SecurityAttribute.of("Moovy", Double.class);
	public static final SecurityAttribute<Double> npRating =
			SecurityAttribute.of("N&P", Double.class);
	public static final SecurityAttribute<Double> fetchRating =
			SecurityAttribute.of("Fetch", Double.class);

	public static final Map<SecurityAttribute<?>, ? super Object> s1Attributes = Map.of(moovyRating,
			90d, npRating, 92d, fetchRating, 88d, SecurityAttribute.duration, 4d);
	public static final Security s1 = new Security("s1", s1Attributes);

	public static final Map<SecurityAttribute<?>, ? super Object> s2Attributes =
			Map.of(moovyRating, 85d, npRating, 78d, SecurityAttribute.duration, 4d);
	public static final Security s2 = new Security("s2", s2Attributes);

	public static final HashSet<Position> p1Positions;
	public static final Portfolio p1;

	public static final HashSet<Position> p2Positions;
	public static final Portfolio p2;

	static {
		p1Positions = new HashSet<>();
		p1Positions.add(new Position(1000, TestUtil.s1));
		p1Positions.add(new Position(500, TestUtil.s2));
		p1 = new Portfolio("p1", p1Positions);

		p2Positions = new HashSet<>();
		p2Positions.add(new Position(500, TestUtil.s1));
		p2Positions.add(new Position(1000, TestUtil.s2));
		p2 = new Portfolio("p2", p2Positions);
	}

	private TestUtil() {
		// nothing to do
	}
}
