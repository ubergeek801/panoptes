package org.slaq.slaqworx.panoptes.cache;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;

import io.micronaut.context.annotation.Factory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.rule.ConfigurableRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleSummary;
import org.slaq.slaqworx.panoptes.serializer.EvaluationContextSerializer;
import org.slaq.slaqworx.panoptes.serializer.EvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSummarySerializer;
import org.slaq.slaqworx.panoptes.serializer.PositionKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.PositionSerializer;
import org.slaq.slaqworx.panoptes.serializer.RoomEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleSerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleSummarySerializer;
import org.slaq.slaqworx.panoptes.serializer.SecurityKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.SecuritySerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeEvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeSerializer;
import org.slaq.slaqworx.panoptes.trade.RoomEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * {@code PanoptesCacheCommonConfiguration} is a Micronaut {@code Factory} that provides
 * {@code Bean}s related to the Hazelcast cache, which are suitable for most configurations (e.g.
 * standalone, cluster member, cluster client).
 *
 * @author jeremy
 */
@Factory
public class PanoptesCacheCommonConfiguration {
    /**
     * Creates a new {@code PanoptesCacheCommonConfiguration}. Restricted because instances of this
     * class should be obtained through the {@code ApplicationContext} (if it is needed at all).
     */
    protected PanoptesCacheCommonConfiguration() {
        // nothing to do
    }

    /**
     * Provides a Hazelcast serialization configuration suitable for serializing Panoptes cached
     * objects.
     *
     * @param assetCacheProvider
     *            a {@code Provider} providing an {@code AssetCache} (used to avoid circular
     *            injection dependencies)
     * @return a Hazelcast {@code SerializationConfig}
     */
    @Singleton
    protected SerializationConfig serializationConfig(Provider<AssetCache> assetCacheProvider) {
        SerializationConfig serializationConfig = new SerializationConfig();
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new EvaluationContextSerializer(assetCacheProvider))
                .setTypeClass(EvaluationContext.class));
        serializationConfig.addSerializerConfig(
                new SerializerConfig().setImplementation(new EvaluationResultSerializer())
                        .setTypeClass(EvaluationResult.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PortfolioEvaluationRequestSerializer(assetCacheProvider))
                .setTypeClass(PortfolioEvaluationRequest.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PortfolioKeySerializer()).setTypeClass(PortfolioKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PortfolioSerializer(assetCacheProvider))
                .setTypeClass(Portfolio.class));
        serializationConfig.addSerializerConfig(
                new SerializerConfig().setImplementation(new PortfolioSummarySerializer())
                        .setTypeClass(PortfolioSummary.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PositionKeySerializer()).setTypeClass(PositionKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new PositionSerializer()).setTypeClass(Position.class));
        serializationConfig.addSerializerConfig(
                new SerializerConfig().setImplementation(new RoomEvaluationRequestSerializer())
                        .setTypeClass(RoomEvaluationRequest.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new RuleKeySerializer()).setTypeClass(RuleKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new RuleSerializer()).setTypeClass(ConfigurableRule.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new RuleSummarySerializer()).setTypeClass(RuleSummary.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new SecurityKeySerializer()).setTypeClass(SecurityKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new SecuritySerializer()).setTypeClass(Security.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new TradeEvaluationRequestSerializer(assetCacheProvider))
                .setTypeClass(TradeEvaluationRequest.class));
        serializationConfig.addSerializerConfig(
                new SerializerConfig().setImplementation(new TradeEvaluationResultSerializer())
                        .setTypeClass(TradeEvaluationResult.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new TradeKeySerializer()).setTypeClass(TradeKey.class));
        serializationConfig.addSerializerConfig(new SerializerConfig()
                .setImplementation(new TradeSerializer()).setTypeClass(Trade.class));

        return serializationConfig;
    }
}
