package org.slaq.slaqworx.panoptes.util;

import io.micronaut.context.ApplicationContext;

/**
 * An interface for an object that is not instantiated through Micronaut but needs to obtain an
 * {@code ApplicationContext} instance. A primary example is an object that is deserialized or
 * otherwise created by Hazelcast, which is not directly aware of the {@code ApplicationContext}.
 *
 * @author jeremy
 */
public interface ApplicationContextAware {
    /**
     * Injects the {@code ApplicationContext} into the object state.
     *
     * @param applicationContext
     *            the {@code ApplicationContext} to be injected
     */
    public void setApplicationContext(ApplicationContext applicationContext);
}
