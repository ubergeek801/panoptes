package org.slaq.slaqworx.panoptes.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * {@code EvaluationGroupTest} tests the functionality of the {@code EvaluationGroup}.
 *
 * @author jeremy
 */
public class EvaluationGroupTest {
    /**
     * Tests that {@code toString()} behaves as expected.
     */
    @Test
    public void testToString() {
        EvaluationGroup group = EvaluationGroup.defaultGroup();
        assertEquals(EvaluationGroup.DEFAULT_EVALUATION_GROUP_ID, group.toString(),
                "default EvaluationGroup should have default group ID");

        group = new EvaluationGroup("group", "key");
        // we are not too particular about the toString() content as long as it is different than
        // the default
        assertNotEquals(EvaluationGroup.DEFAULT_EVALUATION_GROUP_ID, group.toString(),
                "non-default EvaluationGroup should not have default group ID");
    }
}
