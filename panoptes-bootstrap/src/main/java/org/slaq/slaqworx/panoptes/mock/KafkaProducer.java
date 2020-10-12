package org.slaq.slaqworx.panoptes.mock;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.evaluator.EvaluationResult;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluationRequest;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationRequest;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluationResult;
import org.slaq.slaqworx.panoptes.trade.TradeKey;

/**
 * A {@code KafkaClient} that publishes various types of data to various topics. As a
 * {@code KafkaClient}, the implementation is generated by Micronaut.
 *
 * @author jeremy
 */
@KafkaClient
public interface KafkaProducer {
    @Topic("${kafka-topic.benchmark-topic}")
    public void publishBenchmarkEvent(@KafkaKey PortfolioKey benchmarkKey,
            PortfolioEvent benchmarkEvent);

    @Topic("${kafka-topic.portfolio-topic}")
    public void publishPortfolioEvent(@KafkaKey PortfolioKey portfolioKey,
            PortfolioEvent portfolioEvent);

    @Topic("${kafka-topic.portfolio-request-topic}")
    public void publishPortfolioRequest(@KafkaKey PortfolioKey portfolioKey,
            PortfolioEvaluationRequest request);

    @Topic("${kafka-topic.portfolio-result-topic}")
    public void publishPortfolioResult(@KafkaKey PortfolioKey portfolioKey,
            EvaluationResult result);

    @Topic("${kafka-topic.position-topic}")
    public void publishPosition(@KafkaKey PositionKey positionKey, Position position);

    @Topic("${kafka-topic.rules-topic}")
    public void publishRule(@KafkaKey RuleKey ruleKey, Rule rule);

    @Topic("${kafka-topic.security-topic}")
    public void publishSecurity(@KafkaKey SecurityKey securityKey, Security security);

    @Topic("${kafka-topic.trade-result-topic}")
    public void publishTradeResult(@KafkaKey TradeKey tradeKey, TradeEvaluationResult result);

    @Topic("${kafka-topic.trade-request-topic}")
    public void publisTradeRequest(@KafkaKey TradeKey tradeKey, TradeEvaluationRequest request);
}
