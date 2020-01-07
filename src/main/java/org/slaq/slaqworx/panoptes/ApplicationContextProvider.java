package org.slaq.slaqworx.panoptes;

import javax.inject.Singleton;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

/**
 * {@code ApplicationContextProvider} provides access to the {@code ApplicationContext} of the
 * running Panoptes application. This should only be used in cases where dependency injection isn't
 * possible (or at least for which we haven't discovered an elegant mechanism), e.g. from Vaadin UI
 * components.
 *
 * @author jeremy
 */
@Singleton
@Context
public class ApplicationContextProvider {
    private static BeanContext applicationContext;

    /**
     * Obtains the application context of the running Panoptes instance.
     *
     * @return the current {@code ApplicationContext}
     */
    public static BeanContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Creates a new {@code ApplicationContextProvider}. Restricted because this class is managed
     * through Micronaut.
     */
    protected ApplicationContextProvider() {
        // nothing to do
    }

    /**
     * Invoked upon application/context startup to capture the context instance.
     *
     * @param event
     *            the {@code StartupEvent} containing the application context
     */
    @EventListener
    protected void onStartup(StartupEvent event) {
        applicationContext = event.getSource();
    }
}
