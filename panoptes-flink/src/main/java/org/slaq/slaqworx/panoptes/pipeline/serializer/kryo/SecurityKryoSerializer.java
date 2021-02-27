package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.serializer.SecuritySerializer;

public class SecurityKryoSerializer extends ProtobufKryoSerializer<Security> {
  public SecurityKryoSerializer() {
    // nothing to do
  }

  @Override
  protected SecuritySerializer createProtobufSerializer() {
    return new SecuritySerializer();
  }
}
