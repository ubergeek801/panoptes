package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.serializer.RuleSerializer;

public class RuleKryoSerializer extends ProtobufKryoSerializer<ConfigurableRule> {
  public RuleKryoSerializer() {
    // nothing to do
  }

  @Override
  protected RuleSerializer createProtobufSerializer() {
    return new RuleSerializer();
  }
}
