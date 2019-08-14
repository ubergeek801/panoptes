package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A {@code SecurityAttribute} identifies a particular attribute of a {@code Security}.
 *
 * @author jeremy
 * @param <T>
 *            the type which values of this attribute implement
 */
public class SecurityAttribute<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Map<String, SecurityAttribute<?>> attributesByName =
            new HashMap<>(100, 0.5f);
    private static final Map<Integer, SecurityAttribute<?>> attributesByIndex =
            new HashMap<>(100, 0.5f);

    /**
     * Obtains the {@code SecurityAttribute} corresponding to the given index, if it exists. For
     * most purposes, the name should be used, as indices may change over time.
     *
     * @param index
     *            the index of the {@code SecurityAttribute} to obtain
     * @return the {@code SecurityAttribute} corresponding to the given index, or {@code null} if it
     *         does not exist
     */
    public static SecurityAttribute<?> of(int index) {
        return attributesByIndex.get(index);
    }

    /**
     * Obtains the {@code SecurityAttribute} corresponding to the given name, if it exists.
     *
     * @param name
     *            the name of the {@code SecurityAttribute} to obtain
     * @return the {@code SecurityAttribute} corresponding to the given name, or {@code null} if it
     *         does not exist
     */
    @JsonCreator
    public static SecurityAttribute<?> of(String name) {
        return attributesByName.get(name);
    }

    /**
     * Obtains (or creates) a {@code SecurityAttribute} with the given name and value type.
     *
     * @param <T>
     *            the value type of the {@code SecurityAttribute}
     * @param name
     *            the unique name of the {@code SecurityAttribute}
     * @param index
     *            the index of the {@code SecurityAttribute} in an attributes array
     * @param type
     *            the {@code Class} of the value type
     * @return an existing {@code SecurityAttribute} if already defined, otherwise a new
     *         {@code SecurityAttribute}
     */
    public static <T> SecurityAttribute<T> of(String name, int index, Class<T> type) {
        @SuppressWarnings("unchecked")
        SecurityAttribute<T> attribute = (SecurityAttribute<T>)attributesByName
                .computeIfAbsent(name, n -> new SecurityAttribute<>(name, index, type));
        attributesByIndex.put(index, attribute);

        return attribute;
    }

    private final String name;
    private final int index;

    private final Class<T> type;

    /**
     * Creates a new {@code SecurityAttribute} with the given name and index. Restricted to enforce
     * use of the {@code of()} factory method.
     *
     * @param name
     *            the unique name of the {@code SecurityAttribute}
     * @param index
     *            the index of the {@code SecurityAttribute} in an attributes array
     * @param type
     *            the {@code Class} of the value type
     */
    private SecurityAttribute(String name, int index, Class<T> type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SecurityAttribute<?> other = (SecurityAttribute<?>)obj;

        return name.equals(other.name);
    }

    /**
     * Obtains the index at which this {@code SecurityAttribute} may be found in an attributes
     * array.
     *
     * @return the array index for this {@code SecurityAttribute}
     */
    public int getIndex() {
        return index;
    }

    /**
     * Obtains the name of this {@code SecurityAttribute}.
     *
     * @return the attribute name
     */
    public String getName() {
        return name;
    }

    /**
     * Obtains the type implemented by values of this {@code SecurityAttribute}.
     *
     * @return the {@code SecurityAttribute} value type
     */
    public Class<T> getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @JsonValue
    public String toJsonString() {
        return name;
    }

    @Override
    public String toString() {
        return "SecurityAttribute[\"" + name + "\"]";
    }
}
