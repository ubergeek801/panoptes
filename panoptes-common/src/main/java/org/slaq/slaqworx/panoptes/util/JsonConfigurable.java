package org.slaq.slaqworx.panoptes.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An interface for components that can be configured through JSON.
 *
 * @author jeremy
 */
public interface JsonConfigurable {
  ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Provides a default {@code ObjectMapper} suitable for (de)serializing JSON.
   *
   * @return an ObjectMapper
   */
  static ObjectMapper defaultObjectMapper() {
    return objectMapper;
  }

  /**
   * Obtains the component configuration as JSON.
   *
   * @return the current configuration as JSON
   */
  String getJsonConfiguration();
}
