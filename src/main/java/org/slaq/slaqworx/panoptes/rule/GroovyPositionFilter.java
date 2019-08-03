package org.slaq.slaqworx.panoptes.rule;

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
public class GroovyPositionFilter implements Predicate<Position> {
    private static final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    private final String expression;
    private final SecurityProvider securityProvider;
    private final Constructor<Predicate<Position>> filterClassConstructor;

    /**
     * Creates a new GroovyPositionFilter using the given Groovy expression.
     *
     * @param expression
     *            a Groovy expression suitable for use as a Position filter
     * @param securityProvider
     *            the SecurityProvider for use by the filter
     */
    protected GroovyPositionFilter(String expression, SecurityProvider securityProvider) {
        this.expression = expression;
        this.securityProvider = securityProvider;

        StringBuilder classDef = new StringBuilder("import " + Predicate.class.getName() + "\n");
        classDef.append("import " + Position.class.getName() + "\n");
        classDef.append("import " + Security.class.getName() + "\n");
        classDef.append("import " + SecurityAttribute.class.getName() + "\n");
        classDef.append("import " + SecurityProvider.class.getName() + "\n");
        classDef.append("class GroovyFilter implements Predicate<Position> {\n");
        classDef.append(" SecurityProvider secProvider\n");
        classDef.append(" GroovyFilter(SecurityProvider secProvider) {\n");
        classDef.append("  this.secProvider = secProvider\n");
        classDef.append(" }\n");
        classDef.append(" boolean test(Position p) {\n");
        classDef.append("  Security s = p.getSecurity(secProvider)\n");
        classDef.append("  return " + expression);
        classDef.append(" }");
        classDef.append("}");

        Class<Predicate<Position>> filterClass = groovyClassLoader.parseClass(classDef.toString());
        try {
            filterClassConstructor = filterClass.getConstructor(SecurityProvider.class);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not get Groovy filter constructor", e);
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
    public boolean test(Position position) {
        Predicate<Position> groovyFilter;
        try {
            groovyFilter = filterClassConstructor.newInstance(securityProvider);
        } catch (Exception e) {
            // TODO throw a better exception
            throw new RuntimeException("could not instantiate Groovy filter", e);
        }

        return groovyFilter.test(position);
    }
}
