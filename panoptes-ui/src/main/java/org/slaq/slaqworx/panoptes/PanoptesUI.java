package org.slaq.slaqworx.panoptes;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import jakarta.inject.Singleton;
import org.eclipse.jetty.server.Server;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panoptes is a prototype system for investment portfolio compliance assurance. This class is the
 * entry point of the Web application.
 *
 * @author jeremy
 */
@Singleton
@Context
@Requires(notEnv = Environment.TEST)
public class PanoptesUI implements ApplicationEventListener<ApplicationStartupEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(PanoptesUI.class);

  /** Creates a new instance of the Panoptes UI application. */
  protected PanoptesUI() {
    // nothing to do
  }

  /**
   * The entry point for the Panoptes application.
   *
   * @param args the program arguments
   */
  public static void main(String[] args) {
    Micronaut.run(PanoptesUI.class, args);
  }

  @Override
  public void onApplicationEvent(ApplicationStartupEvent event) {
    try {
      ApplicationContext applicationContext = event.getSource().getApplicationContext();
      AssetCache assetCache = applicationContext.getBean(AssetCache.class);

      int numSecurities = assetCache.getSecurityCache().size();
      LOG.info("{} Securities in cache", numSecurities);

      int numPositions = assetCache.getPositionCache().size();
      LOG.info("{} Positions in cache", numPositions);

      int numRules = assetCache.getRuleCache().size();
      LOG.info("{} Rules in cache", numRules);

      int numPortfolios = assetCache.getPortfolioCache().size();
      LOG.info("{} Portfolios in cache", numPortfolios);

      LOG.info("Panoptes cluster client ready");

      LOG.info("starting Web application service");

      Server vaadinServer =
          applicationContext.getBean(Server.class, Qualifiers.byName("vaadinServer"));
      vaadinServer.start();

      LOG.info("Panoptes Web application ready");
    } catch (Exception e) {
      // FIXME throw a better exception
      throw new RuntimeException("could not initialize Panoptes", e);
    }
  }
}
