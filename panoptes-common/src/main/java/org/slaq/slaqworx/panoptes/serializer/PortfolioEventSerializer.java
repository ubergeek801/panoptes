package org.slaq.slaqworx.panoptes.serializer;

import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.event.PortfolioCommandEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.TransactionEvent;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioEventMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioEventMsg.PortfolioCommandMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioEventMsg.TransactionEventMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TransactionMsg;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of {@link PortfolioEvent}
 * subclasses.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioEventSerializer implements ProtobufSerializer<PortfolioEvent> {
  /**
   * Creates a new {@link PortfolioEventSerializer}.
   */
  public PortfolioEventSerializer() {
    // nothing to do
  }

  @Override
  public PortfolioEvent read(byte[] buffer) throws IOException {
    PortfolioEventMsg portfolioEventMsg = PortfolioEventMsg.parseFrom(buffer);
    PortfolioEvent event;
    if (portfolioEventMsg.hasTransactionEvent()) {
      TransactionEventMsg transactionEventMsg = portfolioEventMsg.getTransactionEvent();
      long eventId = transactionEventMsg.getEventId();
      TransactionMsg transactionMsg = transactionEventMsg.getTransaction();
      Transaction transaction = TransactionSerializer.convert(transactionMsg);
      event = new TransactionEvent(eventId, transaction);
    } else if (portfolioEventMsg.hasPortfolioCommand()) {
      // is a command message
      PortfolioCommandMsg portfolioCommandMsg = portfolioEventMsg.getPortfolioCommand();
      long eventId = portfolioCommandMsg.getEventId();
      IdVersionKeyMsg keyMsg = portfolioCommandMsg.getPortfolioKey();
      PortfolioKey portfolioKey = new PortfolioKey(keyMsg.getId(), keyMsg.getVersion());
      event = new PortfolioCommandEvent(eventId, portfolioKey);
    } else {
      // must be a data message
      PortfolioMsg portfolioMsg = portfolioEventMsg.getPortfolioData();
      Portfolio portfolio = PortfolioSerializer.convert(portfolioMsg);
      event = new PortfolioDataEvent(portfolio);
    }

    return event;
  }

  @Override
  public byte[] write(PortfolioEvent event) throws IOException {
    PortfolioEventMsg.Builder portfolioEventBuilder = PortfolioEventMsg.newBuilder();
    if (event instanceof TransactionEvent transactionEvent) {
      TransactionEventMsg.Builder transactionEventMsg = TransactionEventMsg.newBuilder();

      transactionEventMsg.setEventId(transactionEvent.getEventId());
      IdVersionKeyMsg.Builder keyBuilder = IdVersionKeyMsg.newBuilder();
      transactionEventMsg.setPortfolioKey(keyBuilder.build());
      keyBuilder.setId(transactionEvent.getKey().getId());
      keyBuilder.setVersion(transactionEvent.getKey().getVersion());
      transactionEventMsg
          .setTransaction(TransactionSerializer.convert(transactionEvent.getTransaction()));

      portfolioEventBuilder.setTransactionEvent(transactionEventMsg.build());
    } else if (event instanceof PortfolioCommandEvent commandEvent) {
      PortfolioCommandMsg.Builder portfolioCommandMsg = PortfolioCommandMsg.newBuilder();

      portfolioCommandMsg.setEventId(commandEvent.getEventId());

      IdVersionKeyMsg.Builder keyBuilder = IdVersionKeyMsg.newBuilder();
      keyBuilder.setId(commandEvent.getKey().getId());
      keyBuilder.setVersion(commandEvent.getKey().getVersion());
      portfolioCommandMsg.setPortfolioKey(keyBuilder.build());

      portfolioEventBuilder.setPortfolioCommand(portfolioCommandMsg.build());
    } else if (event instanceof PortfolioDataEvent dataEvent) {
      PortfolioMsg portfolioMsg = PortfolioSerializer.convert(dataEvent.getPortfolio());
      portfolioEventBuilder.setPortfolioData(portfolioMsg);
    } else {
      // this shouldn't be possible since only the above subclasses exist
      throw new IllegalArgumentException(
          "cannot serialize PortfolioEvent subclass " + event.getClass());
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    portfolioEventBuilder.build().writeTo(out);
    return out.toByteArray();
  }
}
