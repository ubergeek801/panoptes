package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.server.VaadinService;
import io.micronaut.context.ApplicationContext;

/**
 * A Vaadin {@link Instantiator} that performs Micronaut injection.
 *
 * @author jeremy
 */
public class MicronautVaadinInstantiator extends DefaultInstantiator {
  private static final long serialVersionUID = 1L;

  private final ApplicationContext applicationContext;

  /**
   * Creates a new {@link MicronautVaadinInstantiator}.
   *
   * @param vaadinService
   *     the {@link VaadinService} to initialize the parent instantiator with
   * @param applicationContext
   *     the {@link ApplicationContext} to use to resolve beans
   */
  public MicronautVaadinInstantiator(VaadinService vaadinService,
      ApplicationContext applicationContext) {
    super(vaadinService);
    this.applicationContext = applicationContext;
  }

  @Override
  public <T> T getOrCreate(Class<T> type) {
    // if the ApplicationContext contains a bean of the requested type, use it, otherwise
    // instantiate normally
    return applicationContext.findBean(type).orElseGet(() -> super.getOrCreate(type));
  }
}
