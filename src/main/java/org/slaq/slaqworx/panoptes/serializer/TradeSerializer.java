package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PositionMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TradeMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TransactionMsg;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * {@code TradeSerializer} (de)serializes the state of a {@code Trade} using Protobuf.
 *
 * @author jeremy
 */
@Singleton
public class TradeSerializer implements ByteArraySerializer<Trade> {
    private final Provider<? extends PortfolioProvider> portfolioProvider;
    private final Provider<? extends SecurityProvider> securityProvider;

    /**
     * Creates a new {@code TradeSerializer} which delegates to the given {@code PortfolioProvider}
     * and {@code SecurityProvider}.
     *
     * @param portfolioProvider
     *            the {@code PortfolioProvider} to use to resolve {@code Portfolio}s
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Securities}
     */
    public TradeSerializer(PortfolioProvider portfolioProvider, SecurityProvider securityProvider) {
        this.portfolioProvider = () -> portfolioProvider;
        this.securityProvider = () -> securityProvider;
    }

    /**
     * Creates a new {@code TradeSerializer} which delegates to the given {@code AssetCache}.
     *
     * @param assetCacheProvider
     *            a {@code Provider} which provides an {@code AssetCache} reference (to avoid
     *            circular initialization)
     */
    @Inject
    protected TradeSerializer(Provider<AssetCache> assetCacheProvider) {
        portfolioProvider = assetCacheProvider;
        securityProvider = assetCacheProvider;
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
        Map<PortfolioKey, Transaction> transactions =
                tradeMsg.getTransactionList().stream().map(transactionMsg -> {
                    IdKeyMsg transactionKeyMsg = transactionMsg.getKey();
                    TransactionKey transactionKey = new TransactionKey(transactionKeyMsg.getId());
                    IdVersionKeyMsg portfolioKeyMsg = transactionMsg.getPortfolioKey();
                    PortfolioKey portfolioKey =
                            new PortfolioKey(portfolioKeyMsg.getId(), portfolioKeyMsg.getVersion());
                    Portfolio portfolio = portfolioProvider.get().getPortfolio(portfolioKey);
                    List<Position> allocations =
                            transactionMsg.getPositionList().stream().map(positionMsg -> {
                                // FIXME share code with PositionSerializer
                                IdKeyMsg positionKeyMsg = positionMsg.getKey();
                                PositionKey positionKey = new PositionKey(positionKeyMsg.getId());
                                double amount = positionMsg.getAmount();
                                IdKeyMsg securityKeyMsg = positionMsg.getSecurityKey();
                                SecurityKey securityKey = new SecurityKey(securityKeyMsg.getId());
                                Security security = securityProvider.get().getSecurity(securityKey);
                                return new Position(positionKey, amount, security);
                            }).collect(Collectors.toList());

                    return new Transaction(transactionKey, portfolio, allocations);
                }).collect(Collectors.toMap(t -> t.getPortfolio().getKey(), t -> t));

        return new Trade(tradeKey, transactions);
    }

    @Override
    public byte[] write(Trade trade) throws IOException {
        IdKeyMsg.Builder tradeKeyBuilder = IdKeyMsg.newBuilder();
        tradeKeyBuilder.setId(trade.getKey().getId());

        TradeMsg.Builder tradeBuilder = TradeMsg.newBuilder();
        tradeBuilder.setKey(tradeKeyBuilder);
        trade.getTransactions().values().forEach(t -> {
            IdKeyMsg.Builder transactionKeyBuilder = IdKeyMsg.newBuilder();
            transactionKeyBuilder.setId(t.getKey().getId());
            IdVersionKeyMsg.Builder portfolioKeyBuilder = IdVersionKeyMsg.newBuilder();
            portfolioKeyBuilder.setId(t.getPortfolio().getKey().getId());
            portfolioKeyBuilder.setVersion(t.getPortfolio().getKey().getVersion());
            TransactionMsg.Builder transactionBuilder = TransactionMsg.newBuilder();
            transactionBuilder.setKey(transactionKeyBuilder);
            transactionBuilder.setPortfolioKey(portfolioKeyBuilder);

            t.getPositions().forEach(p -> {
                IdKeyMsg.Builder positionKeyBuilder = IdKeyMsg.newBuilder();
                positionKeyBuilder.setId(p.getKey().getId());
                IdKeyMsg.Builder securityKeyBuilder = IdKeyMsg.newBuilder();
                securityKeyBuilder.setId(p.getSecurity().getKey().getId());

                PositionMsg.Builder positionBuilder = PositionMsg.newBuilder();
                positionBuilder.setKey(positionKeyBuilder);
                positionBuilder.setAmount(p.getAmount());
                positionBuilder.setSecurityKey(securityKeyBuilder);

                transactionBuilder.addPosition(positionBuilder);
            });

            tradeBuilder.addTransaction(transactionBuilder);
        });

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        tradeBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
