package org.slaq.slaqworx.panoptes.rule;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;

import groovy.lang.GroovyClassLoader;

/**
 * {@code GroovyPositionFilter} is a {@code Position}-based {@code Predicate} that can be used as a
 * {@code Position} filter for {@code Rule} evaluation. Currently it is the only such filter that
 * can be persisted.
 *
 * @author jeremy
 */
public class GroovyPositionFilter implements Predicate<PositionEvaluationContext> {
    private static final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    private static final ConcurrentHashMap<String, Predicate<PositionEvaluationContext>> expressionFilterMap =
            new ConcurrentHashMap<>();

    private final String expression;
    private final Predicate<PositionEvaluationContext> groovyFilter;

    /**
     * Creates a new GroovyPositionFilter using the given Groovy expression.
     *
     * @param expression
     *            a Groovy expression suitable for use as a Position filter
     * @param securityProvider
     *            the SecurityProvider for use by the filter
     */
    public GroovyPositionFilter(String expression) {
        this.expression = expression;

        groovyFilter = expressionFilterMap.computeIfAbsent(expression, e -> {
            StringBuilder classDef = new StringBuilder("package org.slaq.slaqworx.panoptes.rule\n");
            classDef.append("import " + Predicate.class.getName() + "\n");
            classDef.append("import " + Position.class.getName() + "\n");
            classDef.append("import " + PositionEvaluationContext.class.getName() + "\n");
            classDef.append("import " + Security.class.getName() + "\n");
            classDef.append(
                    "class GroovyFilter implements Predicate<PositionEvaluationContext> {\n");
            classDef.append(" boolean test(PositionEvaluationContext ctx) {\n");
            classDef.append("  Position p = ctx.position\n");
            classDef.append(
                    "  Security s = p.getSecurity(ctx.evaluationContext.securityProvider)\n");
            classDef.append("  return " + expression);
            classDef.append(" }");
            classDef.append("}");

            Class<Predicate<PositionEvaluationContext>> filterClass =
                    groovyClassLoader.parseClass(classDef.toString());

            try {
                Constructor<Predicate<PositionEvaluationContext>> filterClassConstructor =
                        filterClass.getConstructor();
                return filterClassConstructor.newInstance();
            } catch (Exception ex) {
                // TODO throw a better exception
                throw new RuntimeException("could not instantiate Groovy filter", ex);
            }
        });
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
    public boolean test(PositionEvaluationContext position) {
        return groovyFilter.test(position);
    }
}
