package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.ProxyFactory;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.RuleKey;
import org.slaq.slaqworx.panoptes.rule.RuleProxy;

/**
 * PortfolioSerializerTest tests the functionality of the PortfolioSerializer.
 *
 * @author jeremy
 */
public class PortfolioSerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        PortfolioSerializer serializer =
                new PortfolioSerializer(new ProxyFactory(key -> null, key -> null));

        Portfolio portfolio = TestUtil.p1;
        byte[] buffer = serializer.write(TestUtil.p1);
        Portfolio deserialized = serializer.read(buffer);

        assertEquals("deserialized value should equals() original value", portfolio, deserialized);
        assertEquals("deserialized value should have same benchmark key as original",
                portfolio.getBenchmarkKey(), deserialized.getBenchmarkKey());
        assertEquals("deserialized value should have same name as original", portfolio.getName(),
                deserialized.getName());
        // TOOD assert same Positions and Rules

        Set<? extends Position> positions = TestUtil.p1Positions;
        Collection<? extends Rule> rules = Set.of(new RuleProxy(new RuleKey("test"), null));
        portfolio = new Portfolio(new PortfolioKey("test", 31337), "Test Portfolio", positions,
                new PortfolioKey("benchmark", 1), rules);

        buffer = serializer.write(portfolio);
        deserialized = serializer.read(buffer);

        assertEquals("deserialized value should equals() original value", portfolio, deserialized);
        assertEquals("deserialized value should have same benchmark key as original",
                portfolio.getBenchmarkKey(), deserialized.getBenchmarkKey());
        assertEquals("deserialized value should have same name as original", portfolio.getName(),
                deserialized.getName());
    }
}
