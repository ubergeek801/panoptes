package org.slaq.slaqworx.panoptes.util;

import java.util.concurrent.CompletableFuture;

import com.hazelcast.core.ExecutionCallback;

/**
 * An adapter that exposes a result made available through a Hazelcast {@code ExecutionCallback} as
 * a {@code CompletableFuture}.
 *
 * @author jeremy
 * @param <T>
 *            the type of value being supplied
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
