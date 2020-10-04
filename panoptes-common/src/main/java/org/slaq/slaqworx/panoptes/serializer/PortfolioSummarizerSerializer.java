package org.slaq.slaqworx.panoptes.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.slaq.slaqworx.panoptes.asset.PortfolioProvider;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.cache.PortfolioSummarizer;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.EvaluationContextMsg;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * A utility for(de)serializing the state of a {@code PortfolioSummarizer} using Protobuf.
 *
 * @author jeremy
 */
@Singleton
public class PortfolioSummarizerSerializer implements ProtobufSerializer<PortfolioSummarizer> {
    /**
     * Converts an {@code PortfolioSummarizer} into a new {@code EvaluationContextMsg}.
     *
     * @param portfolioSummarizer
     *            the {@code PortfolioSummarizer} to be converted
     * @return a {@code EvaluationContextMsg}
     */
    public static EvaluationContextMsg convert(PortfolioSummarizer portfolioSummarizer) {
        EvaluationContextMsg.Builder evaluationContextBuilder = EvaluationContextMsg.newBuilder();
        evaluationContextBuilder.setEvaluationMode(
                portfolioSummarizer.getEvaluationContext().getEvaluationMode().name());
        portfolioSummarizer.getEvaluationContext().getSecurityOverrides()
                .forEach((securityKey, attributes) -> {
                    evaluationContextBuilder.putSecurityOverrides(securityKey.getId(),
                            SecuritySerializer.convert(attributes));
                });

        return evaluationContextBuilder.build();
    }

    private final Provider<? extends SecurityProvider> securityProvider;
    private final Provider<? extends PortfolioProvider> portfolioProvider;

    /**
     * Creates a new {@code PortfolioSummarizerSerializer} which delegates to the given
     * {@code AssetCache}.
     *
     * @param assetCacheProvider
     *            a {@code Provider} which provides an {@code AssetCache} reference (to avoid
     *            circular initialization)
     */
    public PortfolioSummarizerSerializer(Provider<AssetCache> assetCacheProvider) {
        securityProvider = assetCacheProvider;
        portfolioProvider = assetCacheProvider;
    }

    /**
     * Creates a new {@code PortfolioSummarizerSerializer} which delegates to the given providers.
     *
     * @param securityProvider
     *            the {@code SecurityProvider} to use to resolve {@code Security} data
     * @param portfolioProvider
     *            the {@code PortfolioProvider} to use to resolve {@code Portfolio} data
     */
    public PortfolioSummarizerSerializer(SecurityProvider securityProvider,
            PortfolioProvider portfolioProvider) {
        this.securityProvider = () -> securityProvider;
        this.portfolioProvider = () -> portfolioProvider;
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int getTypeId() {
        return SerializerTypeId.PORTFOLIO_SUMMARIZER.ordinal();
    }

    @Override
    public PortfolioSummarizer read(byte[] buffer) throws IOException {
        EvaluationContextMsg evaluationContextMsg = EvaluationContextMsg.parseFrom(buffer);

        EvaluationContext evaluationContext = EvaluationContextSerializer
                .convert(evaluationContextMsg, securityProvider.get(), portfolioProvider.get());
        return new PortfolioSummarizer(evaluationContext);
    }

    @Override
    public byte[] write(PortfolioSummarizer portfolioSummarizer) throws IOException {
        EvaluationContextMsg evaluationContextMsg = convert(portfolioSummarizer);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        evaluationContextMsg.writeTo(out);
        return out.toByteArray();
    }
}
