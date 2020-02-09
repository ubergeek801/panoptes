package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.PositionProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioMsg;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProvider;

/**
 * {@code PortfolioSerializer} (de)serializes the state of a {@code Portfolio} using Protobuf.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioSerializer implements ByteArraySerializer<Portfolio> {
    private final Provider<? extends PositionProvider> positionProvider;
    private final Provider<? extends RuleProvider> ruleProvider;

    /**
     * Creates a new {@code PortfolioSerializer} which delegates to the given providers.
     *
     * @param positionProvider
     *            the {@code PositionProvider} to use to resolve {@code Position}s
     * @param ruleProvider
     *            the {@code RuleProvider} to use to resolve {@code Rule}s
     */
    public PortfolioSerializer(PositionProvider positionProvider, RuleProvider ruleProvider) {
        this.positionProvider = () -> positionProvider;
        this.ruleProvider = () -> ruleProvider;
    }

    /**
     * Creates a new {@code PortfolioSerializer} which delegates to the given {@code AssetCache}.
     *
     * @param assetCacheProvider
     *            a {@code Provider} which provides an {@code AssetCache} reference (to avoid
     *            circular initialization)
     */
    public PortfolioSerializer(Provider<AssetCache> assetCacheProvider) {
        positionProvider = assetCacheProvider;
        ruleProvider = assetCacheProvider;
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.PORTFOLIO.ordinal();
    }

    @Override
    public Portfolio read(byte[] buffer) throws IOException {
        PortfolioMsg portfolioMsg = PortfolioMsg.parseFrom(buffer);
        IdVersionKeyMsg keyMsg = portfolioMsg.getKey();
        PortfolioKey key = new PortfolioKey(keyMsg.getId(), keyMsg.getVersion());
        PortfolioKey benchmarkKey;
        if (portfolioMsg.hasBenchmarkKey()) {
            IdVersionKeyMsg benchmarkKeyMsg = portfolioMsg.getBenchmarkKey();
            benchmarkKey = new PortfolioKey(benchmarkKeyMsg.getId(), benchmarkKeyMsg.getVersion());
        } else {
            benchmarkKey = null;
        }

        Set<Position> positions = portfolioMsg.getPositionKeyList().stream()
                .map(k -> positionProvider.get().getPosition(new PositionKey(k.getId())))
                .collect(Collectors.toSet());
        Set<Rule> rules = portfolioMsg.getRuleKeyList().stream()
                .map(k -> ruleProvider.get().getRule(new RuleKey(k.getId())))
                .collect(Collectors.toSet());

        return new Portfolio(key, portfolioMsg.getName(), positions, benchmarkKey, rules);
    }

    @Override
    public byte[] write(Portfolio portfolio) throws IOException {
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
        portfolio.getRules().forEach(r -> {
            IdKeyMsg.Builder ruleKeyBuilder = IdKeyMsg.newBuilder();
            ruleKeyBuilder.setId(r.getKey().getId());
            portfolioBuilder.addRuleKey(ruleKeyBuilder);
        });
        portfolio.getPositions().forEach(k -> {
            IdKeyMsg.Builder positionKeyBuilder = IdKeyMsg.newBuilder();
            positionKeyBuilder.setId(k.getKey().getId());
            portfolioBuilder.addPositionKey(positionKeyBuilder);
        });

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        portfolioBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
