package org.slaq.slaqworx.panoptes.test;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;

/**
 * A {@link SecurityProvider} suitable for testing purposes.
 *
 * @author jeremy
 */
public class TestSecurityProvider implements SecurityProvider {
  private final HashMap<SecurityKey, Security> securityMap = new HashMap<>();

  /**
   * Creates a new {@link TestSecurityProvider}. Restricted because instances of this class should
   * be obtained through {@link TestUtil}.
   */
  protected TestSecurityProvider() {
    // nothing to do
  }

  @Override
  public Security getSecurity(
      @Nonnull SecurityKey key, @Nonnull EvaluationContext evaluationContext) {
    return securityMap.get(key);
  }

  /**
   * Creates a new {@link Security} and makes it available through this provider.
   *
   * @param assetId the asset ID to assign to the {@link Security}; may be {@code null} iff
   *     attributes contains ISIN
   * @param attributes the additional attributes to associate with the {@link Security}
   * @return the newly created {@link Security}
   */
  public Security newSecurity(
      String assetId, Map<SecurityAttribute<?>, ? super Object> attributes) {
    if (assetId != null) {
      attributes = new HashMap<>(attributes);
      attributes.put(SecurityAttribute.isin, assetId);
    }
    Security security = new Security(attributes);
    securityMap.put(security.getKey(), security);

    return security;
  }
}
