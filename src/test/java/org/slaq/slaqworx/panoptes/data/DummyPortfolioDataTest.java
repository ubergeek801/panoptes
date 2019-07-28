package org.slaq.slaqworx.panoptes.data;

import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.hazelcast.core.HazelcastInstance;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

/**
 * DummyPortfolioDataTest tests that loading dummy Portfolio data works as expected.
 *
 * @author jeremy
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DummyPortfolioDataTest {
    @Autowired
    HazelcastInstance hazelcastInstance;

    @Autowired
    PortfolioCache portfolioCache;

    /**
     * Tests that Portfolio data can be loaded and is available to the Hazelcast cache.
     */
    @Test
    public void testLoadData() {
        Map<PortfolioKey, Portfolio> portfolios = hazelcastInstance.getMap("portfolio");
        assertFalse("portfolios should contain some data", portfolios.isEmpty());
    }
}
