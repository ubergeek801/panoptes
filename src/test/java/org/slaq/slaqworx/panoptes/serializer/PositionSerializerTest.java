package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;

/**
 * {@code PositionSerializerTest} tests the functionality of the {@code PositionSerializer}.
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

        Position position = new Position(new PositionKey("foo"), 123456.78, TestUtil.s1.getKey());
        byte[] buffer = serializer.write(position);
        Position deserialized = serializer.read(buffer);

        assertEquals(position, deserialized, "deserialized value should equals() original value");
        assertEquals(position.getAmount(), deserialized.getAmount(),
                "deserialized value should have same amount as original");
        assertEquals(position.getSecurityKey(), deserialized.getSecurityKey(),
                "deserialized value should have same SecurityKey as original");
    }
}
