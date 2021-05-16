package org.slaq.slaqworx.dag;

import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.core.AbstractProcessor;
import com.hazelcast.jet.core.Processor;
import java.io.Serializable;
import javax.annotation.Nonnull;

public class ProcessorVertex<T> extends AbstractProcessor implements Serializable, TypedStream<T> {
  private final @Nonnull
  SupplierEx<? extends Processor> processorSupplier;

  protected ProcessorVertex(SupplierEx<? extends Processor> processorSupplier) {
    this.processorSupplier = processorSupplier;
  }

  public @Nonnull
  SupplierEx<? extends Processor> getProcessorSupplier() {
    return processorSupplier;
  }
}
