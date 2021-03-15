package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.function.BiConsumerEx;
import com.hazelcast.jet.pipeline.SourceBuilder;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.ringbuffer.ReadResultSet;
import com.hazelcast.ringbuffer.Ringbuffer;
import java.util.concurrent.CompletionStage;

/**
 * Facilitates the construction of {@link StreamSource}s which in turn source data from a Hazelcast
 * {@link Ringbuffer}. Supports fault tolerance (as long as the underlying {@link Ringbuffer}
 * doesn't lose events between job failure and recovery), but currently does not support distributed
 * operation.
 *
 * @author jeremy
 */
public class RingbufferSource {
  /**
   * Creates a {@link StreamSource} with the specified configuration.
   *
   * @param name
   *     the name of this source
   * @param ringbufferName
   *     the name of the {@link Ringbuffer} from which to read
   * @param <T>
   *     the type of item to be read from the {@link Ringbuffer}
   *
   * @return a {@link StreamSource}
   */
  public static <T> StreamSource<T> buildRingbufferSource(String name, String ringbufferName) {
    return SourceBuilder.stream(name, context -> new RingbufferSourceContext<T>(
        context.jetInstance().getHazelcastInstance().getRingbuffer(ringbufferName)))
        .fillBufferFn(new RingbufferBufferFiller<>())
        .createSnapshotFn(RingbufferSourceContext::getCurrentSequence)
        .restoreSnapshotFn((context, saved) -> context.setCurrentSequence(saved.get(0))).build();
  }

  /**
   * A fill-buffer function which fills the given {@link SourceBuilder.SourceBuffer} with data from
   * a {@link Ringbuffer}.
   *
   * @param <T>
   *     the type of data contained in the {@link Ringbuffer}
   */
  private static class RingbufferBufferFiller<T>
      implements BiConsumerEx<RingbufferSourceContext<T>, SourceBuilder.SourceBuffer<T>> {
    @Override
    public void acceptEx(RingbufferSourceContext<T> context,
        SourceBuilder.SourceBuffer<T> sourceBuffer) {
      CompletionStage<ReadResultSet<T>> futureReadResults = context.getRingbuffer()
          .readManyAsync(context.getCurrentSequence(), 0, Integer.MAX_VALUE, null);
      ReadResultSet<T> readResults = futureReadResults.toCompletableFuture().join();
      context.setCurrentSequence(readResults.getNextSequenceToReadFrom());
      readResults.forEach(sourceBuffer::add);
    }
  }

  /**
   * Encapsulates the context needed to operate a {@link RingbufferSource}. Supports fault tolerance
   * by allowing the current ringbuffer sequence to be read and set.
   *
   * @param <T>
   *     the type of data contained in the {@link Ringbuffer}
   */
  private static class RingbufferSourceContext<T> {
    private final Ringbuffer<T> ringbuffer;
    private long currentSequence;

    /**
     * Creates a new {@link RingbufferSourceContext} which reads from the given {@link Ringbuffer}
     * starting at its head.
     *
     * @param ringbuffer
     *     the {@link Ringbuffer} from which to source events
     */
    public RingbufferSourceContext(Ringbuffer<T> ringbuffer) {
      this.ringbuffer = ringbuffer;
      currentSequence = ringbuffer.headSequence();
    }

    public Ringbuffer<T> getRingbuffer() {
      return ringbuffer;
    }

    public long getCurrentSequence() {
      return currentSequence;
    }

    public void setCurrentSequence(long sequence) {
      currentSequence = sequence;
    }
  }
}
