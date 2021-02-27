package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.serializer.SecurityKeySerializer;

public class SecurityKeyKryoSerializer extends ProtobufKryoSerializer<SecurityKey> {
  public SecurityKeyKryoSerializer() {
    // nothing to do
  }

  @Override
  protected SecurityKeySerializer createProtobufSerializer() {
    return new SecurityKeySerializer();
  }
}
