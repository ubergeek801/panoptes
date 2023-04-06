package org.slaq.slaqworx.panoptes.cache;

import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.EntryProcessor;
import java.io.Serial;
import java.util.Map;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleSummary;

/**
 * A Hazelcast {@link EntryProcessor} that produces a {@link RuleSummary} projection for a given
 * {@link Rule}.
 *
 * @author jeremy
 */
public class RuleSummarizer
    implements EntryProcessor<RuleKey, ConfigurableRule, RuleSummary>, ReadOnly {
  @Serial private static final long serialVersionUID = 1L;

  @Override
  public EntryProcessor<RuleKey, ConfigurableRule, RuleSummary> getBackupProcessor() {
    // this is appropriate for a ReadOnly processor
    return null;
  }

  @Override
  public RuleSummary process(@Nonnull Map.Entry<RuleKey, ConfigurableRule> e) {
    Rule r = e.getValue();
    return (r == null ? null : RuleSummary.fromRule(r));
  }
}
