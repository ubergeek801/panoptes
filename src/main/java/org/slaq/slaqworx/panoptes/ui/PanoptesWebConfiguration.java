package org.slaq.slaqworx.panoptes.ui;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vaadin.flow.server.VaadinServlet;

/**
 * PanoptesWebConfiguration is a Spring {@code Configuration} that provides {@code Bean}s related to
 * Web applications and services.
 *
 * @author jeremy
 */
@Configuration
public class PanoptesWebConfiguration {
    /**
     * Registers a {@code VaadinServlet} mapping to {@code /frontend/*}, which is necessary when
     * overriding {@code vaadin.urlMapping}.
     *
     * @return a {@code ServletRegistrationBean} that registers the {@code VaadinServlet}
     */
    @Bean
    public ServletRegistrationBean<VaadinServlet> vaadinServlet() {
        ServletRegistrationBean<VaadinServlet> bean =
                new ServletRegistrationBean<>(new VaadinServlet() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void service(HttpServletRequest req, HttpServletResponse resp)
                            throws ServletException, IOException {
                        if (!serveStaticOrWebJarRequest(req, resp)) {
                            resp.sendError(404);
                        }
                    }
                }, "/frontend/*");
        bean.setLoadOnStartup(1);

        return bean;
    }
}
