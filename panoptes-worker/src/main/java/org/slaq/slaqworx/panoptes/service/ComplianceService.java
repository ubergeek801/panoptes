package org.slaq.slaqworx.panoptes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.tuple.Triple;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.ClusterPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.util.SerializerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Web service for testing Panoptes.
 *
 * @author jeremy
 */
@Controller("/compliance")
public class ComplianceService {
  static class ResultState {
    int numEvaluations;
    int resultIndex;

    ResultState() {
      resultIndex = 0;
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(ComplianceService.class);

  private final ClusterPortfolioEvaluator evaluator;
  private final AssetCache assetCache;

  /**
   * Creates a new {@code ComplianceService}.
   *
   * @param clusterPortfolioEvaluator
   *     the evaluator to use for compliance checking
   * @param assetCache
   *     the {@code AssetCache} to use to resolve cached resources
   */
  protected ComplianceService(ClusterPortfolioEvaluator clusterPortfolioEvaluator,
                              AssetCache assetCache) {
    evaluator = clusterPortfolioEvaluator;
    this.assetCache = assetCache;
  }

  /**
   * Performs a compliance evaluation over all portfolios.
   *
   * @return a {@code Flowable<String>} providing a JSON stream, where the content structure is
   *     currently in flux
   */
  @Get(uri = "/all", produces = MediaType.APPLICATION_JSON_STREAM)
  public Flowable<String> evaluateCompliance() {
    Flowable<String> response = Flowable.create(emitter -> {
      Collection<PortfolioKey> portfolioKeys = assetCache.getPortfolioCache().keySet();
      int numPortfolios = portfolioKeys.size();
      ResultState state = new ResultState();
      long startTime = System.currentTimeMillis();

      portfolioKeys.forEach(key -> {
        CompletableFuture<Map<RuleKey, EvaluationResult>> futureResult =
            evaluator.evaluate(key, new EvaluationContext());
        futureResult.whenComplete((result, exception) -> {
          synchronized (emitter) {
            if (emitter.isCancelled()) {
              // TODO maybe cancel remaining Futures too
              return;
            }
            try {
              emitter.onNext(SerializerUtil.defaultJsonMapper()
                  .writeValueAsString(Triple.of(key, result, exception)) + "\n");
            } catch (JsonProcessingException e) {
              // FIXME handle JsonProcessingException somehow
            }
            if (result != null) {
              state.numEvaluations += result.size();
            }

            if (++state.resultIndex == numPortfolios) {
              long endTime = System.currentTimeMillis();
              String message = "processed " + numPortfolios + " Portfolios using "
                  + state.numEvaluations + " Rule evaluations in "
                  + (endTime - startTime) + " ms";
              LOG.info(message);
              emitter.onNext("{ \"message\": \"" + message + "\" }");
              emitter.onComplete();
            }
          }
        });
      });
    }, BackpressureStrategy.BUFFER);

    return response;
  }
}
