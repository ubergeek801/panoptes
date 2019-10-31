package org.slaq.slaqworx.panoptes;

import javax.inject.Singleton;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

@Singleton
@Context
public class ApplicationContextProvider {
    private static BeanContext applicationContext;

    /**
     * Obtains the {@code ApplicationContext} of the running Panoptes application. This should only
     * be used in cases where dependency injection isn't possible, e.g. from Vaadin UI components.
     *
     * @return the current ApplicationContext
     */
    public static BeanContext getApplicationContext() {
        return applicationContext;
    }

    protected static void setApplicationContext(BeanContext applicationContext) {
        ApplicationContextProvider.applicationContext = applicationContext;
    }

    protected ApplicationContextProvider() {
        // nothing to do
    }

    @EventListener
    protected void onStartup(StartupEvent event) {
        applicationContext = event.getSource();
    }
}