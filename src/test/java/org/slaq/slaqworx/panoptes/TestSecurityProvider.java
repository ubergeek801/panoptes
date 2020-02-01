package org.slaq.slaqworx.panoptes;

import java.util.HashMap;
import java.util.Map;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * {@code TestSecurityProvider} is a {@code SecurityProvider} suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestSecurityProvider implements SecurityProvider {
    private final HashMap<SecurityKey, Security> securityMap = new HashMap<>();

    /**
     * Creates a new {@code TestSecurityProvider}. Restricted because instances of this class should
     * be obtained through {@code TestUtil}.
     */
    protected TestSecurityProvider() {
        // nothing to do
    }

    @Override
    public Security getSecurity(SecurityKey key, EvaluationContext evaluationContext) {
        return securityMap.get(key);
    }

    /**
     * Creates a new {@code Security} and makes it available through this provider.
     *
     * @param assetId
     *            the asset ID to assign to the {@code Security}; may be null iff attributes
     *            contains ISIN
     * @param attributes
     *            the additional attributes to associate with the {@code Security}
     * @return the newly created {@code Security}
     */
    public Security newSecurity(String assetId, Map<SecurityAttribute<?>, Object> attributes) {
        if (assetId != null) {
            attributes = new HashMap<>(attributes);
            attributes.put(SecurityAttribute.isin, assetId);
        }
        Security security = new Security(attributes);
        securityMap.put(security.getKey(), security);

        return security;
    }
}
