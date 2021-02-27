package org.slaq.slaqworx.panoptes.util;

/**
 * Indicates that an entity can be referenced by a key of the specified type.
 *
 * @param <T>
 *     the ID/key type
 *
 * @author jeremy
 */
@FunctionalInterface
public interface Keyed<T> {
  /**
   * Obtains the ID/key for this {@code Keyed} entity.
   *
   * @return this entity's key
   */
  T getKey();
}
