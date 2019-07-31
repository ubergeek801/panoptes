package org.slaq.slaqworx.panoptes.rule;

/**
 * RuleProvider is the interface for a service that provides access to Rule data.
 *
 * @author jeremy
 */
@FunctionalInterface
public interface RuleProvider {
    /**
     * Obtains the Rule corresponding to the given key.
     *
     * @param key
     *            the key identifying the Rule to be obtained
     * @return the Rule corresponding to the given key, or null if it could not be located
     */
    public Rule getRule(RuleKey key);
}
