package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * A projection of {@code Rule} used primarily by the compliance display.
 *
 * @author jeremy
 */
public class RuleSummary implements Keyed<RuleKey>, ProtobufSerializable {
  /**
   * Creates a new {@code RuleSummary} from the given {@code Rule}.
   *
   * @param rule
   *     the {@code Rule} to summarize
   *
   * @return a {@code RuleSummary} summarizing the given {@code Rule}
   */
  public static RuleSummary fromRule(Rule rule) {
    return new RuleSummary(rule.getKey(), rule.getDescription(),
        rule.getParameterDescription());
  }

  private final RuleKey key;
  private final String description;
  private final String parameterDescription;

  /**
   * Creates a new {@code RuleSummary} with the given parameters.
   *
   * @param key
   *     the {@code Rule} key
   * @param description
   *     the {@code Rule} description
   * @param parameterDescription
   *     a description of the {@code Rule}'s configuration parameters
   */
  public RuleSummary(RuleKey key, String description, String parameterDescription) {
    this.key = key;
    this.description = description;
    this.parameterDescription = parameterDescription;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RuleSummary other = (RuleSummary) obj;
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }
    if (key == null) {
      if (other.key != null) {
        return false;
      }
    } else if (!key.equals(other.key)) {
      return false;
    }
    if (parameterDescription == null) {
      return other.parameterDescription == null;
    } else return parameterDescription.equals(other.parameterDescription);
  }

  /**
   * Obtains a description of the summarized {@code Rule}.
   *
   * @return a description
   */
  public String getDescription() {
    return description;
  }

  @Override
  public RuleKey getKey() {
    return key;
  }

  /**
   * Obtains a description of the summarized {@code Rule}'s configuration parameters.
   *
   * @return a description of the configuration
   */
  public String getParameterDescription() {
    return parameterDescription;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result
        + ((parameterDescription == null) ? 0 : parameterDescription.hashCode());

    return result;
  }
}
