package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletService;
import io.micronaut.context.ApplicationContext;

/**
 * A {@link VaadinServletService} that uses a {@link MicronautVaadinInstantiator} to perform
 * Micronaut dependency injection on objects created on behalf of the Vaadin application.
 *
 * @author jeremy
 */
public class MicronautVaadinServletService extends VaadinServletService {
  private static final long serialVersionUID = 1L;

  private final MicronautVaadinInstantiator instantiator;

  /**
   * Creates a new {@link MicronautVaadinServletService}.
   *
   * @param vaadinServlet the {@link VaadinServlet} hosting the Vaadin application
   * @param deploymentConfiguration the application's {@link DeploymentConfiguration}
   * @param applicationContext the {@link ApplicationContext} to use to resolve beans
   */
  public MicronautVaadinServletService(
      VaadinServlet vaadinServlet,
      DeploymentConfiguration deploymentConfiguration,
      ApplicationContext applicationContext) {
    super(vaadinServlet, deploymentConfiguration);
    instantiator = new MicronautVaadinInstantiator(this, applicationContext);
  }

  @Override
  public Instantiator getInstantiator() {
    return instantiator;
  }
}
