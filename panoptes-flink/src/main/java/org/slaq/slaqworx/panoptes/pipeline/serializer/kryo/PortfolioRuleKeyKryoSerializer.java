package org.slaq.slaqworx.panoptes.pipeline.serializer.kryo;

import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.serializer.PortfolioRuleKeySerializer;

public class PortfolioRuleKeyKryoSerializer extends ProtobufKryoSerializer<PortfolioRuleKey> {
  public PortfolioRuleKeyKryoSerializer() {
    // nothing to do
  }

  @Override
  protected PortfolioRuleKeySerializer createProtobufSerializer() {
    return new PortfolioRuleKeySerializer();
  }
}
