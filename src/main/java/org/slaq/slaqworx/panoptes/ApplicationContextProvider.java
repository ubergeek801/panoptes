package org.slaq.slaqworx.panoptes;

import javax.inject.Singleton;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.ApplicationStartupEvent;

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
public class ApplicationContextProvider
        implements ApplicationEventListener<ApplicationStartupEvent> {
    private static ApplicationContext applicationContext;

    /**
     * Obtains the application context of the running Panoptes instance.
     *
     * @return the current {@code ApplicationContext}
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
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
