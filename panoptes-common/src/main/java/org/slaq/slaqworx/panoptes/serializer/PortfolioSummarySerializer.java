package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Singleton;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.IdVersionKeyMsg;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.PortfolioSummaryMsg;

/**
 * A {@code ProtobufSerializer} which (de)serializes the state of a {@code PortfolioSummary}.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioSummarySerializer implements ProtobufSerializer<PortfolioSummary> {
    /**
     * Creates a new {@code PortfolioSummarySerializer}.
     */
    public PortfolioSummarySerializer() {
        // nothing to do
    }

    @Override
    public PortfolioSummary read(byte[] buffer) throws IOException {
        PortfolioSummaryMsg portfolioSummaryMsg = PortfolioSummaryMsg.parseFrom(buffer);
        IdVersionKeyMsg keyMsg = portfolioSummaryMsg.getKey();
        PortfolioKey key = new PortfolioKey(keyMsg.getId(), keyMsg.getVersion());
        PortfolioKey benchmarkKey;
        if (portfolioSummaryMsg.hasBenchmarkKey()) {
            IdVersionKeyMsg benchmarkKeyMsg = portfolioSummaryMsg.getBenchmarkKey();
            benchmarkKey = new PortfolioKey(benchmarkKeyMsg.getId(), benchmarkKeyMsg.getVersion());
        } else {
            benchmarkKey = null;
        }
        double totalMarketValue = portfolioSummaryMsg.getTotalMarketValue();
        boolean isAbstract = portfolioSummaryMsg.getIsAbstract();

        return new PortfolioSummary(key, portfolioSummaryMsg.getName(), benchmarkKey,
                totalMarketValue, isAbstract);
    }

    @Override
    public byte[] write(PortfolioSummary portfolioSummary) throws IOException {
        IdVersionKeyMsg.Builder keyBuilder = IdVersionKeyMsg.newBuilder();
        keyBuilder.setId(portfolioSummary.getKey().getId());
        keyBuilder.setVersion(portfolioSummary.getKey().getVersion());

        IdVersionKeyMsg benchmarkKeyMsg;
        PortfolioKey benchmarkKey = portfolioSummary.getBenchmarkKey();
        if (benchmarkKey == null) {
            benchmarkKeyMsg = null;
        } else {
            IdVersionKeyMsg.Builder benchmarkKeyBuilder = IdVersionKeyMsg.newBuilder();
            benchmarkKeyBuilder.setId(benchmarkKey.getId());
            benchmarkKeyBuilder.setVersion(benchmarkKey.getVersion());
            benchmarkKeyMsg = benchmarkKeyBuilder.build();
        }

        PortfolioSummaryMsg.Builder portfolioSummaryBuilder = PortfolioSummaryMsg.newBuilder();
        portfolioSummaryBuilder.setKey(keyBuilder);
        portfolioSummaryBuilder.setName(portfolioSummary.getName());
        if (benchmarkKeyMsg != null) {
            portfolioSummaryBuilder.setBenchmarkKey(benchmarkKeyMsg);
        }
        portfolioSummaryBuilder.setTotalMarketValue(portfolioSummary.getTotalMarketValue());
        portfolioSummaryBuilder.setIsAbstract(portfolioSummary.isAbstract());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        portfolioSummaryBuilder.build().writeTo(out);
        return out.toByteArray();
    }
}
