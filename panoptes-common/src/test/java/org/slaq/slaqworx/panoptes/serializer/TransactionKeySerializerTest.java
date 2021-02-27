package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * {@code TransactionKeySerializerTest} tests the functionality of the {@code
 * TransactionKeySerializer}.
 *
 * @author jeremy
 */
public class TransactionKeySerializerTest {
  /**
   * Tests that (de)serialization works as expected.
   *
   * @throws Exception
   *     if an unexpected error occurs
   */
  @Test
  public void testSerialization() throws Exception {
    TransactionKeySerializer serializer = new TransactionKeySerializer();

    TransactionKey key = new TransactionKey("foo");
    byte[] buffer = serializer.write(key);
    TransactionKey deserialized = serializer.read(buffer);

    assertEquals(key, deserialized, "deserialized value should equals() original value");
  }
}
