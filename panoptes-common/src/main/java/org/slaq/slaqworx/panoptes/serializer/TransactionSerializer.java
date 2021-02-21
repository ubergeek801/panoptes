package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PositionMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TransactionMsg;
import org.slaq.slaqworx.panoptes.trade.TaxLot;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code Transaction}.
 *
 * @author jeremy
 */
@Singleton
public class TransactionSerializer implements ProtobufSerializer<Transaction> {
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

        transaction.getPositions().forEach(t -> {
            IdKeyMsg.Builder positionKeyBuilder = IdKeyMsg.newBuilder();
            positionKeyBuilder.setId(t.getKey().getId());
            IdKeyMsg.Builder securityKeyBuilder = IdKeyMsg.newBuilder();
            securityKeyBuilder.setId(t.getSecurityKey().getId());

            PositionMsg.Builder positionBuilder = PositionMsg.newBuilder();
            positionBuilder.setKey(positionKeyBuilder);
            positionBuilder.setAmount(t.getAmount());
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
     * @return a {@code Transaction}
     */
    public static Transaction convert(TransactionMsg transactionMsg) {
        IdKeyMsg transactionKeyMsg = transactionMsg.getKey();
        TransactionKey transactionKey = new TransactionKey(transactionKeyMsg.getId());
        IdVersionKeyMsg portfolioKeyMsg = transactionMsg.getPortfolioKey();
        PortfolioKey portfolioKey =
                new PortfolioKey(portfolioKeyMsg.getId(), portfolioKeyMsg.getVersion());
        List<TaxLot> allocations = transactionMsg.getPositionList().stream()
                .map(PositionSerializer::convertTaxLot).collect(Collectors.toList());

        return new Transaction(transactionKey, portfolioKey, allocations);
    }

    /**
     * Creates a new {@code TransactionSerializer}.
     */
    public TransactionSerializer() {
        // nothing to do
    }

    @Override
    public Transaction read(byte[] buffer) throws IOException {
        TransactionMsg transactionMsg = TransactionMsg.parseFrom(buffer);

        return convert(transactionMsg);
    }

    @Override
    public byte[] write(Transaction transaction) throws IOException {
        TransactionMsg transactionMsg = convert(transaction);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transactionMsg.writeTo(out);
        return out.toByteArray();
    }
}
