package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link TradeKey}.
 *
 * @author jeremy
 */
public class TradeKeySerializer implements ProtobufSerializer<TradeKey> {
  /**
   * Creates a new {@link TradeKeySerializer}.
   */
  public TradeKeySerializer() {
    // nothing to do
  }

  @Override
  public TradeKey read(byte[] buffer) throws IOException {
    IdKeyMsg keyMsg = IdKeyMsg.parseFrom(buffer);
    return new TradeKey(keyMsg.getId());
  }

  @Override
  public byte[] write(TradeKey key) throws IOException {
    IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
    keyBuilder.setId(key.getId());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    keyBuilder.build().writeTo(out);
    return out.toByteArray();
  }
}
