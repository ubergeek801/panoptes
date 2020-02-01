package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PositionMsg;

/**
 * {@code PositionSerializer} (de)serializes the state of a {@code Position} using Protobuf.
 *
 * @author jeremy
 */
@Singleton
public class PositionSerializer implements ByteArraySerializer<Position> {
    /**
     * Converts a {@code PositionMsg} into a new {@code Position}.
     *
     * @param positionMsg
     *            the message to be converted
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Security} references
     * @return a {@code Position}
     */
    public static Position convert(PositionMsg positionMsg, SecurityProvider securityProvider) {
        IdKeyMsg keyMsg = positionMsg.getKey();
        PositionKey key = new PositionKey(keyMsg.getId());
        IdKeyMsg securityKeyMsg = positionMsg.getSecurityKey();
        SecurityKey securityKey = new SecurityKey(securityKeyMsg.getId());

        return new Position(key, positionMsg.getAmount(),
                securityProvider.getSecurity(securityKey));

    }

    private final Provider<? extends SecurityProvider> securityProvider;

    /**
     * Creates a new {@code PositionSerializer} which delegates to the given {@code AssetCache}.
     *
     * @param assetCacheProvider
     *            a {@code Provider} which provides an {@code AssetCache} reference (to avoid
     *            circular initialization)
     */
    public PositionSerializer(Provider<AssetCache> assetCacheProvider) {
        securityProvider = assetCacheProvider;
    }

    /**
     * Creates a new {@code PositionSerializer} which delegates to the given
     * {@code SecurityProvider}.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Securities}
     */
    public PositionSerializer(SecurityProvider securityProvider) {
        this.securityProvider = () -> securityProvider;
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
        PositionMsg positionMsg = PositionMsg.parseFrom(buffer);
        return convert(positionMsg, securityProvider.get());
    }

    @Override
    public byte[] write(Position position) throws IOException {
        IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
        keyBuilder.setId(position.getKey().getId());

        IdKeyMsg.Builder securityKeyBuilder = IdKeyMsg.newBuilder();
        securityKeyBuilder.setId(position.getSecurity().getKey().getId());

        PositionMsg.Builder positionBuilder = PositionMsg.newBuilder();
        positionBuilder.setKey(keyBuilder);
        positionBuilder.setAmount(position.getAmount());
        positionBuilder.setSecurityKey(securityKeyBuilder);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        positionBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
