package org.slaq.slaqworx.panoptes.asset;

import org.slaq.slaqworx.panoptes.trade.TradeKey;

public class TaxLot extends Position {
    private final TradeKey tradeKey;

    public TaxLot(double amount, SecurityKey securityKey, TradeKey tradeKey) {
        super(amount, securityKey);
        this.tradeKey = tradeKey;
    }

    public TradeKey getTradeKey() {
        return tradeKey;
    }
}
