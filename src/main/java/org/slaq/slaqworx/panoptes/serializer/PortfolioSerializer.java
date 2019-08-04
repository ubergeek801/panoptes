package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hazelcast.nio.serialization.ByteArraySerializer;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PositionProxy;
import org.slaq.slaqworx.panoptes.asset.ProxyFactory;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioMsg;
import org.slaq.slaqworx.panoptes.rule.RuleProxy;

@Service
public class PortfolioSerializer implements ByteArraySerializer<Portfolio> {
    private final ProxyFactory proxyFactory;

    /**
     * Creates a new PortfolioSerializer which delegates to the given ProxyFactory.
     *
     * @param proxyFactory
     *            the ProxyFactory to use when creating proxy entities
     */
    public PortfolioSerializer(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
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

        Set<PositionProxy> positionKeys = portfolioMsg.getPositionKeysList().stream()
                .map(k -> proxyFactory.positionProxy(k.getId())).collect(Collectors.toSet());
        Set<RuleProxy> ruleKeys = portfolioMsg.getRuleKeysList().stream()
                .map(k -> proxyFactory.ruleProxy(k.getId())).collect(Collectors.toSet());

        return new Portfolio(key, portfolioMsg.getName(), positionKeys, benchmarkKey, ruleKeys);
    }

    @Override
    public byte[] write(Portfolio portfolio) throws IOException {
        IdVersionKeyMsg.Builder keyBuilder = IdVersionKeyMsg.newBuilder();
        keyBuilder.setId(portfolio.getKey().getId());
        keyBuilder.setVersion(portfolio.getKey().getVersion());
        IdVersionKeyMsg key = keyBuilder.build();

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
        portfolioBuilder.setKey(key);
        portfolioBuilder.setName(portfolio.getName());
        if (benchmarkKeyMsg != null) {
            portfolioBuilder.setBenchmarkKey(benchmarkKeyMsg);
        }
        portfolio.getRuleKeys().forEach(k -> {
            IdKeyMsg.Builder ruleKeyBuilder = IdKeyMsg.newBuilder();
            ruleKeyBuilder.setId(k.getId());
            portfolioBuilder.addRuleKeys(ruleKeyBuilder.build());
        });
        portfolio.getPositions().forEach(k -> {
            IdKeyMsg.Builder positionKeyBuilder = IdKeyMsg.newBuilder();
            positionKeyBuilder.setId(k.getKey().getId());
            portfolioBuilder.addRuleKeys(positionKeyBuilder.build());
        });

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        portfolioBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
