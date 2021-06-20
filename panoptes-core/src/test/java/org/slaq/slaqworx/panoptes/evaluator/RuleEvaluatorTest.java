package org.slaq.slaqworx.panoptes.evaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import org.junit.jupiter.api.Test;
import org.slaq.slaqworx.panoptes.NoDataException;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.rule.ConcentrationRule;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroup;
import org.slaq.slaqworx.panoptes.rule.EvaluationGroupClassifier;
import org.slaq.slaqworx.panoptes.rule.GenericRule;
import org.slaq.slaqworx.panoptes.rule.GroovyPositionFilter;
import org.slaq.slaqworx.panoptes.rule.PositionEvaluationContext;
import org.slaq.slaqworx.panoptes.rule.Rule;
import org.slaq.slaqworx.panoptes.rule.ValueResult;
import org.slaq.slaqworx.panoptes.rule.WeightedAverageRule;
import org.slaq.slaqworx.panoptes.test.TestUtil;

/**
 * Tests the functionality of the {@link RuleEvaluator}.
 *
 * @author jeremy
 */
@MicronautTest
public class RuleEvaluatorTest {
  @Inject
  private AssetCache assetCache;

  /**
   * Tests that {@link Position} classification behaves as expected.
   */
  @Test
  public void testClassify() {
    // a dumb classifier that merely "classifies" by security ID
    EvaluationGroupClassifier classifier =
        (ctx -> new EvaluationGroup(ctx.get().getPosition().getSecurityKey().id(), "id"));
    Rule rule = new GenericRule(null, "dummy rule", classifier) {
      @Nonnull
      @Override
      protected ValueResult eval(@Nonnull PositionSupplier positions,
          @Nonnull EvaluationGroup evaluationGroup, @Nonnull EvaluationContext evaluationContext) {
        // will not actually be invoked anyway
        return null;
      }
    };

    Map<EvaluationGroup, PositionSupplier> classifiedPositions =
        new RuleEvaluator(rule, null, TestUtil.defaultTestEvaluationContext())
            .classify(TestUtil.p1, null);
    assertEquals(2, classifiedPositions.size(),
        "Positions in distinct Securities should have been classified distinctly");
  }

  /**
   * Tests that {@link Position} evaluation behaves as expected.
   */
  @Test
  public void testEvaluate() {
    // a dumb classifier that merely "classifies" by security ID
    EvaluationGroupClassifier classifier =
        (ctx -> new EvaluationGroup(ctx.get().getPosition().getSecurityKey().id(), "id"));
    // a dumb filter that matches Positions in s1
    Predicate<PositionEvaluationContext> filter =
        (c -> c.getPosition().getSecurityKey().equals(TestUtil.s1.getKey()));
    Rule rule =
        new WeightedAverageRule<>(null, "test rule", filter, SecurityAttribute.duration, 0d, 3.9,
            classifier);

    EvaluationResult result =
        new RuleEvaluator(rule, TestUtil.p1, TestUtil.defaultTestEvaluationContext()).call();
    assertNotNull(result, "result should never be null");
    Map<EvaluationGroup, ValueResult> groupedResults = result.results();
    assertNotNull(groupedResults, "result groups should never be null");
    assertEquals(1, groupedResults.size(),
        "a single (filtered) Position should result in a single group");
    ValueResult ruleResult = groupedResults.values().iterator().next();
    assertFalse(ruleResult.isPassed(), "rule with 3.9 upper limit should have failed");

    rule =
        new WeightedAverageRule<>(null, "test rule", filter, SecurityAttribute.duration, 3.9, 4.1,
            classifier);
    result = new RuleEvaluator(rule, TestUtil.p1, TestUtil.defaultTestEvaluationContext()).call();
    assertTrue(result.isPassed(),
        "rule with 3.9 lower limit and 4.1 upper limit should have passed");

    // p1's concentration in s1 is 66.667% so should fail this rule
    rule = new ConcentrationRule(null, "test rule", filter, null, 0.65, null);
    result = new RuleEvaluator(rule, TestUtil.p1, TestUtil.defaultTestEvaluationContext()).call();
    assertFalse(result.isPassed(), "rule with 65% upper limit should have failed");
    // ...and should pass this rule
    rule = new ConcentrationRule(null, "test rule", filter, null, 0.67, null);
    result = new RuleEvaluator(rule, TestUtil.p1, TestUtil.defaultTestEvaluationContext()).call();
    assertTrue(result.isPassed(), "rule with 67% upper limit should have passed");
  }

  /**
   * Tests that {@link Position} evaluation behaves as expected when an exception is thrown during
   * calculation.
   */
  @Test
  public void testEvaluate_exception() {
    Security s1 = TestUtil.createTestSecurity(assetCache, "s1", "issuer1", 100);
    // a Security without an issuer will cause problems with an issuer filter later
    Security s2 = TestUtil.createTestSecurity(assetCache, "s2",
        SecurityAttribute.mapOf(SecurityAttribute.price, 100d));

    Position p1 = TestUtil.createTestPosition(assetCache, 250, s1);
    Position p2 = TestUtil.createTestPosition(assetCache, 750, s2);

    Set<Position> positions = Set.of(p1, p2);
    Portfolio portfolio = TestUtil
        .createTestPortfolio(assetCache, "test", "Test Portfolio", positions, null,
            Collections.emptyList());

    GroovyPositionFilter filter = GroovyPositionFilter.of("s.issuer == 'issuer1'");
    ConcentrationRule rule = new ConcentrationRule(null, "test rule", filter, 0d, 10d, null);

    EvaluationContext context = TestUtil.defaultTestEvaluationContext();
    EvaluationResult results = new RuleEvaluator(rule, portfolio, context).call();

    // there was no grouping so there should be only one result
    assertEquals(1, results.results().size(), "unexpected number of results");
    ValueResult result = results.getResult(EvaluationGroup.defaultGroup());
    assertNotNull(result, "expected to find result for default group");
    // the Rule should have failed due to a NoDataException
    assertFalse(result.isPassed(), "failed evaluation result should not have passed");
    assertNull(result.getValue(), "failed evaluation result should not have a value");
    assertNotNull(result.getException(), "failed evaluation result should contain an exception");
    assertEquals(NoDataException.class, result.getException().getClass(),
        "failed evaluation result had unexpected exception type");
  }
}
