package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Position;
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
        PositionSerializer serializer = new PositionSerializer(TestUtil.testSecurityProvider());

        Position position = new Position(new PositionKey("foo"), 123456.78, TestUtil.s1);
        byte[] buffer = serializer.write(position);
        Position deserialized = serializer.read(buffer);

        assertEquals(position, deserialized, "deserialized value should equals() original value");
        assertEquals(position.getAmount(), deserialized.getAmount(), TestUtil.EPSILON,
                "deserialized value should have same amount as original");
        System.err.println("position=" + position.getSecurity().getAttributes());
        System.err.println("deserialized=" + deserialized.getSecurity().getAttributes());
        assertEquals(position.getSecurity(), deserialized.getSecurity(),
                "deserialized value should have same security as original");
    }
}
