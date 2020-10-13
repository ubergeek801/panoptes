package org.slaq.slaqworx.panoptes;

import javax.inject.Singleton;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.runtime.Micronaut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.pipeline.PanoptesApp;
import org.slaq.slaqworx.panoptes.pipeline.PanoptesPipeline;

@Singleton
@Context
@Requires(env = "test-app")
public class PanoptesTestApp {
    private static final Logger LOG = LoggerFactory.getLogger(PanoptesTestApp.class);

    private static ApplicationContext globalAppContext;

    public static ApplicationContext getApplicationContext(String... args) {
        if (globalAppContext == null) {
            globalAppContext = createAppContext(args);
        }

        return globalAppContext;
    }

    public static void main(String[] args) throws Exception {
        try (ApplicationContext appContext = createAppContext(args)) {
            globalAppContext = appContext;
            LOG.info("configuring PanoptesPipeline");

            PanoptesPipeline pipeline = appContext.getBean(PanoptesPipeline.class);

            LOG.info("executing PanoptesPipeline");

            pipeline.execute();
        }
    }

    protected static ApplicationContext createAppContext(String... args) {
        return Micronaut.build(args).mainClass(PanoptesApp.class)
                .environments("test-app", Environment.TEST).start();
    }
}
