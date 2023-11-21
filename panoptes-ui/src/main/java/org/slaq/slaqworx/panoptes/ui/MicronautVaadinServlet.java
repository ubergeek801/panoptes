package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletService;
import io.micronaut.context.ApplicationContext;
import java.io.Serial;

/**
 * A {@link VaadinServlet} that uses a Micronaut {@link ApplicationContext} to perform dependency
 * injection on beans used by the Vaadin application.
 *
 * @author jeremy
 */
public class MicronautVaadinServlet extends VaadinServlet {
  @Serial private static final long serialVersionUID = 1L;

  private final ApplicationContext applicationContext;

  /**
   * Creates a new {@link MicronautVaadinServlet}.
   *
   * @param applicationContext the {@link ApplicationContext} to use to resolve beans
   */
  public MicronautVaadinServlet(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  protected VaadinServletService createServletService(
      DeploymentConfiguration deploymentConfiguration) throws ServiceException {
    // this is basically identical to the VaadinServlet implementation except that we create a
    // MicronautVaadinServletService instead
    MicronautVaadinServletService service =
        new MicronautVaadinServletService(this, deploymentConfiguration, applicationContext);
    service.init();

    return service;
  }
}
