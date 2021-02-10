package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.DateMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.TradeMsg;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code Trade}.
 *
 * @author jeremy
 */
@Singleton
public class TradeSerializer implements ProtobufSerializer<Trade> {
    /**
     * Creates a new {@code TradeSerializer}.
     */
    public TradeSerializer() {
        // nothing to do
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
        DateMsg tradeDateMsg = tradeMsg.getTradeDate();
        LocalDate tradeDate = LocalDate.of(tradeDateMsg.getYear(), tradeDateMsg.getMonth(),
                tradeDateMsg.getDay());
        DateMsg settlementDateMsg = tradeMsg.getSettlementDate();
        LocalDate settlementDate = LocalDate.of(settlementDateMsg.getYear(),
                settlementDateMsg.getMonth(), settlementDateMsg.getDay());
        Map<PortfolioKey, Transaction> transactions =
                tradeMsg.getTransactionList().stream().map(TransactionSerializer::convert)
                        .collect(Collectors.toMap(Transaction::getPortfolioKey, t -> t));
        return new Trade(tradeKey, tradeDate, settlementDate, transactions);
    }

    @Override
    public byte[] write(Trade trade) throws IOException {
        IdKeyMsg.Builder tradeKeyBuilder = IdKeyMsg.newBuilder();
        tradeKeyBuilder.setId(trade.getKey().getId());

        LocalDate tradeDate = trade.getTradeDate();
        DateMsg.Builder tradeDateBuilder = DateMsg.newBuilder();
        tradeDateBuilder.setYear(tradeDate.getYear());
        tradeDateBuilder.setMonth(tradeDate.getMonthValue());
        tradeDateBuilder.setDay(tradeDate.getDayOfMonth());

        LocalDate settlementDate = trade.getSettlementDate();
        DateMsg.Builder settlementDateBuilder = DateMsg.newBuilder();
        settlementDateBuilder.setYear(settlementDate.getYear());
        settlementDateBuilder.setMonth(settlementDate.getMonthValue());
        settlementDateBuilder.setDay(settlementDate.getDayOfMonth());

        TradeMsg.Builder tradeBuilder = TradeMsg.newBuilder();
        tradeBuilder.setKey(tradeKeyBuilder);
        tradeBuilder.setTradeDate(tradeDateBuilder);
        tradeBuilder.setSettlementDate(settlementDateBuilder);
        trade.getTransactions().values().forEach(t -> {
            tradeBuilder.addTransaction(TransactionSerializer.convert(t));
        });

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        tradeBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
