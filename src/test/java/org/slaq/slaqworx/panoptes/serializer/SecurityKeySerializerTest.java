package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * SecurityKeySerializerTest tests the functionality of the SecurityKeySerializer.
 *
 * @author jeremy
 */
public class SecurityKeySerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        SecurityKeySerializer serializer = new SecurityKeySerializer();

        SecurityKey key = new SecurityKey("foo");
        byte[] buffer = serializer.write(key);
        SecurityKey deserialized = serializer.read(buffer);

        assertEquals("deserialized value should equals() original value", key, deserialized);
    }
}
