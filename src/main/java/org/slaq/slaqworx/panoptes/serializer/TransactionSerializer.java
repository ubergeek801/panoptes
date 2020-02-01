package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PositionMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TransactionMsg;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * {@code TransactionSerializer} (de)serializes the state of a {@code Transaction} using Protobuf.
 *
 * @author jeremy
 */
@Singleton
public class TransactionSerializer implements ByteArraySerializer<Transaction> {
    /**
     * Converts a {@code Transaction} into a new {@code TransactionMsg}.
     *
     * @param transaction
     *            the {@code Transaction} to be converted
     * @return a {@code TransactionMsg}
     */
    public static TransactionMsg convert(Transaction transaction) {
        IdKeyMsg.Builder transactionKeyBuilder = IdKeyMsg.newBuilder();
        transactionKeyBuilder.setId(transaction.getKey().getId());

        IdVersionKeyMsg.Builder portfolioKeyBuilder = IdVersionKeyMsg.newBuilder();
        portfolioKeyBuilder.setId(transaction.getPortfolioKey().getId());
        portfolioKeyBuilder.setVersion(transaction.getPortfolioKey().getVersion());

        TransactionMsg.Builder transactionBuilder = TransactionMsg.newBuilder();
        transactionBuilder.setKey(transactionKeyBuilder);
        transactionBuilder.setPortfolioKey(portfolioKeyBuilder);

        transaction.getPositions().forEach(p -> {
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

        return transactionBuilder.build();
    }

    /**
     * Converts a {@code TransactionMsg} into a new {@code Transaction}.
     *
     * @param transactionMsg
     *            the message to be converted
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Security} references
     * @return a {@code Transaction}
     */
    public static Transaction convert(TransactionMsg transactionMsg,
            SecurityProvider securityProvider) {
        IdKeyMsg transactionKeyMsg = transactionMsg.getKey();
        TransactionKey transactionKey = new TransactionKey(transactionKeyMsg.getId());
        IdVersionKeyMsg portfolioKeyMsg = transactionMsg.getPortfolioKey();
        PortfolioKey portfolioKey =
                new PortfolioKey(portfolioKeyMsg.getId(), portfolioKeyMsg.getVersion());
        List<Position> allocations = transactionMsg.getPositionList().stream()
                .map(positionMsg -> PositionSerializer.convert(positionMsg, securityProvider))
                .collect(Collectors.toList());

        return new Transaction(transactionKey, portfolioKey, allocations);
    }

    private final Provider<? extends SecurityProvider> securityProvider;

    /**
     * Creates a new {@code TransactionSerializer} which delegates to the given
     * {@code PortfolioProvider} and {@code SecurityProvider}.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Securities}
     */
    public TransactionSerializer(SecurityProvider securityProvider) {
        this.securityProvider = () -> securityProvider;
    }

    /**
     * Creates a new {@code TransactionSerializer} which delegates to the given {@code AssetCache}.
     *
     * @param assetCacheProvider
     *            a {@code Provider} which provides an {@code AssetCache} reference (to avoid
     *            circular initialization)
     */
    @Inject
    protected TransactionSerializer(Provider<AssetCache> assetCacheProvider) {
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
    public Transaction read(byte[] buffer) throws IOException {
        TransactionMsg transactionMsg = TransactionMsg.parseFrom(buffer);

        return convert(transactionMsg, securityProvider.get());
    }

    @Override
    public byte[] write(Transaction transaction) throws IOException {
        TransactionMsg transactionMsg = convert(transaction);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transactionMsg.writeTo(out);
        return out.toByteArray();
    }
}
