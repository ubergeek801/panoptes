package org.slaq.slaqworx.panoptes.serializer;

/**
 * {@code SerializerTypeId} enumerates the type IDs used by Hazelcast serializers.
 *
 * @author jeremy
 */
public enum SerializerTypeId {
    DONT_USE_ZERO,
    POSITION_KEY,
    POSITION,
    PORTFOLIO_KEY,
    PORTFOLIO,
    PORTFOLIO_SUMMARY,
    SECURITY_KEY,
    SECURITY,
    RULE_KEY,
    RULE,
    RULE_SUMMARY,
    TRANSACTION_KEY,
    TRANSACTION,
    TRADE_KEY,
    TRADE,
    EVALUATION_CONTEXT,
    PORTFOLIO_EVALUATION_REQUEST,
    ROOM_EVALUATION_REQUEST,
    TRADE_EVALUATION_REQUEST,
    EXCEPTION,
    RULE_RESULT,
    EVALUATION_RESULT,
    RULE_IMPACT,
    PORTFOLIO_RULE_IMPACT,
    TRADE_EVALUATION_RESULT
}
