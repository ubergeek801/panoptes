package org.slaq.slaqworx.panoptes.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An interface for components that can be configured through JSON.
 *
 * @author jeremy
 */
public interface JsonConfigurable {
    static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Provides a default {@code ObjectMapper} suitable for (de)serializing JSON.
     *
     * @return an ObjectMapper
     */
    public static ObjectMapper defaultObjectMapper() {
        return objectMapper;
    }

    /**
     * Obtains the component configuration as JSON.
     *
     * @return the current configuration as JSON
     */
    public String getJsonConfiguration();
}
