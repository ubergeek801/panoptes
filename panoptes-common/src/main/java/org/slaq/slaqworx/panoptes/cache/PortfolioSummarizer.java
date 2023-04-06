package org.slaq.slaqworx.panoptes.cache;

import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.EntryProcessor;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A Hazelcast {@link EntryProcessor} that produces a {@link PortfolioSummary} projection for a
 * given {@link Portfolio}.
 *
 * <p>Note that although {@link EntryProcessor} is {@link Serializable}, this class expects to be
 * serialized using Protobuf (because the contained {@link EvaluationContext} is not {@link
 * Serializable}.
 *
 * @param evaluationContext the {@link EvaluationContext} in effect for this {@link
 *     PortfolioSummarizer}
 * @author jeremy
 */
public record PortfolioSummarizer(@Nonnull EvaluationContext evaluationContext)
    implements EntryProcessor<PortfolioKey, Portfolio, PortfolioSummary>,
        ReadOnly,
        ProtobufSerializable {
  @Serial private static final long serialVersionUID = 1L;

  @Override
  public EntryProcessor<PortfolioKey, Portfolio, PortfolioSummary> getBackupProcessor() {
    // this is appropriate for a ReadOnly processor
    return null;
  }

  @Override
  public PortfolioSummary process(@Nonnull Map.Entry<PortfolioKey, Portfolio> e) {
    Portfolio p = e.getValue();
    return (p == null ? null : PortfolioSummary.fromPortfolio(p, evaluationContext));
  }
}
