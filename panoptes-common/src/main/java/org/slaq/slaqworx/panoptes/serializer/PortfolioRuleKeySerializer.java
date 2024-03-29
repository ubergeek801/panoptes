package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioRuleKey;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioRuleKeyMsg;
import org.slaq.slaqworx.panoptes.rule.RuleKey;

/**
 * A {@link ProtobufSerializer} which (de)serializes the state of a {@link PortfolioRuleKey}.
 *
 * @author jeremy
 */
public class PortfolioRuleKeySerializer implements ProtobufSerializer<PortfolioRuleKey> {
  /** Creates a new {@link PortfolioEventSerializer}. */
  public PortfolioRuleKeySerializer() {
    // nothing to do
  }

  @Override
  public PortfolioRuleKey read(byte[] buffer) throws IOException {
    IdVersionKeyMsg portfolioKeyMsg = IdVersionKeyMsg.parseFrom(buffer);
    IdKeyMsg ruleKeyMsg = IdKeyMsg.parseFrom(buffer);
    return new PortfolioRuleKey(
        new PortfolioKey(portfolioKeyMsg.getId(), portfolioKeyMsg.getVersion()),
        new RuleKey(ruleKeyMsg.getId()));
  }

  @Override
  public byte[] write(PortfolioRuleKey key) throws IOException {
    IdVersionKeyMsg.Builder portfolioKeyBuilder = IdVersionKeyMsg.newBuilder();
    portfolioKeyBuilder.setId(key.portfolioKey().getId());
    portfolioKeyBuilder.setVersion(key.portfolioKey().getVersion());

    IdKeyMsg.Builder ruleKeyBuilder = IdKeyMsg.newBuilder();
    ruleKeyBuilder.setId(key.ruleKey().id());

    PortfolioRuleKeyMsg.Builder keyBuilder = PortfolioRuleKeyMsg.newBuilder();
    keyBuilder.setPortfolioKey(portfolioKeyBuilder);
    keyBuilder.setRuleKey(ruleKeyBuilder);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    keyBuilder.build().writeTo(out);
    return out.toByteArray();
  }
}
