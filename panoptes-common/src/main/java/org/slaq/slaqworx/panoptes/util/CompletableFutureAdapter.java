package org.slaq.slaqworx.panoptes.util;

import com.hazelcast.core.ExecutionCallback;
import java.util.concurrent.CompletableFuture;

/**
 * An adapter that exposes a result made available through a Hazelcast {@code ExecutionCallback} as
 * a {@code CompletableFuture}.
 *
 * @param <T>
 *     the type of value being supplied
 *
 * @author jeremy
 */
public class CompletableFutureAdapter<T> extends CompletableFuture<T>
    implements ExecutionCallback<T> {
  /**
   * Creates a new {@code CompletableFutureAdapter} with an empty value; the actual value is
   * supplied by the {@code onResponse()} caller.
   */
  public CompletableFutureAdapter() {
    // nothing to do
  }

  @Override
  public void onFailure(Throwable t) {
    completeExceptionally(t);
  }

  @Override
  public void onResponse(T response) {
    complete(response);
  }
}
