package org.slaq.slaqworx.panoptes.pipeline;

import javax.inject.Singleton;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.runtime.Micronaut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.cache.AssetCache;

@Singleton
@Context
@Requires(notEnv = Environment.TEST)
public class PanoptesApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(PanoptesApp.class);

    private static ApplicationContext globalAppContext;

    public static ApplicationContext getApplicationContext(String... args) {
        if (globalAppContext == null) {
            globalAppContext = createAppContext(args);
        }

        return globalAppContext;
    }

    public static AssetCache getAssetCache(String... args) {
        return getApplicationContext(args).getBean(AssetCache.class);
    }

    public static void main(String[] args) throws Exception {
        try (ApplicationContext appContext = createAppContext(args)) {
            globalAppContext = appContext;
            LOGGER.info("configuring PanoptesPipeline");

            PanoptesPipeline pipeline = appContext.getBean(PanoptesPipeline.class);

            LOGGER.info("executing PanoptesPipeline");

            pipeline.execute(args);
        }
    }

    protected static ApplicationContext createAppContext(String... args) {
        return Micronaut.build(args).mainClass(PanoptesApp.class).environments(args).start();
    }
}
