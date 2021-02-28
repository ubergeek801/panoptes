package org.slaq.slaqworx.panoptes.rule;

import org.slaq.slaqworx.panoptes.asset.Portfolio;

/**
 * An interface which facilitates the comparison of {@link Rule} results between some target entity
 * (typically a {@link Portfolio} and a benchmark.
 */
public interface BenchmarkComparable {
  /**
   * Obtains the related {@link Rule}'s (possibly {@code null}) lower limit.
   *
   * @return the lower limit against which to evaluate
   */
  public Double getLowerLimit();

  /**
   * Obtains the related {@link Rule}'s (possibly {@code null}) upper limit.
   *
   * @return the upper limit against which to evaluate
   */
  public Double getUpperLimit();

  /**
   * Indicates whether the related {@link Rule} supports a comparison against a benchmark.
   *
   * @return {@code true} if a benchmark comparison is supported, {@code false} otherwise
   */
  public boolean isBenchmarkSupported();
}
