package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Singleton;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PositionMsg;
import org.slaq.slaqworx.panoptes.trade.TaxLot;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code Position}.
 *
 * @author jeremy
 */
@Singleton
public class PositionSerializer implements ProtobufSerializer<Position> {
    /**
     * Converts a {@code Position} into a new {@code PositionMsg}.
     *
     * @param position
     *            the {@code Position} to be converted
     * @return a {@code PositionMsg}
     */
    public static PositionMsg convert(Position position) {
        IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
        keyBuilder.setId(position.getKey().getId());

        IdKeyMsg.Builder securityKeyBuilder = IdKeyMsg.newBuilder();
        securityKeyBuilder.setId(position.getSecurityKey().getId());

        PositionMsg.Builder positionBuilder = PositionMsg.newBuilder();
        positionBuilder.setKey(keyBuilder);
        positionBuilder.setAmount(position.getAmount());
        positionBuilder.setSecurityKey(securityKeyBuilder);

        return positionBuilder.build();
    }

    /**
     * Converts a {@code PositionMsg} into a new {@code Position}.
     *
     * @param positionMsg
     *            the message to be converted
     * @return a {@code Position}
     */
    public static Position convert(PositionMsg positionMsg) {
        IdKeyMsg keyMsg = positionMsg.getKey();
        PositionKey key = new PositionKey(keyMsg.getId());
        IdKeyMsg securityKeyMsg = positionMsg.getSecurityKey();
        SecurityKey securityKey = new SecurityKey(securityKeyMsg.getId());

        return new SimplePosition(key, positionMsg.getAmount(), securityKey);
    }

    /**
     * Converts a {@code PositionMsg} into a new {@code TaxLot}.
     *
     * @param positionMsg
     *            the message to be converted
     * @return a {@code TaxLot}
     */
    public static TaxLot convertTaxLot(PositionMsg positionMsg) {
        IdKeyMsg keyMsg = positionMsg.getKey();
        PositionKey key = new PositionKey(keyMsg.getId());
        IdKeyMsg securityKeyMsg = positionMsg.getSecurityKey();
        SecurityKey securityKey = new SecurityKey(securityKeyMsg.getId());

        return new TaxLot(key, positionMsg.getAmount(), securityKey);
    }

    /**
     * Creates a new {@code PositionSerializer}.
     */
    public PositionSerializer() {
        // nothing to do
    }

    @Override
    public Position read(byte[] buffer) throws IOException {
        PositionMsg positionMsg = PositionMsg.parseFrom(buffer);
        return convert(positionMsg);
    }

    @Override
    public byte[] write(Position position) throws IOException {
        PositionMsg positionMsg = convert(position);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        positionMsg.writeTo(out);
        return out.toByteArray();
    }
}
