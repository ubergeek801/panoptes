package org.slaq.slaqworx.panoptes;

import java.util.HashMap;
import java.util.Map;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;

/**
 * TestSecurityProvider is a SecurityProvider suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestSecurityProvider implements SecurityProvider {
    private final HashMap<String, Security> securityMap = new HashMap<>();

    /**
     * Creates a new TestSecurityProvider. Restricted because instances of this class should be
     * obtained through TestUtil.
     */
    protected TestSecurityProvider() {
        // nothing to do
    }

    @Override
    public Security getSecurity(String key) {
        return securityMap.get(key);
    }

    /**
     * Creates a new Security and makes it available through this provider.
     *
     * @param attributes
     *            the attributes to associate with the Security
     * @return the newly created Security
     */
    public Security newSecurity(Map<SecurityAttribute<?>, Object> attributes) {
        Security security = new Security(attributes);
        securityMap.put(security.getId(), security);

        return security;
    }
}
