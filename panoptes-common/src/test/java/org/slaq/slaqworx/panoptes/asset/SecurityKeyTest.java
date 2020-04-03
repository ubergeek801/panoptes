package org.slaq.slaqworx.panoptes.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

/**
 * {@code SecurityKeyTest} tests the functionality of the {@code SecurityKey}.
 *
 * @author jeremy
 */
public class SecurityKeyTest {
    /**
     * Tests that {@code SecurityKey}s are sortable.
     */
    @Test
    public void testSort() {
        SecurityKey key1 = new SecurityKey("asset1");
        SecurityKey key2 = new SecurityKey("asset2");
        SecurityKey key3 = new SecurityKey("asset3");

        ArrayList<SecurityKey> keys = new ArrayList<>();
        keys.add(key3);
        keys.add(key1);
        keys.add(key2);

        keys.sort(SecurityKey::compareTo);

        assertEquals(key1, keys.get(0), "unexpected first key");
        assertEquals(key2, keys.get(1), "unexpected second key");
        assertEquals(key3, keys.get(2), "unexpected third key");
    }
}
