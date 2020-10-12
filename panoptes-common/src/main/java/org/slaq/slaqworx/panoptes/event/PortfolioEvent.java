package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.Keyed;

public abstract class PortfolioEvent implements Keyed<PortfolioKey>, ProtobufSerializable {
    protected PortfolioEvent() {
        // nothing to do
    }

    public abstract PortfolioKey getBenchmarkKey();

    /**
     * Synonymous with {@code getKey()}. Exists because Flink can't figure out {@code Keyed<T>} even
     * when given type information.
     *
     * @return the event/portfolio key
     */
    public PortfolioKey getPortfolioKey() {
        return getKey();
    }
}
