package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.server.InitParameters;
import com.vaadin.flow.server.VaadinServlet;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import javax.inject.Named;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

/**
 * A Micronaut {@link Factory} that provides {@link Bean}s related to the experimental Vaadin user
 * interface.
 *
 * @author jeremy
 */
@Factory
public class PanoptesUIConfiguration {
  /**
   * Creates a new {@link PanoptesUIConfiguration}. Restricted because instances of this class
   * should be obtained through the {@link ApplicationContext} (if it is needed at all).
   */
  protected PanoptesUIConfiguration() {
    // nothing to do
  }

  /**
   * Provides a Jetty {@link Server} to host the Vaadin interface, running alongside the Micronaut
   * server.
   *
   * @param vaadinServlet
   *     the {@link VaadinServlet} to host in the server
   *
   * @return a {@link Server}
   */
  @Bean
  @Named("vaadinServer")
  protected Server servletServer(VaadinServlet vaadinServlet) {
    Server server = new Server(9090);

    WebAppContext context = new WebAppContext();
    context.setInitParameter(InitParameters.SERVLET_PARAMETER_PRODUCTION_MODE, "false");
    context.setContextPath("/");
    context.setBaseResource(Resource.newResource(getClass().getClassLoader().getResource("ui")));
    context.setAttribute(WebInfConfiguration.CONTAINER_JAR_PATTERN, ".*");
    context.setConfigurationDiscovered(true);
    context.setConfigurations(
        new Configuration[] {new AnnotationConfiguration(), new WebInfConfiguration(),
            new WebXmlConfiguration(), new MetaInfConfiguration()});
    context.getServletContext().setExtendedListenerTypes(true);
    context.addServlet(new ServletHolder(vaadinServlet), "/*");

    server.setHandler(context);

    return server;
  }

  /**
   * Obtains a {@link VaadinServlet} instance.
   *
   * @param applicationContext
   *     the {@link ApplicationContext} to provide to the created servlet
   *
   * @return a {@link VaadinServlet}
   */
  @Bean
  protected VaadinServlet vaadinServlet(ApplicationContext applicationContext) {
    MicronautVaadinServlet servlet = new MicronautVaadinServlet(applicationContext);

    return servlet;
  }
}
