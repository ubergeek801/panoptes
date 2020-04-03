package org.slaq.slaqworx.panoptes.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface SerializablePredicate<T> extends Predicate<T>, Serializable {
    /**
     * Returns a predicate that tests if two arguments are equal according to
     * {@link Objects#equals(Object, Object)}.
     *
     * @param <T>
     *            the type of arguments to the predicate
     * @param targetRef
     *            the object reference with which to compare for equality, which may be {@code null}
     * @return a predicate that tests if two arguments are equal according to
     *         {@link Objects#equals(Object, Object)}
     */
    public static <T> SerializablePredicate<T> isEqual(Object targetRef) {
        return (null == targetRef) ? Objects::isNull : object -> targetRef.equals(object);
    }

    /**
     * Returns a predicate that is the negation of the supplied predicate. This is accomplished by
     * returning result of the calling {@code target.negate()}.
     *
     * @param <T>
     *            the type of arguments to the specified predicate
     * @param target
     *            predicate to negate
     * @return a predicate that negates the results of the supplied predicate
     * @throws NullPointerException
     *             if target is null
     * @since 11
     */
    @SuppressWarnings("unchecked")
    public static <T> SerializablePredicate<T> not(Predicate<? super T> target) {
        Objects.requireNonNull(target);
        return (SerializablePredicate<T>)target.negate();
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of this predicate
     * and another. When evaluating the composed predicate, if this predicate is {@code false}, then
     * the {@code other} predicate is not evaluated.
     * <p>
     * Any exceptions thrown during evaluation of either predicate are relayed to the caller; if
     * evaluation of this predicate throws an exception, the {@code other} predicate will not be
     * evaluated.
     *
     * @param other
     *            a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate that represents the short-circuiting logical AND of this
     *         predicate and the {@code other} predicate
     * @throws NullPointerException
     *             if other is null
     */
    @Override
    public default SerializablePredicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) && other.test(t);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return a predicate that represents the logical negation of this predicate
     */
    @Override
    public default SerializablePredicate<T> negate() {
        return t -> !test(t);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate
     * and another. When evaluating the composed predicate, if this predicate is {@code true}, then
     * the {@code other} predicate is not evaluated.
     * <p>
     * Any exceptions thrown during evaluation of either predicate are relayed to the caller; if
     * evaluation of this predicate throws an exception, the {@code other} predicate will not be
     * evaluated.
     *
     * @param other
     *            a predicate that will be logically-ORed with this predicate
     * @return a composed predicate that represents the short-circuiting logical OR of this
     *         predicate and the {@code other} predicate
     * @throws NullPointerException
     *             if other is null
     */
    @Override
    public default SerializablePredicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) || other.test(t);
    }
}
