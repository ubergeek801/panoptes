package org.slaq.slaqworx.panoptes.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

/**
 * {@code AssetCacheTest} tests the functionality of the {@code AssetCache}.
 *
 * @author jeremy
 */
@MicronautTest
public class AssetCacheTest {
    @Inject
    private AssetCache assetCache;

    /**
     * Tests that the various methods for obtaining distinct values of certain security attributes
     * (e.g. {@code getCountries()}) behave as expected.
     */
    @Test
    public void testDistinctGetters() {
        Map<SecurityKey, Security> securityMap = assetCache.getSecurityCache();
        securityMap.clear();

        String country1 = "country1";
        String country2 = "country2";
        String currency1 = "currency1";
        String currency2 = "currency2";
        String region1 = "region1";
        String sector1 = "sector1";
        String sector2 = "sector2";
        String sector3 = "sector3";

        Map<SecurityAttribute<?>, ? super Object> attributes =
                SecurityAttribute.mapOf(SecurityAttribute.isin, "test1", SecurityAttribute.country,
                        country1, SecurityAttribute.currency, currency1, SecurityAttribute.region,
                        region1, SecurityAttribute.sector, sector1);
        Security security = new Security(attributes);
        securityMap.put(security.getKey(), security);

        attributes = SecurityAttribute.mapOf(SecurityAttribute.isin, "test2",
                SecurityAttribute.country, country1, SecurityAttribute.currency, currency1,
                SecurityAttribute.region, region1, SecurityAttribute.sector, sector2);
        security = new Security(attributes);
        securityMap.put(security.getKey(), security);

        attributes = SecurityAttribute.mapOf(SecurityAttribute.isin, "test3",
                SecurityAttribute.country, country2, SecurityAttribute.currency, currency2,
                SecurityAttribute.region, region1, SecurityAttribute.sector, sector2);
        security = new Security(attributes);
        securityMap.put(security.getKey(), security);

        attributes = SecurityAttribute.mapOf(SecurityAttribute.isin, "test4",
                SecurityAttribute.country, country2, SecurityAttribute.currency, currency2,
                SecurityAttribute.region, region1, SecurityAttribute.sector, sector3);
        security = new Security(attributes);
        securityMap.put(security.getKey(), security);

        attributes = SecurityAttribute.mapOf(SecurityAttribute.isin, "test5",
                SecurityAttribute.country, country2, SecurityAttribute.currency, currency2,
                SecurityAttribute.region, region1);
        security = new Security(attributes);
        securityMap.put(security.getKey(), security);

        Set<String> countries = assetCache.getCountries();
        assertEquals(2, countries.size(), "unexpected number of countries");
        assertTrue(countries.contains(country1), "countries should have contained " + country1);
        assertTrue(countries.contains(country2), "countries should have contained " + country2);

        Set<String> currencies = assetCache.getCurrencies();
        assertEquals(2, currencies.size(), "unexpected number of currencies");
        assertTrue(currencies.contains(currency1), "currencies should have contained " + currency1);
        assertTrue(currencies.contains(currency2), "currencies should have contained " + currency2);

        Set<String> regions = assetCache.getRegions();
        assertEquals(1, regions.size(), "unexpected number of regions");
        assertTrue(regions.contains(region1), "regions should have contained " + region1);

        Set<String> sectors = assetCache.getSectors();
        assertEquals(3, sectors.size(), "unexpected number of sectors");
        assertTrue(sectors.contains(sector1), "sectors should have contained " + sector1);
        assertTrue(sectors.contains(sector2), "sectors should have contained " + sector2);
        assertTrue(sectors.contains(sector3), "sectors should have contained " + sector3);
    }
}
