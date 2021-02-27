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
import javax.inject.Singleton;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point of the Hazelcast Jet edition of Panoptes. The application configures a
 * Micronaut {@code ApplicationContext} and initializes the Jet pipeline.
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
   * Obtains the {@code AssetCache} from the application context.
   *
   * @param args
   *     the program arguments with which to initialize the {@code ApplicationContext}; ignored
   *     (and
   *     may be empty) if the context has already been created
   *
   * @return the {@code AssetCache} singleton
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

      PanoptesPipeline pipeline = appContext.getBean(PanoptesPipeline.class);
      JetConfig jetConfig = appContext.getBean(JetConfig.class);
      JetInstance jetInstance = Jet.newJetInstance(jetConfig);

      // FIXME elect a member to start the job
      String hostname = System.getenv("NODENAME");
      if (hostname == null || hostname.contains("uberkube02")) {
        LOG.info("elected to start Jet");
        JobConfig jobConfig = appContext.getBean(JobConfig.class);
        LOG.info("executing PanoptesPipeline");
        jetInstance.newJob(pipeline.getJetPipeline(), jobConfig).join();
      } else {
        LOG.info("host {} not elected to start Jet", hostname);
      }
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
    return Micronaut.build(args).mainClass(PanoptesApp.class).environments(args).start();
  }
}
