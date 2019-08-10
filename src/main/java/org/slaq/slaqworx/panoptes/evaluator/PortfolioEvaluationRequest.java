package org.slaq.slaqworx.panoptes.evaluator;

import java.io.Serializable;
import java.util.UUID;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * {@code PortfolioEvaluationRequest} encapsulates a request to evaluate a specified
 * {@code Portfolio}.
 *
 * @author jeremy
 */
public class PortfolioEvaluationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID requestId;
    private final PortfolioKey portfolioKey;

    /**
     * Creates a new {@code RuleEvaluationMessage}.
     *
     * @param requestId
     *            an ID uniquely identifying the message
     * @param portfolioKey
     *            the key of the {@code Portfolio} to be evaluated
     */
    public PortfolioEvaluationRequest(UUID requestId, PortfolioKey portfolioKey) {
        this.requestId = requestId;
        this.portfolioKey = portfolioKey;
    }

    /**
     * Obtains the key of the {@code Portfolio} to be evaluated.
     *
     * @return a {@code PortfolioKey} identifying the evaluated {@code Portfolio}
     */
    public PortfolioKey getPortfolioKey() {
        return portfolioKey;
    }

    /**
     * Obtains the unique ID of this request.
     *
     * @return the request ID
     */
    public UUID getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return "PortfolioEvaluationRequest[requestId= " + requestId + ", portfolioKey="
                + portfolioKey + "]";
    }
}
