package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.server.Constants;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

@Factory
public class PanoptesUIConfiguration {
    /**
     * Creates a new {@code PanoptesUIConfiguration}. Restricted because instances of this class
     * should be obtained through the {@code ApplicationContext} (if it is needed at all).
     */
    protected PanoptesUIConfiguration() {
        // nothing to do
    }

    @Bean
    protected Server servletServer() {
        Server server = new Server(8090);

        WebAppContext context = new WebAppContext();
        context.setInitParameter(Constants.SERVLET_PARAMETER_PRODUCTION_MODE, "false");
        context.setContextPath("/panoptes");
        context.setBaseResource(
                Resource.newResource(getClass().getClassLoader().getResource("ui")));
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*");
        context.setConfigurationDiscovered(true);
        context.setConfigurations(new Configuration[] { new AnnotationConfiguration(),
                new WebInfConfiguration(), new WebXmlConfiguration(), new MetaInfConfiguration() });
        context.getServletContext().setExtendedListenerTypes(true);
        // example code includes this but it appears to duplicate other configuration
        // context.addEventListener(new ServletContextListeners());
        server.setHandler(context);

        return server;
    }
}
