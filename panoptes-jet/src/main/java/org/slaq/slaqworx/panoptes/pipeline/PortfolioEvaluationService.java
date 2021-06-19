package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.pipeline.ServiceFactories;
import com.hazelcast.jet.pipeline.ServiceFactory;
import com.hazelcast.map.IMap;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.PortfolioEvaluationInput;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RulesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service (in the Hazelcast Jet sense) which performs rule evaluations and emits results. Receipt
 * of a {@link PortfolioEvaluationInput} implies that the target {@link Portfolio} is "ready" for
 * evaluation, that is, all of its held securities are known to have been encountered.
 * <p>
 * Rule evaluation is provided in this manner because, while generally non-blocking, the process may
 * take on the order of tens to hundreds of milliseconds, for which Jet recommends designating the
 * process as non-cooperative.
 */
public class PortfolioEvaluationService {
  private static final Logger LOG = LoggerFactory.getLogger(PortfolioEvaluationService.class);

  private final AssetCache assetCache;

  /**
   * Creates a new {@link PortfolioEvaluationService}. Restricted because instances should be
   * obtained through the {@link #serviceFactory()} method.
   */
  protected PortfolioEvaluationService() {
    assetCache = PanoptesApp.getAssetCache();
  }

  /**
   * Obtains a {@link ServiceFactory} which provides a shared {@link PortfolioEvaluationService}
   * instance.
   *
   * @param assetCache
   *     the {@link AssetCache} to be used to resolve data references
   *
   * @return a {@link ServiceFactory}
   */
  @Nonnull
  public static ServiceFactory<?, PortfolioEvaluationService> serviceFactory() {
    return ServiceFactories.sharedService(context -> new PortfolioEvaluationService())
        .toNonCooperative();
  }

  /**
   * Performs a portfolio evaluation and publishes the result.
   *
   * @param portfolioEvaluationInput
   *     a {@link PortfolioEvaluationInput} containing the input parameters of the evaluation
   *
   * @return a {@link Traverser} over the results of portfolio rule evaluation
   */
  @Nonnull
  public Traverser<RuleEvaluationResult> evaluate(
      @Nonnull PortfolioEvaluationInput portfolioEvaluationInput) {
    EvaluationSource evaluationSource = portfolioEvaluationInput.getEvaluationSource();
    RulesProvider rulesProvider = () -> portfolioEvaluationInput.getRules().stream();
    int numRules = portfolioEvaluationInput.getRules().size();

    IMap<SecurityKey, Security> securityMap = assetCache.getSecurityCache();
    SecurityProvider securityProvider = (k, context) -> securityMap.get(k);

    Portfolio portfolio = PanoptesApp.getAssetCache().getPortfolioCache()
        .get(portfolioEvaluationInput.getPortfolioKey());
    // this is questionable but there shouldn't be any other Portfolios requested by rule evaluation
    PortfolioProvider portfolioProvider = (k -> portfolio);

    LOG.info("processing {} rules for {} {} (\"{}\")", numRules, evaluationSource,
        portfolio.getKey(), portfolio.getName());
    ArrayList<RuleEvaluationResult> results = new ArrayList<>(numRules);
    long startTime = System.currentTimeMillis();
    rulesProvider.getRules().forEach(rule -> {
      // FIXME get/generate eventId
      long eventId = System.currentTimeMillis();

      EvaluationResult evaluationResult =
          new org.slaq.slaqworx.panoptes.evaluator.RuleEvaluator(rule, portfolio,
              new EvaluationContext(securityProvider, portfolioProvider)).call();
      // enrich the result with some other essential information
      RuleEvaluationResult ruleEvaluationResult =
          new RuleEvaluationResult(eventId, portfolio.getKey(), portfolio.getBenchmarkKey(),
              evaluationSource, rule.isBenchmarkSupported(), rule.getLowerLimit(),
              rule.getUpperLimit(), evaluationResult);
      results.add(ruleEvaluationResult);
    });
    LOG.info("processed {} rules for {} {} (\"{}\") in {} ms", numRules, evaluationSource,
        portfolio.getKey(), portfolio.getName(), System.currentTimeMillis() - startTime);

    return Traversers.traverseIterable(results);
  }
}
