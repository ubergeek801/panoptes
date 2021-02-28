package org.slaq.slaqworx.panoptes.cache;

import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.EntryProcessor;
import java.io.Serializable;
import java.util.Map;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

/**
 * A Hazelcast {@link EntryProcessor} that produces a {@link PortfolioSummary} projection for a
 * given {@link Portfolio}.
 * <p>
 * Note that although {@link EntryProcessor} is {@link Serializable}, this class expects to be
 * serialized using Protobuf (because the contained {@link EvaluationContext} is not {@link
 * Serializable}.
 *
 * @author jeremy
 */
public class PortfolioSummarizer
    implements EntryProcessor<PortfolioKey, Portfolio, PortfolioSummary>, ReadOnly,
    ProtobufSerializable {
  private static final long serialVersionUID = 1L;

  // note that the EvaluationContext isn't Serializable
  private final transient EvaluationContext evaluationContext;

  public PortfolioSummarizer(EvaluationContext evaluationContext) {
    this.evaluationContext = evaluationContext;
  }

  @Override
  public EntryProcessor<PortfolioKey, Portfolio, PortfolioSummary> getBackupProcessor() {
    // this is appropriate for a ReadOnly processor
    return null;
  }

  /**
   * Obtains the {@link EvaluationContext} in effect for this {@link PortfolioSummarizer}.
   *
   * @return an {@link EvaluationContext}
   */
  public EvaluationContext getEvaluationContext() {
    return evaluationContext;
  }

  @Override
  public PortfolioSummary process(Map.Entry<PortfolioKey, Portfolio> e) {
    Portfolio p = e.getValue();
    return (p == null ? null : PortfolioSummary.fromPortfolio(p, evaluationContext));
  }
}
