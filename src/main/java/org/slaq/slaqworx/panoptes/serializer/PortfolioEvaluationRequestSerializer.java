package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationContextMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioEvaluationRequestMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TransactionMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * {@code PortfolioEvaluationRequestSerializer} (de)serializes the state of a
 * {@code PortfolioEvaluationRequest} using Protobuf.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioEvaluationRequestSerializer
        implements ByteArraySerializer<PortfolioEvaluationRequest> {
    private final Provider<? extends PortfolioProvider> portfolioProvider;
    private final Provider<? extends SecurityProvider> securityProvider;

    /**
     * Creates a new {@code PortfolioSerializer} which delegates to the given
     * {@code PositionProvider} and {@code RuleProvider}.
     *
     * @param portfolioProvider
     *            the {@code PortfolioProvider} to use to resolve {@code Portfolio}s
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Securities}
     */
    public PortfolioEvaluationRequestSerializer(PortfolioProvider portfolioProvider,
            SecurityProvider securityProvider) {
        this.portfolioProvider = () -> portfolioProvider;
        this.securityProvider = () -> securityProvider;
    }

    /**
     * Creates a new {@code PortfolioSerializer} which delegates to the given {@code AssetCache}.
     *
     * @param assetCacheProvider
     *            a {@code Provider} which provides an {@code AssetCache} reference (to avoid
     *            circular initialization)
     */
    public PortfolioEvaluationRequestSerializer(Provider<AssetCache> assetCacheProvider) {
        portfolioProvider = assetCacheProvider;
        securityProvider = assetCacheProvider;
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.PORTFOLIO_EVALUATION_REQUEST.ordinal();
    }

    @Override
    public PortfolioEvaluationRequest read(byte[] buffer) throws IOException {
        PortfolioEvaluationRequestMsg requestMsg = PortfolioEvaluationRequestMsg.parseFrom(buffer);
        IdVersionKeyMsg keyMsg = requestMsg.getPortfolioKey();
        PortfolioKey key = new PortfolioKey(keyMsg.getId(), keyMsg.getVersion());
        EvaluationContextMsg evaluationContextMsg = requestMsg.getEvaluationContext();
        EvaluationContext evaluationContext =
                EvaluationContextSerializer.convert(evaluationContextMsg);

        Portfolio portfolio = portfolioProvider.get().getPortfolio(key);
        Transaction transaction;
        if (requestMsg.hasTransaction()) {
            TransactionMsg transactionMsg = requestMsg.getTransaction();
            transaction = TransactionSerializer.convert(transactionMsg, securityProvider.get());
        } else {
            transaction = null;
        }

        return new PortfolioEvaluationRequest(portfolio, transaction, evaluationContext);
    }

    @Override
    public byte[] write(PortfolioEvaluationRequest request) throws IOException {
        IdVersionKeyMsg.Builder portfolioKeyBuilder = IdVersionKeyMsg.newBuilder();
        portfolioKeyBuilder.setId(request.getPortfolioKey().getId());
        portfolioKeyBuilder.setVersion(request.getPortfolioKey().getVersion());

        PortfolioEvaluationRequestMsg.Builder requestBuilder =
                PortfolioEvaluationRequestMsg.newBuilder();
        requestBuilder.setPortfolioKey(portfolioKeyBuilder);
        EvaluationContextMsg evaluationContext =
                EvaluationContextSerializer.convert(request.getEvaluationContext());
        requestBuilder.setEvaluationContext(evaluationContext);
        if (request.getTransaction() != null) {
            requestBuilder.setTransaction(TransactionSerializer.convert(request.getTransaction()));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        requestBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
