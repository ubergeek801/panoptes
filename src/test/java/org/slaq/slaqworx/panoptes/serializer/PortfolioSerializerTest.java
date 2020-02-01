package org.slaq.slaqworx.panoptes.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
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
        assertEquals(portfolio.getPositions().count(), deserialized.getPositions().count(),
                "deserialized Portfolio should have same number of Positions as original");

        Comparator<Position> positionComparator =
                (p1, p2) -> p1.getKey().getId().compareTo(p2.getKey().getId());

        // sort the Position lists so we can compare elements
        ArrayList<Position> originalPositions = new ArrayList<>();
        portfolio.getPositions().forEach(originalPositions::add);
        originalPositions.sort(positionComparator);
        ArrayList<Position> deserializedPositions = new ArrayList<>();
        deserialized.getPositions().forEach(deserializedPositions::add);
        deserializedPositions.sort(positionComparator);

        Iterator<Position> positionIter = originalPositions.iterator();
        Iterator<Position> deserializedPositionIter = deserializedPositions.iterator();
        while (positionIter.hasNext()) {
            Position position = positionIter.next();
            Position deserializedPosition = deserializedPositionIter.next();

            assertEquals(position, deserializedPosition,
                    "deserialized Position should equals() original");
            assertEquals(position.getKey(), deserializedPosition.getKey(),
                    "deserialized Position should have same key as original");
            assertEquals(position.getAmount(), deserializedPosition.getAmount(), TestUtil.EPSILON,
                    "deserialized Position should have same amount as original");
            assertEquals(position.getSecurityKey(), deserializedPosition.getSecurityKey(),
                    "deserialized Position should have same SecurityKey as original");
        }

        Set<? extends Position> positions = TestUtil.p1Positions;
        Rule testRule = TestUtil.testRuleProvider().newConcentrationRule(null, "test rule",
                GroovyPositionFilter.of("s.region == 'Emerging Markets'"), null, 0.1, null);
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
        assertEquals(portfolio.getRules().count(), deserialized.getRules().count(),
                "deserialized Portfolio should have same number of Rules as original");

        Iterator<Rule> ruleIter = portfolio.getRules().iterator();
        Iterator<Rule> deserializedRuleIter = deserialized.getRules().iterator();
        while (ruleIter.hasNext()) {
            Rule rule = ruleIter.next();
            Rule deserializedRule = deserializedRuleIter.next();

            assertEquals(rule, deserializedRule, "deserialized Rule should equals() original");
            assertEquals(rule.getKey(), deserializedRule.getKey(),
                    "deserialized Rule should have same key as original");
            assertEquals(rule.getDescription(), deserializedRule.getDescription(),
                    "deserialized Rule should have same description as original");
            // TODO compare classifiers and aggregators
        }
    }
}
