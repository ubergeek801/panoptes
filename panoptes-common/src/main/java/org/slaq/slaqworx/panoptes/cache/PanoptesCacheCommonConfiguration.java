package org.slaq.slaqworx.panoptes.cache;

import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import java.util.concurrent.ForkJoinPool;
import org.slaq.slaqworx.panoptes.asset.EligibilityList;
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
import org.slaq.slaqworx.panoptes.serializer.hazelcast.EligibilityListSerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.EvaluationContextSerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.EvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.PortfolioEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.PortfolioKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.PortfolioSerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.PortfolioSummarizerSerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.PortfolioSummarySerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.PositionKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.PositionSerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.RoomEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.RuleKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.RuleSerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.RuleSummarySerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.SecurityKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.SecuritySerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.TradeEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.TradeEvaluationResultSerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.TradeKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.hazelcast.TradeSerializer;
import org.slaq.slaqworx.panoptes.trade.RoomEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * A Micronaut {@link Factory} that provides {@link Bean}s related to the Hazelcast cache, which are
 * suitable for most configurations (e.g. standalone, cluster member, cluster client).
 *
 * @author jeremy
 */
@Factory
public class PanoptesCacheCommonConfiguration {
  /**
   * Creates a new {@link PanoptesCacheCommonConfiguration}. Restricted because instances of this
   * class should be obtained through the {@link ApplicationContext} (if it is needed at all).
   */
  protected PanoptesCacheCommonConfiguration() {
    // nothing to do
  }

  /**
   * Provides a Hazelcast {@link ExecutorConfig} suitable for executing computations on the
   * cluster.
   *
   * @return a Hazelcast {@link ExecutorConfig}
   */
  @Singleton
  protected ExecutorConfig clusterExecutorConfig() {
    return new ExecutorConfig(AssetCache.CLUSTER_EXECUTOR_NAME,
        ForkJoinPool.getCommonPoolParallelism());
  }

  /**
   * Provides a Hazelcast serialization configuration suitable for serializing Panoptes cached
   * objects.
   *
   * @return a Hazelcast {@link SerializationConfig}
   */
  @Singleton
  protected SerializationConfig serializationConfig() {
    SerializationConfig config = new SerializationConfig();
    config.addSerializerConfig(
        new SerializerConfig().setImplementation(new EligibilityListSerializer())
            .setTypeClass(EligibilityList.class));
    config.addSerializerConfig(
        new SerializerConfig().setImplementation(new EvaluationContextSerializer())
            .setTypeClass(EvaluationContext.class));
    config.addSerializerConfig(
        new SerializerConfig().setImplementation(new EvaluationResultSerializer())
            .setTypeClass(EvaluationResult.class));
    config.addSerializerConfig(
        new SerializerConfig().setImplementation(new PortfolioEvaluationRequestSerializer())
            .setTypeClass(PortfolioEvaluationRequest.class));
    config.addSerializerConfig(
        new SerializerConfig().setImplementation(new PortfolioKeySerializer())
            .setTypeClass(PortfolioKey.class));
    config.addSerializerConfig(new SerializerConfig().setImplementation(new PortfolioSerializer())
        .setTypeClass(Portfolio.class));
    config.addSerializerConfig(
        new SerializerConfig().setImplementation(new PortfolioSummarizerSerializer())
            .setTypeClass(PortfolioSummarizer.class));
    config.addSerializerConfig(
        new SerializerConfig().setImplementation(new PortfolioSummarySerializer())
            .setTypeClass(PortfolioSummary.class));
    config.addSerializerConfig(new SerializerConfig().setImplementation(new PositionKeySerializer())
        .setTypeClass(PositionKey.class));
    config.addSerializerConfig(new SerializerConfig().setImplementation(new PositionSerializer())
        .setTypeClass(Position.class));
    config.addSerializerConfig(
        new SerializerConfig().setImplementation(new RoomEvaluationRequestSerializer())
            .setTypeClass(RoomEvaluationRequest.class));
    config.addSerializerConfig(new SerializerConfig().setImplementation(new RuleKeySerializer())
        .setTypeClass(RuleKey.class));
    config.addSerializerConfig(new SerializerConfig().setImplementation(new RuleSerializer())
        .setTypeClass(ConfigurableRule.class));
    config.addSerializerConfig(new SerializerConfig().setImplementation(new RuleSummarySerializer())
        .setTypeClass(RuleSummary.class));
    config.addSerializerConfig(new SerializerConfig().setImplementation(new SecurityKeySerializer())
        .setTypeClass(SecurityKey.class));
    config.addSerializerConfig(new SerializerConfig().setImplementation(new SecuritySerializer())
        .setTypeClass(Security.class));
    config.addSerializerConfig(
        new SerializerConfig().setImplementation(new TradeEvaluationRequestSerializer())
            .setTypeClass(TradeEvaluationRequest.class));
    config.addSerializerConfig(
        new SerializerConfig().setImplementation(new TradeEvaluationResultSerializer())
            .setTypeClass(TradeEvaluationResult.class));
    config.addSerializerConfig(new SerializerConfig().setImplementation(new TradeKeySerializer())
        .setTypeClass(TradeKey.class));
    config.addSerializerConfig(
        new SerializerConfig().setImplementation(new TradeSerializer()).setTypeClass(Trade.class));

    return config;
  }
}
