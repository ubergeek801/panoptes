package org.slaq.slaqworx.panoptes.cache;

import java.util.Map;

import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.EntryProcessor;

import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleSummary;

/**
 * A Hazelcast {@code EntryProcessor} that produces a {@code RuleSummary} projection for a given
 * {@code Rule}.
 *
 * @author jeremy
 */
public class RuleSummarizer
        implements EntryProcessor<RuleKey, ConfigurableRule, RuleSummary>, ReadOnly {
    private static final long serialVersionUID = 1L;

    @Override
    public EntryProcessor<RuleKey, ConfigurableRule, RuleSummary> getBackupProcessor() {
        // this is appropriate for a ReadOnly processor
        return null;
    }

    @Override
    public RuleSummary process(Map.Entry<RuleKey, ConfigurableRule> e) {
        Rule r = e.getValue();
        return (r == null ? null : RuleSummary.fromRule(r));
    }
}
