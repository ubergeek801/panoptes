package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.serializer.RuleKeySerializer;

public class RuleKeyKryoSerializer extends ProtobufKryoSerializer<RuleKey> {
  public RuleKeyKryoSerializer() {
    // nothing to do
  }

  @Override
  protected RuleKeySerializer createProtobufSerializer() {
    return new RuleKeySerializer();
  }
}
