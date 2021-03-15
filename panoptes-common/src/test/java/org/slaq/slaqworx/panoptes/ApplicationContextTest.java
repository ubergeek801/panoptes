package org.slaq.slaqworx.panoptes;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * {@link ApplicationContextTest} tests the {@link ApplicationContext} for the Panoptes
 * application.
 *
 * @author jeremy
 */
@MicronautTest
public class ApplicationContextTest {
  @Inject
  private ApplicationContext applicationContext;

  /**
   * Tests that the {@link ApplicationContext} can be constructed.
   */
  @Test
  public void testApplicationContext() {
    assertNotNull(applicationContext, "should have obtained an ApplicationContext");
  }
}
