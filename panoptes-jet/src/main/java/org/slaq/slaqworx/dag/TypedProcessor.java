package org.slaq.slaqworx.dag;

import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.core.AbstractProcessor;
import com.hazelcast.jet.core.Edge;
import com.hazelcast.jet.core.Processor;
import java.io.Serializable;
import javax.annotation.Nonnull;

public abstract class TypedProcessor<I0, I1, O> extends AbstractProcessor
    implements Serializable, TypedStream<O> {
  protected TypedProcessor() {
    // nothing to do
  }

  public @Nonnull
  SupplierEx<? extends Processor> getProcessorSupplier() {
    return (this::newInstance);
  }

  protected Edge configureEdge0(Edge edge) {
    return edge;
  }

  protected Edge configureEdge1(Edge edge) {
    return edge;
  }

  @Override
  protected boolean tryProcess0(@Nonnull Object item) {
    @SuppressWarnings("unchecked") I0 typedItem = (I0) item;
    return process0(typedItem);
  }

  @Override
  protected boolean tryProcess1(@Nonnull Object item) {
    @SuppressWarnings("unchecked") I1 typedItem = (I1) item;
    return process1(typedItem);
  }

  protected abstract @Nonnull
  TypedProcessor<I0, I1, O> newInstance();

  protected abstract boolean process0(@Nonnull I0 item);

  protected abstract boolean process1(@Nonnull I1 item);
}
