package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletService;
import io.micronaut.context.ApplicationContext;

/**
 * A {@code VaadinServletService} that uses a {@code MicronautVaadinInstantiator} to perform
 * Micronaut dependency injection on objects created on behalf of the Vaadin application.
 *
 * @author jeremy
 */
public class MicronautVaadinServletService extends VaadinServletService {
  private static final long serialVersionUID = 1L;

  private final MicronautVaadinInstantiator instantiator;

  /**
   * Creates a new {@code MicronautVaadinServletService}.
   *
   * @param vaadinServlet
   *     the {@code VaadinServlet} hosting the Vaadin application
   * @param deploymentConfiguration
   *     the application's {@code DeploymentConfiguration}
   * @param applicationContext
   *     the {code AppliationContext} to use to resolve beans
   */
  public MicronautVaadinServletService(VaadinServlet vaadinServlet,
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
