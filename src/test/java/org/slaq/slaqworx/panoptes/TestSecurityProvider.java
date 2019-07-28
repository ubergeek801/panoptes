package org.slaq.slaqworx.panoptes;

import java.util.HashMap;
import java.util.Map;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;

public class TestSecurityProvider implements SecurityProvider {
    private final HashMap<SecurityKey, Security> securityMap = new HashMap<>();

    /**
     * Creates a new TestSecurityProvider. Restricted because instances of this class should be
     * obtained through TestUtil.
     */
    protected TestSecurityProvider() {
        // nothing to do
    }

    @Override
    public Security getSecurity(SecurityKey key) {
        return securityMap.get(key);
    }

    /**
     * Creates a new Security and makes it available through this provider.
     *
     * @param cusip
     *            the Security CUSIP
     * @param attributes
     *            the attributes to associate with the Security
     * @return the newly created Security
     */
    public Security newSecurity(String cusip, Map<SecurityAttribute<?>, Object> attributes) {
        SecurityKey key = new SecurityKey(cusip, 1);
        Security security = new Security(key, attributes);
        securityMap.put(key, security);

        return security;
    }
}
