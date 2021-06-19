package org.slaq.slaqworx.panoptes.serializer;

import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SimplePosition;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PositionMsg;
import org.slaq.slaqworx.panoptes.trade.TaxLot;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link Position}.
 *
 * @author jeremy
 */
@Singleton
public class PositionSerializer implements ProtobufSerializer<Position> {
  /**
   * Creates a new {@link PositionSerializer}.
   */
  public PositionSerializer() {
    // nothing to do
  }

  /**
   * Converts a {@link Position} into a new {@link PositionMsg}.
   *
   * @param position
   *     the {@link Position} to be converted
   *
   * @return a {@link PositionMsg}
   */
  public static PositionMsg convert(Position position) {
    IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
    keyBuilder.setId(position.getKey().id());

    IdKeyMsg.Builder securityKeyBuilder = IdKeyMsg.newBuilder();
    securityKeyBuilder.setId(position.getSecurityKey().id());

    PositionMsg.Builder positionBuilder = PositionMsg.newBuilder();
    positionBuilder.setKey(keyBuilder);
    positionBuilder.setAmount(position.getAmount());
    positionBuilder.setSecurityKey(securityKeyBuilder);

    return positionBuilder.build();
  }

  /**
   * Converts a {@link PositionMsg} into a new {@link Position}.
   *
   * @param positionMsg
   *     the message to be converted
   *
   * @return a {@link Position}
   */
  public static Position convert(PositionMsg positionMsg) {
    IdKeyMsg keyMsg = positionMsg.getKey();
    PositionKey key = new PositionKey(keyMsg.getId());
    IdKeyMsg securityKeyMsg = positionMsg.getSecurityKey();
    SecurityKey securityKey = new SecurityKey(securityKeyMsg.getId());

    return new SimplePosition(key, positionMsg.getAmount(), securityKey);
  }

  /**
   * Converts a {@link PositionMsg} into a new {@link TaxLot}.
   *
   * @param positionMsg
   *     the message to be converted
   *
   * @return a {@link TaxLot}
   */
  public static TaxLot convertTaxLot(PositionMsg positionMsg) {
    IdKeyMsg keyMsg = positionMsg.getKey();
    PositionKey key = new PositionKey(keyMsg.getId());
    IdKeyMsg securityKeyMsg = positionMsg.getSecurityKey();
    SecurityKey securityKey = new SecurityKey(securityKeyMsg.getId());

    return new TaxLot(key, positionMsg.getAmount(), securityKey);
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
