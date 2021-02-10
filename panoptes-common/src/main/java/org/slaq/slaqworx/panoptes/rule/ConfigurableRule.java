package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * A {@code Rule} that can be configured, typically via deserialization from a persistent
 * representation using JSON configuration parameters and/or Groovy filter expressions.
 *
 * @author jeremy
 */
public interface ConfigurableRule extends Rule, JsonConfigurable, ProtobufSerializable {
    /**
     * Obtains this {@code Rule}'s {@code Position} filter, if any, as a Groovy expression. The
     * filter would have been specified at create time through a JSON configuration.
     *
     * @return the {@code Position} filter as a Groovy expression, or {@code null} if no filter is
     *         specified
     */
    public String getGroovyFilter();
}
