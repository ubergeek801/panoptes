package org.slaq.slaqworx.panoptes.cache;

import java.util.Map;

import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.EntryProcessor;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;

/**
 * {@code PortfolioSummarizer} is a Hazelcast {@code EntryProcessor} that produces a
 * {@code PortfolioSummary} projection for a given {@code Portfolio}.
 *
 * @author jeremy
 */
public class PortfolioSummarizer
        implements EntryProcessor<PortfolioKey, Portfolio, PortfolioSummary>, ReadOnly {
    private static final long serialVersionUID = 1L;

    @Override
    public EntryProcessor<PortfolioKey, Portfolio, PortfolioSummary> getBackupProcessor() {
        // this is appropriate for a ReadOnly processor
        return null;
    }

    @Override
    public PortfolioSummary process(Map.Entry<PortfolioKey, Portfolio> e) {
        Portfolio p = e.getValue();
        return (p == null ? null : PortfolioSummary.fromPortfolio(p));
    }
}
