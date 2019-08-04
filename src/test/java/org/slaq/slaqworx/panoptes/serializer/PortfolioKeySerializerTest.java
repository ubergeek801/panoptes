package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * PortfolioKeySerializerTest tests the functionality of the PortfolioKeySerializer.
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

        assertEquals("deserialized value should equals() original value", key, deserialized);
    }
}
