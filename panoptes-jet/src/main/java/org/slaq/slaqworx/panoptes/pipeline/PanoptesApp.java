package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.config.JobConfig;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point of the Hazelcast Jet edition of Panoptes. The application configures a
 * Micronaut {@link ApplicationContext} and initializes the Jet pipeline.
 *
 * @author jeremy
 */
@Singleton
@Context
@Requires(notEnv = Environment.TEST)
public class PanoptesApp {
  private static final Logger LOG = LoggerFactory.getLogger(PanoptesApp.class);

  private static ApplicationContext globalAppContext;

  /**
   * Obtains the {@link ApplicationContext} singleton.
   *
   * @param args
   *     the program arguments with which to initialize the {@link ApplicationContext}; ignored (and
   *     may be empty) if the context has already been created
   *
   * @return the {@link ApplicationContext}
   */
  public static ApplicationContext getApplicationContext(String... args) {
    if (globalAppContext == null) {
      globalAppContext = createApplicationContext(args);
    }

    return globalAppContext;
  }

  /**
   * Obtains the {@link AssetCache} from the application context.
   *
   * @param args
   *     the program arguments with which to initialize the {@link ApplicationContext}; ignored (and
   *     may be empty) if the context has already been created
   *
   * @return the {@link AssetCache} singleton
   */
  public static AssetCache getAssetCache(String... args) {
    return getApplicationContext(args).getBean(AssetCache.class);
  }

  /**
   * Executes the Panoptes application.
   *
   * @param args
   *     the program arguments
   */
  public static void main(String[] args) {
    try (ApplicationContext appContext = createApplicationContext(args)) {
      globalAppContext = appContext;
      LOG.info("configuring PanoptesPipeline");

      JetConfig jetConfig = appContext.getBean(JetConfig.class);
      JetInstance jetInstance = Jet.newJetInstance(jetConfig);

      PanoptesPipeline pipeline = appContext.getBean(PanoptesPipeline.class);
      JobConfig jobConfig = appContext.getBean(JobConfig.class);
      jetInstance.newJobIfAbsent(pipeline.getJetPipeline(), jobConfig).join();
    }
  }

  /**
   * Creates the Micronaut {@link ApplicationContext}.
   *
   * @param args
   *     the program arguments with which to initialize the {@link ApplicationContext}
   *
   * @return the {@link ApplicationContext}
   */
  protected static ApplicationContext createApplicationContext(String... args) {
    return Micronaut.build(args).mainClass(PanoptesApp.class).environments(args).start();
  }
}
