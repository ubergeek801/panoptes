package org.slaq.slaqworx.dag;

import com.hazelcast.jet.core.ProcessorMetaSupplier;

public record TypedSource<T>(ProcessorMetaSupplier processorMetaSupplier)
    implements TypedStream<T> {
  // trivial
}
