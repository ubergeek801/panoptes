package org.slaq.slaqworx.panoptes.util;

/**
 * Keyed indicates that an entity can be referenced by a key of the specified type.
 * 
 * @author jeremy
 * @param <T>
 *            the ID/key type
 */
@FunctionalInterface
public interface Keyed<T> {
    /**
     * Obtains the ID/key for this Keyed entity.
     *
     * @return this entity's ID
     */
    public T getId();
}
