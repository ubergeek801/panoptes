package org.slaq.slaqworx.panoptes.asset;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A SecurityAttribute identifies a particular attribute of a Security.
 *
 * @author jeremy
 * @param <T>
 *            the type which values of this attribute implement
 */
@Entity
public class SecurityAttribute<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Map<String, SecurityAttribute<?>> attributes = new HashMap<>(100, 0.5f);

    @JsonCreator
    public static SecurityAttribute<?> fromJsonString(String string) {
        return of(string);
    }

    /**
     * Obtains the SecurityAttribute corresponding to the given name, if it exists.
     *
     * @param name
     *            the name of the SecurityAttribute to obtain
     * @return the SecurityAttribute corresponding to the given name, or null if it does not exist
     */
    public static SecurityAttribute<?> of(String name) {
        return attributes.get(name);
    }

    /**
     * Obtains (or creates) a SecurityAttribute with the given name and value type.
     *
     * @param <T>
     *            the value type of the SecurityAttribute
     * @param name
     *            the unique name of the SecurityAttribute
     * @param index
     *            the index of the SecurityAttribute in an attributes array
     * @param type
     *            the Class of the value type
     * @return an existing SecurityAttribute if already defined, otherwise a new SecurityAttribute
     */
    public static <T> SecurityAttribute<T> of(String name, int index, Class<T> type) {
        @SuppressWarnings("unchecked")
        SecurityAttribute<T> attribute = (SecurityAttribute<T>)attributes.computeIfAbsent(name,
                n -> new SecurityAttribute<>(name, index, type));
        return attribute;
    }

    @Id
    private final String name;
    private final int index;

    @Type(type = "org.hibernate.type.StringType")
    private final Class<T> type;

    /**
     * Creates a new SecurityAttribute with the given name and index. Restricted to enforce use of
     * the of() factory method.
     *
     * @param name
     *            the unique name of the SecurityAttribute
     * @param index
     *            the index of the SecurityAttribute in an attributes array
     * @param type
     *            the Class of the value type
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
     * Obtains the index at which this SecurityAttribute may be found in an attributes array.
     *
     * @return the array index for this SecurityAttribute
     */
    public int getIndex() {
        return index;
    }

    /**
     * Obtains the name of this SecurityAttribute.
     *
     * @return the attribute name
     */
    public String getName() {
        return name;
    }

    /**
     * Obtains the type implemented by values of this SecurityAttribute.
     *
     * @return the SecurityAttribute value type
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
