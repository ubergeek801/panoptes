package org.slaq.slaqworx.panoptes.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests the functionality of {@link FakeSet}.
 *
 * @author jeremy
 */
public class FakeSetTest {
  /** Tests that {@link FakeSet} behaves as expected. */
  @Test
  public void testFakeSet() {
    List<String> sourceList = List.of("a", "b", "c");
    FakeSet<String> fakeSet = new FakeSet<>(sourceList);

    assertEquals(sourceList.size(), fakeSet.size(), "FakeSet should have same size as source");

    ArrayList<String> contents = new ArrayList<>();
    fakeSet.iterator().forEachRemaining(contents::add);

    assertEquals(
        sourceList.size(),
        contents.size(),
        "contents from Iterator should have same size as source");
    assertTrue(contents.contains("a"), "FakeSet should contain same elements as source");
    assertTrue(contents.contains("b"), "FakeSet should contain same elements as source");
    assertTrue(contents.contains("c"), "FakeSet should contain same elements as source");
  }
}
