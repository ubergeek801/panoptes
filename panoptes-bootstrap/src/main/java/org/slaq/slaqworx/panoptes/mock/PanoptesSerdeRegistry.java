package org.slaq.slaqworx.panoptes.mock;

import io.micronaut.configuration.kafka.serde.SerdeRegistry;
import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.PortfolioSummarizer;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleSummary;
import org.slaq.slaqworx.panoptes.serializer.EvaluationContextSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioEventSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSummarizerSerializer;
import org.slaq.slaqworx.panoptes.serializer.PortfolioSummarySerializer;
import org.slaq.slaqworx.panoptes.serializer.PositionKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.PositionSerializer;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializer;
import org.slaq.slaqworx.panoptes.serializer.RoomEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleSerializer;
import org.slaq.slaqworx.panoptes.serializer.RuleSummarySerializer;
import org.slaq.slaqworx.panoptes.serializer.SecurityKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.SecuritySerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeEvaluationRequestSerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.TradeSerializer;
import org.slaq.slaqworx.panoptes.serializer.TransactionKeySerializer;
import org.slaq.slaqworx.panoptes.serializer.TransactionSerializer;
import org.slaq.slaqworx.panoptes.trade.RoomEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.Trade;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.TradeKey;
import org.slaq.slaqworx.panoptes.trade.Transaction;
import org.slaq.slaqworx.panoptes.trade.TransactionKey;

/**
 * A {@link SerdeRegistry} that provides {@link Serde}s for Panoptes-specific classes, falling back
 * to the default provided by {@link Serdes} for non-Panoptes types.
 *
 * @author jeremy
 */
@Singleton
@Replaces(SerdeRegistry.class)
public class PanoptesSerdeRegistry implements SerdeRegistry {
  private final Map<Class<?>, Serde<?>> serdeMap;

  /**
   * Creates a new {@link PanoptesSerdeRegistry}. Restricted because this class is managed through
   * Micronaut.
   */
  protected PanoptesSerdeRegistry() {
    serdeMap = new HashMap<>();

    serdeMap.put(EvaluationContext.class, createSerde(new EvaluationContextSerializer()));
    serdeMap.put(Portfolio.class, createSerde(new PortfolioSerializer()));
    serdeMap.put(PortfolioEvent.class, createSerde(new PortfolioEventSerializer()));
    serdeMap.put(PortfolioKey.class, createSerde(new PortfolioKeySerializer()));
    serdeMap.put(PortfolioSummarizer.class, createSerde(new PortfolioSummarizerSerializer()));
    serdeMap.put(PortfolioSummary.class, createSerde(new PortfolioSummarySerializer()));
    serdeMap.put(PositionKey.class, createSerde(new PositionKeySerializer()));
    serdeMap.put(Position.class, createSerde(new PositionSerializer()));
    serdeMap.put(RoomEvaluationRequest.class, createSerde(new RoomEvaluationRequestSerializer()));
    serdeMap.put(RuleKey.class, createSerde(new RuleKeySerializer()));
    serdeMap.put(Rule.class, createSerde(new RuleSerializer()));
    serdeMap.put(RuleSummary.class, createSerde(new RuleSummarySerializer()));
    serdeMap.put(SecurityKey.class, createSerde(new SecurityKeySerializer()));
    serdeMap.put(Security.class, createSerde(new SecuritySerializer()));
    serdeMap.put(TradeEvaluationRequest.class, createSerde(new TradeEvaluationRequestSerializer()));
    serdeMap.put(TradeKey.class, createSerde(new TradeKeySerializer()));
    serdeMap.put(Trade.class, createSerde(new TradeSerializer()));
    serdeMap.put(TransactionKey.class, createSerde(new TransactionKeySerializer()));
    serdeMap.put(Transaction.class, createSerde(new TransactionSerializer()));
  }

  @Override
  public <T> Serde<T> getSerde(Class<T> type) {
    try {
      return (Serde<T>) serdeMap.computeIfAbsent(type, Serdes::serdeFrom);
    } catch (IllegalArgumentException e) {
      // unfortunately the message doesn't tell us the offending type, so produce a new
      // exception/message
      throw new IllegalArgumentException("cannot resolve Serde for type " + type, e);
    }
  }

  /**
   * Creates a {@link Serde} that delegates to the given {@link ProtobufSerializer}.
   *
   * @param <T>
   *     the type to be (de)serialized
   * @param protobufSerializer
   *     a {@link ProtobufSerializer} that can (de)serialize the given type
   *
   * @return a {@link Serde} that can (de)serialize the given type
   */
  protected <T extends ProtobufSerializable> Serde<T> createSerde(
      @Nonnull ProtobufSerializer<T> protobufSerializer) {
    return new Serde<>() {
      @Override
      public Deserializer<T> deserializer() {
        return (topic, data) -> {
          try {
            return protobufSerializer.read(data);
          } catch (IOException e) {
            // FIXME throw a better exception
            throw new RuntimeException("could not deserialize data for topic " + topic, e);
          }
        };
      }

      @Override
      public Serializer<T> serializer() {
        return (topic, data) -> {
          try {
            return protobufSerializer.write(data);
          } catch (IOException e) {
            // FIXME throw a better exception
            throw new RuntimeException("could not serialize data for topic " + topic, e);
          }
        };
      }
    };
  }
}
