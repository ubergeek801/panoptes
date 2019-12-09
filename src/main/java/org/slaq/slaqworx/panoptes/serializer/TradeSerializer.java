package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TradeMsg;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * {@code TradeSerializer} (de)serializes the state of a {@code Trade} using Protobuf.
 *
 * @author jeremy
 */
@Singleton
public class TradeSerializer implements ByteArraySerializer<Trade> {
    private final Provider<? extends SecurityProvider> securityProvider;

    /**
     * Creates a new {@code TradeSerializer} which delegates to the given {@code AssetCache}.
     *
     * @param assetCacheProvider
     *            a {@code Provider} which provides an {@code AssetCache} reference (to avoid
     *            circular initialization)
     */
    public TradeSerializer(Provider<AssetCache> assetCacheProvider) {
        securityProvider = assetCacheProvider;
    }

    /**
     * Creates a new {@code TradeSerializer} which delegates to the given {@code PortfolioProvider}
     * and {@code SecurityProvider}.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Securities}
     */
    public TradeSerializer(SecurityProvider securityProvider) {
        this.securityProvider = () -> securityProvider;
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.TRADE.ordinal();
    }

    @Override
    public Trade read(byte[] buffer) throws IOException {
        TradeMsg tradeMsg = TradeMsg.parseFrom(buffer);
        IdKeyMsg tradeKeyMsg = tradeMsg.getKey();
        TradeKey tradeKey = new TradeKey(tradeKeyMsg.getId());
        Map<PortfolioKey, Transaction> transactions = tradeMsg.getTransactionList().stream()
                .map(transactionMsg -> TransactionSerializer.convert(transactionMsg,
                        securityProvider.get()))
                .collect(Collectors.toMap(t -> t.getPortfolioKey(), t -> t));

        return new Trade(tradeKey, transactions);
    }

    @Override
    public byte[] write(Trade trade) throws IOException {
        IdKeyMsg.Builder tradeKeyBuilder = IdKeyMsg.newBuilder();
        tradeKeyBuilder.setId(trade.getKey().getId());

        TradeMsg.Builder tradeBuilder = TradeMsg.newBuilder();
        tradeBuilder.setKey(tradeKeyBuilder);
        trade.getTransactions().values().forEach(t -> {
            tradeBuilder.addTransaction(TransactionSerializer.convert(t));
        });

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        tradeBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
