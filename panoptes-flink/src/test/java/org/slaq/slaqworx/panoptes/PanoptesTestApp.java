package org.slaq.slaqworx.panoptes;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.runtime.Micronaut;
import javax.inject.Singleton;
import org.slaq.slaqworx.panoptes.pipeline.PanoptesApp;
import org.slaq.slaqworx.panoptes.pipeline.PanoptesPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point of the test variation of the Flink edition of Panoptes. The application
 * configures a Micronaut {@code ApplicationContext} and initializes the Flink pipeline.
 *
 * @author jeremy
 */
@Singleton
@Context
@Requires(env = "test-app")
public class PanoptesTestApp {
  private static final Logger LOG = LoggerFactory.getLogger(PanoptesTestApp.class);

  private static ApplicationContext globalAppContext;

  /**
   * Obtains the {@code ApplicationContext} singleton.
   *
   * @param args
   *     the program arguments with which to initialize the {@code ApplicationContext}; ignored
   *     (and
   *     may be empty) if the context has already been created
   *
   * @return the {@code ApplicationContext}
   */
  public static ApplicationContext getApplicationContext(String... args) {
    if (globalAppContext == null) {
      globalAppContext = createApplicationContext(args);
    }

    return globalAppContext;
  }

  /**
   * Executes the Panoptes application.
   *
   * @param args
   *     the program arguments
   *
   * @throws Exception
   *     if the program could not be initialized
   */
  public static void main(String[] args) throws Exception {
    try (ApplicationContext appContext = createApplicationContext(args)) {
      globalAppContext = appContext;
      LOG.info("configuring PanoptesPipeline");

      PanoptesPipeline pipeline = appContext.getBean(PanoptesPipeline.class);

      LOG.info("executing PanoptesPipeline");

      pipeline.create();
    }
  }

  /**
   * Creates the Micronaut {@code ApplicationContext}.
   *
   * @param args
   *     the program arguments with which to initialize the {@code ApplicationContext}
   *
   * @return the {@code ApplicationContext}
   */
  protected static ApplicationContext createApplicationContext(String... args) {
    return Micronaut.build(args).mainClass(PanoptesApp.class)
        .environments("test-app", Environment.TEST).start();
  }
}
