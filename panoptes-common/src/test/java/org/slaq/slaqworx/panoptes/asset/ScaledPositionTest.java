package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * Tests the functionality of {@link ScaledPosition}.
 *
 * @author jeremy
 */
public class ScaledPositionTest {
  /**
   * Tests that {@link Position} scaling behaves as expected.
   */
  @Test
  public void testScaling() {
    Position position = new SimplePosition(1_000_000, TestUtil.s1.getKey());
    ScaledPosition scaledPosition = new ScaledPosition(position, 0.5);

    assertEquals(position.getSecurityKey(), scaledPosition.getSecurityKey(),
        "ScaledPosition should have same SecurityKey as source");
    assertEquals(500_000, scaledPosition.getAmount(), TestUtil.EPSILON,
        "ScaledPosition amount should equal source amount * scale");
  }
}
