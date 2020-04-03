package org.slaq.slaqworx.panoptes.util;

/**
 * {@code Keyed} indicates that an entity can be referenced by a key of the specified type.
 *
 * @author jeremy
 * @param <T>
 *            the ID/key type
 */
@FunctionalInterface
public interface Keyed<T> {
    /**
     * Obtains the ID/key for this {@code Keyed} entity.
     *
     * @return this entity's key
     */
    public T getKey();
}
