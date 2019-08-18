package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.Panoptes;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.PortfolioCache;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PositionMsg;

/**
 * {@code PositionSerializer} (de)serializes the state of a {@code Position} using Protobuf.
 *
 * @author jeremy
 */
public class PositionSerializer implements ByteArraySerializer<Position> {
    private SecurityProvider securityProvider;

    /**
     * Creates a new {@code PositionSerializer} which uses the current {@code ApplicationContext} to
     * resolve {@code Bean} references.
     */
    public PositionSerializer() {
        // nothing to do
    }

    /**
     * Creates a new {@code PositionSerializer} which delegates to the given
     * {@code SecurityProvider}.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Securities}
     */
    public PositionSerializer(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.POSITION.ordinal();
    }

    @Override
    public Position read(byte[] buffer) throws IOException {
        if (securityProvider == null) {
            securityProvider = Panoptes.getApplicationContext().getBean(PortfolioCache.class);
        }

        PositionMsg positionMsg = PositionMsg.parseFrom(buffer);
        IdKeyMsg keyMsg = positionMsg.getKey();
        PositionKey key = new PositionKey(keyMsg.getId());
        IdKeyMsg securityKeyMsg = positionMsg.getSecurityKey();
        SecurityKey securityKey = new SecurityKey(securityKeyMsg.getId());

        return new Position(key, positionMsg.getAmount(),
                securityProvider.getSecurity(securityKey));
    }

    @Override
    public byte[] write(Position position) throws IOException {
        IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
        keyBuilder.setId(position.getKey().getId());
        IdKeyMsg key = keyBuilder.build();

        IdKeyMsg.Builder securityKeyBuilder = IdKeyMsg.newBuilder();
        securityKeyBuilder.setId(position.getSecurity().getKey().getId());
        IdKeyMsg securityKey = securityKeyBuilder.build();

        PositionMsg.Builder positionBuilder = PositionMsg.newBuilder();
        positionBuilder.setKey(key);
        positionBuilder.setAmount(position.getAmount());
        positionBuilder.setSecurityKey(securityKey);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        positionBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
