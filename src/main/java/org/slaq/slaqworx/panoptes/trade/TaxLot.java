package org.slaq.slaqworx.panoptes.trade;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

public class TaxLot extends Position {
    private TradeKey tradeKey;

    public TaxLot(double amount, SecurityKey securityKey) {
        super(amount, securityKey);
    }

    public TaxLot(PositionKey positionKey, double amount, SecurityKey securityKey) {
        super(positionKey, amount, securityKey);
    }

    public TradeKey getTradeKey() {
        return tradeKey;
    }

    protected void setTradeKey(TradeKey tradeKey) {
        this.tradeKey = tradeKey;
    }
}
