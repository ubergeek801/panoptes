package org.apache.ignite.internal.processors.resource;

/**
 * {@code PublicGridResourceMethod} is an abuse of {@code GridResourceMethod} which makes it usable
 * outside its package.
 *
 * @author jeremy
 */
public class PublicGridResourceMethod extends GridResourceMethod {
    /**
     * Creates a new {@code PublicGridResourceMethod} wrapping the given {@code GridResourceMethod}.
     *
     * @param method
     *            the method to be wrapped
     */
    public PublicGridResourceMethod(GridResourceMethod method) {
        super(method.getMethod(), method.getAnnotation());
    }
}
