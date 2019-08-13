package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.MaterializedPosition;
import org.slaq.slaqworx.panoptes.asset.PositionKey;

/**
 * PositionSerializerTest tests the functionality of the PositionSerializer.
 *
 * @author jeremy
 */
public class PositionSerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        PositionSerializer serializer = new PositionSerializer();

        MaterializedPosition position =
                new MaterializedPosition(new PositionKey("foo"), 123456.78, TestUtil.s1.getKey());
        byte[] buffer = serializer.write(position);
        MaterializedPosition deserialized = serializer.read(buffer);

        assertEquals(position, deserialized, "deserialized value should equals() original value");
        assertEquals(position.getAmount(), deserialized.getAmount(), TestUtil.EPSILON,
                "deserialized value should have same amount as original");
        assertEquals(position.getSecurityKey(), deserialized.getSecurityKey(),
                "deserialized value should have same security as original");
    }
}
