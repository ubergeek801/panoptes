package org.slaq.slaqworx.panoptes.service;

import java.util.ArrayList;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.data.PortfolioCache;
import org.slaq.slaqworx.panoptes.evaluator.ClusterPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

@Controller("/")
public class ComplianceController {
    private final PortfolioCache portfolioCache;
    private final ClusterPortfolioEvaluator portfolioEvaluator;

    protected ComplianceController(PortfolioCache portfolioCache,
            ClusterPortfolioEvaluator portfolioEvaluator) {
        this.portfolioCache = portfolioCache;
        this.portfolioEvaluator = portfolioEvaluator;
    }

    @Get("/compliance")
    String runCompliance() {
        long startTime = System.currentTimeMillis();

        ArrayList<Thread> threads = new ArrayList<>();
        for (Portfolio portfolio : portfolioCache.getPortfolioCache().values()) {
            Thread evaluationThread = new Thread(() -> {
                try {
                    portfolioEvaluator.evaluate(portfolio,
                            new EvaluationContext(portfolioCache, portfolioCache, portfolioCache));
                } catch (InterruptedException e) {
                    return;
                }
            });
            evaluationThread.start();
            threads.add(evaluationThread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                break;
            }
        }

        return "compliance run completed in " + (System.currentTimeMillis() - startTime) + " ms";
    }
}
