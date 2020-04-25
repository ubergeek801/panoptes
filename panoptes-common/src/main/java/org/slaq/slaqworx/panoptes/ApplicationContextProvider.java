package org.slaq.slaqworx.panoptes;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.ApplicationStartupEvent;

/**
 * {@code ApplicationContextProvider} provides access to the {@code ApplicationContext} of the
 * running Panoptes application. This should only be used in cases where dependency injection isn't
 * possible, which are very few.
 *
 * @author jeremy
 */
@Singleton
@Context
public class ApplicationContextProvider
        implements ApplicationEventListener<ApplicationStartupEvent> {
    @Inject private static ApplicationContext applicationContext;

    /**
     * Obtains the application context of the running Panoptes instance.
     *
     * @return the current {@code ApplicationContext}
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Manually sets the {@code ApplicationContext}. Restricted because only the application itself
     * should do this.
     *
     * @param applicationContext
     *            the singleton {@code ApplicationContext} to set
     */
    protected static void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextProvider.applicationContext = applicationContext;
    }

    /**
     * Creates a new {@code ApplicationContextProvider}. Restricted because this class is managed
     * through Micronaut.
     */
    protected ApplicationContextProvider() {
        // nothing to do
    }

    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        applicationContext = event.getSource().getApplicationContext();
    }
}
