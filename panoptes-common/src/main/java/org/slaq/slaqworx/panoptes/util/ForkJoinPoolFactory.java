package org.slaq.slaqworx.panoptes.util;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * Provides {@code ForkJoinPool} instances with particular characteristics.
 *
 * @author jeremy
 */
public class ForkJoinPoolFactory {
  /**
   * Creates a new {@code ForkJoinPool} which creates worker threads with a specified prefix name.
   *
   * @param parallelism
   *     the parallelism level. For default value, use
   *     {@link java.lang.Runtime#availableProcessors}.
   * @param prefixName
   *     the name with which to prefix worker thread names
   *
   * @return a {@code ForkJoinPool}
   */
  public static ForkJoinPool newForkJoinPool(int parallelism, String prefixName) {
    return new ForkJoinPool(parallelism, pool -> {
      ForkJoinWorkerThread worker =
          ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
      worker.setName(prefixName + "-" + worker.getPoolIndex());

      return worker;
    }, null, true);
  }

  /**
   * Creates a new {@code ForkJoinPoolFactory}. Restricted to enforce class utility semantics.
   */
  private ForkJoinPoolFactory() {
    // nothing to do
  }
}
