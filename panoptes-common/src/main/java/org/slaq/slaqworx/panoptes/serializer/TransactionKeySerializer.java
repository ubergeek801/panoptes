package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link TransactionKey}.
 *
 * @author jeremy
 */
public class TransactionKeySerializer implements ProtobufSerializer<TransactionKey> {
  /** Creates a new {@link TransactionKeySerializer}. */
  public TransactionKeySerializer() {
    // nothing to do
  }

  @Override
  public TransactionKey read(byte[] buffer) throws IOException {
    IdKeyMsg keyMsg = IdKeyMsg.parseFrom(buffer);
    return new TransactionKey(keyMsg.getId());
  }

  @Override
  public byte[] write(TransactionKey key) throws IOException {
    IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
    keyBuilder.setId(key.getId());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    keyBuilder.build().writeTo(out);
    return out.toByteArray();
  }
}
