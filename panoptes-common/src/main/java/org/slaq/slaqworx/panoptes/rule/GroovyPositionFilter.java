package org.slaq.slaqworx.panoptes.rule;

import groovy.lang.GroovyClassLoader;
import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * A {@link Position}-based {@link Predicate} that can be used as a {@link Position} filter for
 * {@link Rule} evaluation. Currently it is the only such filter that can be persisted.
 *
 * @author jeremy
 */
public class GroovyPositionFilter implements Predicate<PositionEvaluationContext> {
  private static final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
  private static final ConcurrentHashMap<String, GroovyPositionFilter> expressionFilterMap =
      new ConcurrentHashMap<>(25_000);
  private static final Pattern expressionTranslationPattern = Pattern.compile("s\\.(\\w+)");
  private final String expression;
  private final Predicate<PositionEvaluationContext> groovyFilter;

  /**
   * Creates a new {@link GroovyPositionFilter} using the given Groovy expression. Restricted
   * because instances of this class should be obtained through the {@code of()} factory method.
   *
   * @param expression
   *     a Groovy expression suitable for use as a {@link Position} filter
   */
  private GroovyPositionFilter(String expression) {
    this.expression = expression;

    // translate "shorthand" expressions like s.coupon into an equivalent
    // getAttributeValue() invocation, which is much faster
    Matcher securityExpressionMatcher = expressionTranslationPattern.matcher(expression);
    StringBuffer translatedExpression = new StringBuffer();
    while (securityExpressionMatcher.find()) {
      // if the matched substring corresponds to a known SecurityAttribute, substitute an
      // invocation
      String matchedSubstring = securityExpressionMatcher.group(1);
      SecurityAttribute<?> matchedAttribute = SecurityAttribute.of(matchedSubstring);
      String replacement = "s." + (matchedAttribute == null ? matchedSubstring :
          "getEffectiveAttributeValue(" + matchedAttribute.getIndex() + ", ctx)");
      securityExpressionMatcher.appendReplacement(translatedExpression, replacement);
    }
    securityExpressionMatcher.appendTail(translatedExpression);
    String translatedExpressionString = translatedExpression.toString();

    Class<?> filterClass;
    try {
      filterClass =
          groovyClassLoader.parseClass(toClassDefString(translatedExpressionString, true));
    } catch (CompilationFailedException e) {
      // try parsing without @groovy.transform.CompileStatic
      filterClass =
          groovyClassLoader.parseClass(toClassDefString(translatedExpressionString, false));
    }

    try {
      Constructor<Predicate<PositionEvaluationContext>> filterClassConstructor =
          (Constructor<Predicate<PositionEvaluationContext>>) filterClass.getConstructor();
      groovyFilter = filterClassConstructor.newInstance();
    } catch (Exception e) {
      // TODO throw a better exception
      throw new RuntimeException("could not instantiate Groovy filter", e);
    }
  }

  /**
   * Obtains a {@link GroovyPositionFilter} corresponding to the given filter expression.
   *
   * @param expression
   *     the expression for which to obtain a filter
   *
   * @return a {@link GroovyPositionFilter} compiled from the given expression, or {@code null} if
   *     the expression is empty
   */
  public static GroovyPositionFilter of(String expression) {
    if (StringUtils.isEmpty(expression)) {
      return null;
    }

    return expressionFilterMap.computeIfAbsent(expression, GroovyPositionFilter::new);
  }

  /**
   * Obtains the Groovy expression used to implement this filter.
   *
   * @return a Groovy expression
   */
  public String getExpression() {
    return expression;
  }

  @Override
  public boolean test(PositionEvaluationContext evaluationContext) {
    try {
      return groovyFilter.test(evaluationContext);
    } catch (Exception e) {
      // TODO make this logic available to all filters
      evaluationContext.setException(e);
      return true;
    }
  }

  /**
   * Translates the given expression to an equivalent Groovy class definition, implementing a {@link
   * Predicate}.
   *
   * @param expression
   *     the expression to be translated to a class definition
   * @param isCompileStatic
   *     {@code true} if the translated class should be annotated with {@code @groovy.transform
   *     .CompileStatic}, {@code false} otherwise
   *
   * @return a {@link String} containing a Groovy class definition
   */
  protected String toClassDefString(String expression, boolean isCompileStatic) {
    StringBuilder classDef = new StringBuilder("package org.slaq.slaqworx.panoptes.rule\n");
    classDef.append("import " + Predicate.class.getName() + "\n");
    classDef.append("import " + EvaluationContext.class.getName() + "\n");
    classDef.append("import " + Position.class.getName() + "\n");
    classDef.append("import " + PositionEvaluationContext.class.getName() + "\n");
    classDef.append("import " + Security.class.getName() + "\n");
    classDef.append("import " + SecurityAttribute.class.getName() + "\n");
    if (isCompileStatic) {
      classDef.append("@groovy.transform.CompileStatic\n");
    }
    classDef.append("class GroovyFilter implements Predicate<PositionEvaluationContext> {\n");
    classDef.append(" boolean test(PositionEvaluationContext pctx) {\n");
    classDef.append("  EvaluationContext ctx = pctx.evaluationContext\n");
    classDef.append("  Position p = pctx.position\n");
    classDef.append("  Security s = p.getSecurity(ctx)\n");
    classDef.append("  return " + expression + "\n");
    classDef.append(" }\n");
    classDef.append("}");

    return classDef.toString();
  }
}
