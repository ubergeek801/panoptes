package org.slaq.slaqworx.panoptes.serializer;

import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PositionMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TransactionMsg;
import org.slaq.slaqworx.panoptes.trade.TaxLot;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link Transaction}.
 *
 * @author jeremy
 */
@Singleton
public class TransactionSerializer implements ProtobufSerializer<Transaction> {
  /**
   * Creates a new {@link TransactionSerializer}.
   */
  public TransactionSerializer() {
    // nothing to do
  }

  /**
   * Converts a {@link Transaction} into a new {@link TransactionMsg}.
   *
   * @param transaction
   *     the {@link Transaction} to be converted
   *
   * @return a {@link TransactionMsg}
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
   * Converts a {@link TransactionMsg} into a new {@link Transaction}.
   *
   * @param transactionMsg
   *     the message to be converted
   *
   * @return a {@link Transaction}
   */
  public static Transaction convert(TransactionMsg transactionMsg) {
    IdKeyMsg transactionKeyMsg = transactionMsg.getKey();
    TransactionKey transactionKey = new TransactionKey(transactionKeyMsg.getId());
    IdVersionKeyMsg portfolioKeyMsg = transactionMsg.getPortfolioKey();
    PortfolioKey portfolioKey =
        new PortfolioKey(portfolioKeyMsg.getId(), portfolioKeyMsg.getVersion());
    List<TaxLot> allocations =
        transactionMsg.getPositionList().stream().map(PositionSerializer::convertTaxLot)
            .collect(Collectors.toList());

    return new Transaction(transactionKey, portfolioKey, allocations);
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
