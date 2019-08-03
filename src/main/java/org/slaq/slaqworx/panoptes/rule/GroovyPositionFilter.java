package org.slaq.slaqworx.panoptes.rule;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.function.Predicate;

import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityProvider;

import groovy.lang.GroovyClassLoader;

/**
 * GroovyPositionFilter is a Position-based Predicate that can be used as a Position filter for Rule
 * evaluation.
 *
 * @author jeremy
 */
public class GroovyPositionFilter implements Predicate<PositionEvaluationContext>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    private final String expression;
    private transient final Predicate<PositionEvaluationContext> groovyFilter;

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

        StringBuilder classDef = new StringBuilder("import " + Predicate.class.getName() + "\n");
        classDef.append("import " + Position.class.getName() + "\n");
        classDef.append("import " + PositionEvaluationContext.class.getName() + "\n");
        classDef.append("import " + Security.class.getName() + "\n");
        classDef.append("import " + SecurityAttribute.class.getName() + "\n");
        classDef.append("import " + SecurityProvider.class.getName() + "\n");
        classDef.append("class GroovyFilter implements Predicate<PositionEvaluationContext> {\n");
        classDef.append(" boolean test(PositionEvaluationContext ctx) {\n");
        classDef.append("  Position p = ctx.position\n");
        classDef.append("  Security s = p.getSecurity(ctx.evaluationContext.securityProvider)\n");
        classDef.append("  return " + expression);
        classDef.append(" }");
        classDef.append("}");

        Class<Predicate<PositionEvaluationContext>> filterClass =
                groovyClassLoader.parseClass(classDef.toString());
        try {
            Constructor<Predicate<PositionEvaluationContext>> filterClassConstructor =
                    filterClass.getConstructor();
            groovyFilter = filterClassConstructor.newInstance();
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not instantaite Groovy filter", e);
        }
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
