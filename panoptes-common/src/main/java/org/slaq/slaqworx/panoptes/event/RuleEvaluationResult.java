package org.slaq.slaqworx.panoptes.event;

import java.util.Objects;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.rule.BenchmarkComparable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;

public class RuleEvaluationResult implements BenchmarkComparable, ProtobufSerializable {
    private final long eventId;
    private final PortfolioKey portfolioKey;
    private final PortfolioKey benchmarkKey;
    private final boolean isBenchmarkSupported;
    private final Double lowerLimit;
    private final Double upperLimit;
    private final EvaluationResult evaluationResult;

    public RuleEvaluationResult(long eventId, PortfolioKey portfolioKey, PortfolioKey benchmarkKey,
            boolean isBenchmarkSupported, Double lowerLimit, Double upperLimit,
            EvaluationResult evaluationResult) {
        this.eventId = eventId;
        this.portfolioKey = portfolioKey;
        this.benchmarkKey = benchmarkKey;
        this.isBenchmarkSupported = isBenchmarkSupported;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.evaluationResult = evaluationResult;
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
        RuleEvaluationResult other = (RuleEvaluationResult)obj;
        return Objects.equals(benchmarkKey, other.benchmarkKey)
                && Objects.equals(evaluationResult, other.evaluationResult)
                && eventId == other.eventId && isBenchmarkSupported == other.isBenchmarkSupported
                && Objects.equals(lowerLimit, other.lowerLimit)
                && Objects.equals(portfolioKey, other.portfolioKey)
                && Objects.equals(upperLimit, other.upperLimit);
    }

    public PortfolioRuleKey getBenchmarkEvaluationKey() {
        return new PortfolioRuleKey(benchmarkKey == null ? portfolioKey : benchmarkKey,
                evaluationResult.getRuleKey());
    }

    public PortfolioKey getBenchmarkKey() {
        return benchmarkKey;
    }

    public EvaluationResult getEvaluationResult() {
        return evaluationResult;
    }

    public long getEventId() {
        return eventId;
    }

    @Override
    public Double getLowerLimit() {
        return lowerLimit;
    }

    public PortfolioKey getPortfolioKey() {
        return portfolioKey;
    }

    @Override
    public Double getUpperLimit() {
        return upperLimit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(benchmarkKey, isBenchmarkSupported, lowerLimit, portfolioKey,
                upperLimit);
    }

    @Override
    public boolean isBenchmarkSupported() {
        return isBenchmarkSupported;
    }

    @Override
    public String toString() {
        return "RuleEvaluationResult [eventId=" + eventId + ", portfolioKey=" + portfolioKey
                + ", benchmarkKey=" + benchmarkKey + ", isBenchmarkSupported="
                + isBenchmarkSupported + ", lowerLimit=" + lowerLimit + ", upperLimit=" + upperLimit
                + ", evaluationResult=" + evaluationResult + "]";
    }
}
