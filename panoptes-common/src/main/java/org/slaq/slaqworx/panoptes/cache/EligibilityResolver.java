package org.slaq.slaqworx.panoptes.cache;

import java.util.Set;
import org.slaq.slaqworx.panoptes.asset.EligibilityListProvider;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * A service which can determine eligibility of {@link Security} entities for investment, via
 * heuristics such as membership (or not) in an eligibility list, membership in the constituent
 * securities of an index, etc.
 *
 * @author jeremy
 */
public class EligibilityResolver {
  private final EligibilityListProvider eligibilityListProvider;

  /**
   * Creates a new {@link EligibilityResolver} that uses the given {@link EligibilityListProvider}
   * to resolve resources such as eligibility lists.
   *
   * @param eligibilityListProvider the {@link EligibilityListProvider} to use
   */
  public EligibilityResolver(EligibilityListProvider eligibilityListProvider) {
    this.eligibilityListProvider = eligibilityListProvider;
  }

  /**
   * Determines whether the given {@link Security} is a member of the specified country list, that
   * is, the value of the security's {@code country} attribute appears in the list.
   *
   * @param security the {@link Security} to test
   * @param listName the name of the list on which to test membership
   * @return {@code true} if the security's {@code country} appears in the specified country list,
   *     {@code false} otherwise
   */
  public boolean isCountryListMember(Security security, String listName) {
    String country = security.getAttributeValue(SecurityAttribute.country);
    Set<String> countryList = eligibilityListProvider.getEligibilityList(listName);

    return (countryList.contains(country));
  }

  /**
   * Determines whether the given {@link Security} is a member of the specified issuer list, that
   * is, the value of the security's {@code issuer} attribute appears in the list.
   *
   * @param security the {@link Security} to test
   * @param listName the name of the list on which to test membership
   * @return {@code true} if the security's {@code issuer} appears in the specified issuer list,
   *     {@code false} otherwise
   */
  public boolean isIssuerListMember(Security security, String listName) {
    String issuer = security.getAttributeValue(SecurityAttribute.issuer);
    Set<String> issuerList = eligibilityListProvider.getEligibilityList(listName);

    return (issuerList.contains(issuer));
  }

  /**
   * Determines whether the given {@link Security} is a member of the specified security list, that
   * is, the value of the security's {@code cusip} or {@code isin} attribute appears in the list.
   *
   * @param security the {@link Security} to test
   * @param listName the name of the list on which to test membership
   * @return {@code true} if the security's {@code cusip} or {@code isin} appears in the specified
   *     security list, {@code false} otherwise
   */
  public boolean isSecurityListMember(Security security, String listName) {
    String cusip = security.getAttributeValue(SecurityAttribute.cusip);
    String isin = security.getAttributeValue(SecurityAttribute.isin);
    Set<String> securityList = eligibilityListProvider.getEligibilityList(listName);

    return (securityList.contains(cusip) || securityList.contains(isin));
  }
}
