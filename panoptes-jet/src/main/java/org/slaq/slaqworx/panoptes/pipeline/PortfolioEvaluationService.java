package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.pipeline.ServiceFactories;
import com.hazelcast.jet.pipeline.ServiceFactory;
import com.hazelcast.map.IMap;
import java.util.ArrayList;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg.EvaluationSource;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RulesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service (in the Hazelcast Jet sense) which performs rule evaluations and emits results. Receipt
 * of a {@link PortfolioEvaluationInput} implies that the target {@link Portfolio} is "ready" for
 * evaluation, that is, all of its held securities are known to have been encountered.
 */
public class PortfolioEvaluationService {
  private static final Logger LOG = LoggerFactory.getLogger(PortfolioEvaluationService.class);

  /**
   * Creates a new {@link PortfolioEvaluationService}. Restricted because instances should be
   * obtained through the {@link #serviceFactory()} method.
   */
  protected PortfolioEvaluationService() {
    // nothing to do
  }

  /**
   * Obtains a {@link ServiceFactory} which provides a shared {@link PortfolioEvaluationService}
   * instance. Note that the factory is tagged as non-cooperative because portfolio-level rule
   * evaluation may take on the order of tens to hundreds of milliseconds, which is well over the
   * threshold of cooperative task requirements.
   *
   * @return a {@link ServiceFactory}
   */
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
  public Traverser<RuleEvaluationResult> evaluate(
      PortfolioEvaluationInput portfolioEvaluationInput) {
    EvaluationSource evaluationSource = portfolioEvaluationInput.getEvaluationSource();
    Portfolio portfolio = portfolioEvaluationInput.getPortfolio();
    RulesProvider rulesProvider = () -> portfolioEvaluationInput.getRules().stream();

    // this is questionable but there shouldn't be any other portfolios queried
    PortfolioProvider portfolioProvider = (k -> portfolio);
    IMap<SecurityKey, Security> securityMap = PanoptesApp.getAssetCache().getSecurityCache();
    SecurityProvider securityProvider = (k, context) -> securityMap.get(k);

    LOG.info("processing rules for {} {} (\"{}\")", evaluationSource, portfolio.getKey(),
        portfolio.getName());
    ArrayList<RuleEvaluationResult> results = new ArrayList<>();
    long startTime = System.currentTimeMillis();
    int[] numRules = new int[1];
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
      numRules[0]++;
    });
    LOG.info("processed {} rules for {} {} (\"{}\") in {} ms", numRules[0], evaluationSource,
        portfolio.getKey(), portfolio.getName(), System.currentTimeMillis() - startTime);

    return Traversers.traverseIterable(results);
  }
}
