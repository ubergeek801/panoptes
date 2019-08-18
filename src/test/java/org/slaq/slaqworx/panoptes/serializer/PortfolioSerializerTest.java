package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.slaq.slaqworx.panoptes.TestUtil;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.rule.GroovyPositionFilter;
import org.slaq.slaqworx.panoptes.rule.Rule;

/**
 * {@code PortfolioSerializerTest} tests the functionality of the {@code PortfolioSerializer}.
 *
 * @author jeremy
 */
public class PortfolioSerializerTest {
    /**
     * Tests that (de)serialization works as expected.
     */
    @Test
    public void testSerialization() throws Exception {
        PortfolioSerializer serializer = new PortfolioSerializer(TestUtil.testPositionProvider(),
                TestUtil.testRuleProvider());

        Portfolio portfolio = TestUtil.p1;
        byte[] buffer = serializer.write(TestUtil.p1);
        Portfolio deserialized = serializer.read(buffer);

        assertEquals(portfolio, deserialized, "deserialized value should equals() original value");
        assertEquals(portfolio.getBenchmarkKey(), deserialized.getBenchmarkKey(),
                "deserialized value should have same benchmark key as original");
        assertEquals(portfolio.getName(), deserialized.getName(),
                "deserialized value should have same name as original");
        // TOOD assert same Positions and Rules

        Set<? extends Position> positions = TestUtil.p1Positions;
        Rule testRule = TestUtil.testRuleProvider().newConcentrationRule(null, "test rule",
                new GroovyPositionFilter("s.region == \"Emerging Markets\""), null, 0.1, null);
        Collection<Rule> rules = Set.of(testRule);
        portfolio = new Portfolio(new PortfolioKey("test", 31337), "Test Portfolio", positions,
                new PortfolioKey("benchmark", 1), rules);

        buffer = serializer.write(portfolio);
        deserialized = serializer.read(buffer);

        assertEquals(portfolio, deserialized, "deserialized value should equals() original value");
        assertEquals(portfolio.getBenchmarkKey(), deserialized.getBenchmarkKey(),
                "deserialized value should have same benchmark key as original");
        assertEquals(portfolio.getName(), deserialized.getName(),
                "deserialized value should have same name as original");
    }
}
