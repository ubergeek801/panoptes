package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * {@code PortfolioKeySerializerTest} tests the functionality of the {@code PortfolioKeySerializer}.
 *
 * @author jeremy
 */
public class PortfolioKeySerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        PortfolioKeySerializer serializer = new PortfolioKeySerializer();

        PortfolioKey key = new PortfolioKey("foo", 12345);
        byte[] buffer = serializer.write(key);
        PortfolioKey deserialized = serializer.read(buffer);

        assertEquals(key, deserialized, "deserialized value should equals() original value");
    }
}
