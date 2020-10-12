package org.slaq.slaqworx.panoptes.rule;

public interface BenchmarkComparable {
    /**
     * Obtains this {@code Rule}'s (possibly {@code null}) lower limit.
     *
     * @return the lower limit against which to evaluate
     */
    Double getLowerLimit();

    /**
     * Obtains this {@code Rule}'s (possibly {@code null}) upper limit.
     *
     * @return the upper limit against which to evaluate
     */
    Double getUpperLimit();

    boolean isBenchmarkSupported();
}
