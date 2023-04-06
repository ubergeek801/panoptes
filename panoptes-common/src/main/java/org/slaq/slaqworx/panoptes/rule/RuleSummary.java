package org.slaq.slaqworx.panoptes.rule;

import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A projection of {@link Rule} used primarily by the compliance display.
 *
 * @param key the {@link Rule} key
 * @param description the {@link Rule} description
 * @param parameterDescription a description of the {@link Rule}'s configuration parameters
 * @author jeremy
 */
public record RuleSummary(RuleKey key, String description, String parameterDescription)
    implements Keyed<RuleKey>, ProtobufSerializable {
  /**
   * Creates a new {@link RuleSummary} from the given {@link Rule}.
   *
   * @param rule the {@link Rule} to summarize
   * @return a {@link RuleSummary} summarizing the given {@link Rule}
   */
  @Nonnull
  public static RuleSummary fromRule(@Nonnull Rule rule) {
    return new RuleSummary(rule.getKey(), rule.getDescription(), rule.getParameterDescription());
  }

  @Override
  @Nonnull
  public RuleKey getKey() {
    return key;
  }
}
