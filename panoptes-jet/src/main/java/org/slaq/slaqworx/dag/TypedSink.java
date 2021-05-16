package org.slaq.slaqworx.dag;

import com.hazelcast.jet.core.ProcessorMetaSupplier;

public record TypedSink<T>(ProcessorMetaSupplier processorMetaSupplier) implements TypedInput<T> {
  // trivial
}
