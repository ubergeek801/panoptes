package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * {@code TradeKeySerializerTest} tests the functionality of the {@code TradeKeySerializer}.
 *
 * @author jeremy
 */
public class TradeKeySerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     * 
     * @throws Exception
     *             if an unexpected error occurs
     */
    @Test
    public void testSerialization() throws Exception {
        TradeKeySerializer serializer = new TradeKeySerializer();

        TradeKey key = new TradeKey("foo");
        byte[] buffer = serializer.write(key);
        TradeKey deserialized = serializer.read(buffer);

        assertEquals(key, deserialized, "deserialized value should equals() original value");
    }
}
