package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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

        assertEquals("deserialized value should equals() original value", position, deserialized);
        assertEquals("deserialized value should have same amount as original", position.getAmount(),
                deserialized.getAmount(), TestUtil.EPSILON);
        assertEquals("deserialized value should have same security as original",
                position.getSecurityKey(), deserialized.getSecurityKey());
    }
}
