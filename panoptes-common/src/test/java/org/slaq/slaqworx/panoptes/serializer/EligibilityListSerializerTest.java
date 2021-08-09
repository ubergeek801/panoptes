package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.asset.EligibilityList;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EligibilityListMsg.ListType;

/**
 * Tests the functionality of the {@link EligibilityListSerializer}.
 *
 * @author jeremy
 */
public class EligibilityListSerializerTest {
  /**
   * Tests that (de)serialization works as expected.
   *
   * @throws Exception
   *     if an unexpected error occurs
   */
  @Test
  public void testSerialization() throws Exception {
    EligibilityListSerializer serializer = new EligibilityListSerializer();

    EligibilityList list = new EligibilityList("list", ListType.ISSUER, "test list",
        Set.of("item 1", "item 2", "item 3"));

    byte[] buffer = serializer.write(list);
    EligibilityList deserialized = serializer.read(buffer);

    assertEquals(list.name(), deserialized.name(),
        "deserialized value should have same name as original");
    assertEquals(list.type(), deserialized.type(),
        "deserialized value should have same type as original");
    assertEquals(list.description(), deserialized.description(),
        "deserialized value should have same description as original");
    assertEquals(list.items().size(), deserialized.items().size(),
        "deserialized value should have same number of items as original");
  }
}
