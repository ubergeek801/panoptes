package org.apache.ignite.internal.processors.resource;

/**
 * {@code PublicGridResourceField} is an abuse of {@code GridResourceField} which makes it usable
 * outside its package.
 *
 * @author jeremy
 */
public class PublicGridResourceField extends GridResourceField {
    /**
     * Creates a new {@code PublicGridResourceField} wrapping the given {@code GridResourceField}.
     *
     * @param field
     *            the field to be wrapped
     */
    public PublicGridResourceField(GridResourceField field) {
        super(field.getField(), field.getAnnotation());
    }
}
