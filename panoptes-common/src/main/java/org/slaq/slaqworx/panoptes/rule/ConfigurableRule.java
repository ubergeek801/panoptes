package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * A {@link Rule} that can be configured, typically via deserialization from a persistent
 * representation using JSON configuration parameters and/or Groovy filter expressions.
 *
 * @author jeremy
 */
public interface ConfigurableRule extends Rule, JsonConfigurable, ProtobufSerializable {
  /**
   * Obtains this {@link Rule}'s {@link Position} filter, if any, as a Groovy expression. The filter
   * would have been specified at create time through a JSON configuration.
   *
   * @return the {@link Position} filter as a Groovy expression, or {@code null} if no filter is
   *     specified
   */
  String getGroovyFilter();
}
