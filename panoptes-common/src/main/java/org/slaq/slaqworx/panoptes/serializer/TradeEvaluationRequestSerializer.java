package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Provider;

import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TradeEvaluationRequestMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * {@code TradeEvaluationRequestSerializer} (de)serializes the state of a
 * {@code TradeEvaluationRequest} using Protobuf.
 *
 * @author jeremy
 */
public class TradeEvaluationRequestSerializer
        implements ProtobufSerializer<TradeEvaluationRequest> {
    private final Provider<? extends SecurityProvider> securityProvider;
    private final Provider<? extends PortfolioProvider> portfolioProvider;

    /**
     * Creates a new {@code TradeEvaluationRequestSerializer} which delegates to the given
     * {@code AssetCache}.
     *
     * @param assetCacheProvider
     *            a {@code Provider} which provides an {@code AssetCache} reference (to avoid
     *            circular initialization)
     */
    public TradeEvaluationRequestSerializer(Provider<AssetCache> assetCacheProvider) {
        portfolioProvider = assetCacheProvider;
        securityProvider = assetCacheProvider;
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.TRADE_EVALUATION_REQUEST.ordinal();
    }

    @Override
    public TradeEvaluationRequest read(byte[] buffer) throws IOException {
        TradeEvaluationRequestMsg requestMsg = TradeEvaluationRequestMsg.parseFrom(buffer);

        IdKeyMsg keyMsg = requestMsg.getTradeKey();
        TradeKey tradeKey = new TradeKey(keyMsg.getId());

        EvaluationContext context = EvaluationContextSerializer.convert(
                requestMsg.getEvaluationContext(), securityProvider.get(), portfolioProvider.get());

        return new TradeEvaluationRequest(tradeKey, context);
    }

    @Override
    public byte[] write(TradeEvaluationRequest request) throws IOException {
        IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
        keyBuilder.setId(request.getTradeKey().getId());

        TradeEvaluationRequestMsg.Builder requestBuilder = TradeEvaluationRequestMsg.newBuilder();
        requestBuilder.setTradeKey(keyBuilder.build());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        requestBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
