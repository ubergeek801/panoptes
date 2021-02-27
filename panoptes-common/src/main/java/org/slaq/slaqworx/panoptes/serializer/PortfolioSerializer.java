package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioMsg;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code Portfolio}.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioSerializer implements ProtobufSerializer<Portfolio> {
  public static PortfolioMsg convert(Portfolio portfolio) {
    IdVersionKeyMsg.Builder keyBuilder = IdVersionKeyMsg.newBuilder();
    keyBuilder.setId(portfolio.getKey().getId());
    keyBuilder.setVersion(portfolio.getKey().getVersion());

    IdVersionKeyMsg benchmarkKeyMsg;
    PortfolioKey benchmarkKey = portfolio.getBenchmarkKey();
    if (benchmarkKey == null) {
      benchmarkKeyMsg = null;
    } else {
      IdVersionKeyMsg.Builder benchmarkKeyBuilder = IdVersionKeyMsg.newBuilder();
      benchmarkKeyBuilder.setId(benchmarkKey.getId());
      benchmarkKeyBuilder.setVersion(benchmarkKey.getVersion());
      benchmarkKeyMsg = benchmarkKeyBuilder.build();
    }

    PortfolioMsg.Builder portfolioBuilder = PortfolioMsg.newBuilder();
    portfolioBuilder.setKey(keyBuilder);
    portfolioBuilder.setName(portfolio.getName());
    if (benchmarkKeyMsg != null) {
      portfolioBuilder.setBenchmarkKey(benchmarkKeyMsg);
    }
    portfolio.getRules().forEach(
        r -> portfolioBuilder.addRule(RuleSerializer.convert((ConfigurableRule) r)));
    portfolio.getPositions()
        .forEach(p -> portfolioBuilder.addPosition(PositionSerializer.convert(p)));

    return portfolioBuilder.build();
  }

  public static Portfolio convert(PortfolioMsg portfolioMsg) {
    IdVersionKeyMsg keyMsg = portfolioMsg.getKey();
    PortfolioKey key = new PortfolioKey(keyMsg.getId(), keyMsg.getVersion());
    PortfolioKey benchmarkKey;
    if (portfolioMsg.hasBenchmarkKey()) {
      IdVersionKeyMsg benchmarkKeyMsg = portfolioMsg.getBenchmarkKey();
      benchmarkKey = new PortfolioKey(benchmarkKeyMsg.getId(), benchmarkKeyMsg.getVersion());
    } else {
      benchmarkKey = null;
    }

    Set<Position> positions = portfolioMsg.getPositionList().stream()
        .map(PositionSerializer::convert).collect(Collectors.toSet());
    Set<Rule> rules = portfolioMsg.getRuleList().stream().map(RuleSerializer::convert)
        .collect(Collectors.toSet());

    return new Portfolio(key, portfolioMsg.getName(), positions, benchmarkKey, rules);
  }

  /**
   * Creates a new {@code PortfolioSerializer}.
   */
  public PortfolioSerializer() {
    // nothing to do
  }

  @Override
  public Portfolio read(byte[] buffer) throws IOException {
    PortfolioMsg portfolioMsg = PortfolioMsg.parseFrom(buffer);

    return convert(portfolioMsg);
  }

  @Override
  public byte[] write(Portfolio portfolio) throws IOException {
    PortfolioMsg msg = convert(portfolio);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    msg.writeTo(out);
    return out.toByteArray();
  }
}
