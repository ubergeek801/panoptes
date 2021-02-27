package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code SecurityKey}.
 *
 * @author jeremy
 */
public class SecurityKeySerializer implements ProtobufSerializer<SecurityKey> {
  /**
   * Creates a new {@code SecurityKeySerializer}.
   */
  public SecurityKeySerializer() {
    // nothing to do
  }

  @Override
  public SecurityKey read(byte[] buffer) throws IOException {
    IdKeyMsg keyMsg = IdKeyMsg.parseFrom(buffer);
    return new SecurityKey(keyMsg.getId());
  }

  @Override
  public byte[] write(SecurityKey key) throws IOException {
    IdKeyMsg.Builder keyBuilder = IdKeyMsg.newBuilder();
    keyBuilder.setId(key.getId());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    keyBuilder.build().writeTo(out);
    return out.toByteArray();
  }
}
