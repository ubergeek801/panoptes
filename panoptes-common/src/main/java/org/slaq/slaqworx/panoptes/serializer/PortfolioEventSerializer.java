package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Singleton;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.event.PortfolioCommandEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioEventMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioEventMsg.PortfolioCommandMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioMsg;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code PortfolioEvent}.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioEventSerializer implements ProtobufSerializer<PortfolioEvent> {
    /**
     * Creates a new {@code PortfolioEventSerializer}.
     */
    public PortfolioEventSerializer() {
        // nothing to do
    }

    @Override
    public PortfolioEvent read(byte[] buffer) throws IOException {
        PortfolioEventMsg portfolioEventMsg = PortfolioEventMsg.parseFrom(buffer);
        PortfolioEvent event;
        if (portfolioEventMsg.hasCommand()) {
            // is a command message
            PortfolioCommandMsg portfolioCommandMsg = portfolioEventMsg.getCommand();
            long eventId = portfolioCommandMsg.getEventId();
            IdVersionKeyMsg keyMsg = portfolioCommandMsg.getPortfolioKey();
            PortfolioKey portfolioKey = new PortfolioKey(keyMsg.getId(), keyMsg.getVersion());
            event = new PortfolioCommandEvent(eventId, portfolioKey);
        } else {
            // must be a data message
            PortfolioMsg portfolioMsg = portfolioEventMsg.getPortfolio();
            Portfolio portfolio = PortfolioSerializer.convert(portfolioMsg);
            event = new PortfolioDataEvent(portfolio);
        }

        return event;
    }

    @Override
    public byte[] write(PortfolioEvent event) throws IOException {
        PortfolioEventMsg.Builder portfolioEventBuilder = PortfolioEventMsg.newBuilder();
        if (event instanceof PortfolioCommandEvent) {
            PortfolioCommandEvent commandEvent = (PortfolioCommandEvent)event;
            PortfolioCommandMsg.Builder portfolioCommandMsg = PortfolioCommandMsg.newBuilder();

            portfolioCommandMsg.setEventId(commandEvent.getEventId());

            IdVersionKeyMsg.Builder keyBuilder = IdVersionKeyMsg.newBuilder();
            keyBuilder.setId(commandEvent.getKey().getId());
            keyBuilder.setVersion(commandEvent.getKey().getVersion());
            portfolioCommandMsg.setPortfolioKey(keyBuilder.build());

            portfolioEventBuilder.setCommand(portfolioCommandMsg.build());
        } else if (event instanceof PortfolioDataEvent) {
            PortfolioDataEvent dataEvent = (PortfolioDataEvent)event;
            PortfolioMsg portfolioMsg = PortfolioSerializer.convert(dataEvent.getPortfolio());
            portfolioEventBuilder.setPortfolio(portfolioMsg);
        } else {
            // this shouldn't be possible since only two subclasses exist
            throw new IllegalArgumentException(
                    "cannot serialize PortfolioEvent subclass " + event.getClass());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        portfolioEventBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
