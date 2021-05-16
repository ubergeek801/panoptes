package org.slaq.slaqworx.dag;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.core.DAG;
import com.hazelcast.jet.core.Edge;
import com.hazelcast.jet.core.ProcessorMetaSupplier;
import com.hazelcast.jet.core.Vertex;
import com.hazelcast.jet.core.processor.Processors;
import java.util.IdentityHashMap;
import javax.annotation.Nonnull;

public class TypedDAG {
  private final IdentityHashMap<TypedVertex<?>, VertexInfo> vertexMap = new IdentityHashMap<>();
  private final DAG dag = new DAG();

  public TypedDAG() {
    // nothing to do
  }

  public DAG toDAG() {
    return dag;
  }

  public <T> TypedSource<T> addSource(String name, ProcessorMetaSupplier source) {
    Vertex vertex = dag.newVertex(name, source);
    vertex.localParallelism(1);

    TypedSource<T> typedSource = new TypedSource<>(source);
    vertexMap.put(typedSource, new VertexInfo(vertex));

    return typedSource;
  }

  public <T> TypedSink<T> addSink(String name, TypedStream<T> input, ProcessorMetaSupplier sink) {
    Vertex vertex = dag.newVertex(name, sink);
    VertexInfo sinkVertex = new VertexInfo(vertex);
    TypedSink<T> typedSink = new TypedSink<>(sink);
    vertexMap.put(typedSink, sinkVertex);

    VertexInfo inputVertex = vertexMap.get(input);
    dag.edge(Edge.from(inputVertex.vertex(), inputVertex.nextOutputOrdinal)
        .to(sinkVertex.vertex(), sinkVertex.nextInputOrdinal()));

    return typedSink;
  }

  public <I0, I1, O> TypedStream<O> connect(@Nonnull String name, @Nonnull TypedStream<I0> input0,
      @Nonnull TypedStream<I1> input1, @Nonnull TypedProcessor<I0, I1, O> coProcessor) {
    Vertex vertex = dag.newVertex(name, coProcessor.getProcessorSupplier());
    VertexInfo coProcessorVertex = new VertexInfo(vertex);
    vertexMap.put(coProcessor, coProcessorVertex);

    VertexInfo inputVertex0 = vertexMap.get(input0);
    Edge edge0 = Edge.from(inputVertex0.vertex(), inputVertex0.nextOutputOrdinal())
        .to(coProcessorVertex.vertex(), coProcessorVertex.nextInputOrdinal());
    coProcessor.configureEdge0(edge0);
    dag.edge(edge0);

    VertexInfo inputVertex1 = vertexMap.get(input1);
    Edge edge1 = Edge.from(inputVertex1.vertex(), inputVertex1.nextOutputOrdinal())
        .to(coProcessorVertex.vertex(), coProcessorVertex.nextInputOrdinal());
    coProcessor.configureEdge1(edge1);
    dag.edge(edge1);

    return coProcessor;
  }

  public <T> TypedStream<T> merge(@Nonnull String name, @Nonnull TypedStream<T> input0,
      @Nonnull TypedStream<T> input1) {
    ProcessorVertex<T> joinProcessor =
        new ProcessorVertex<>(Processors.mapP(FunctionEx.identity()));
    Vertex vertex = dag.newVertex(name, joinProcessor.getProcessorSupplier());
    VertexInfo joinProcessorVertex = new VertexInfo(vertex);
    vertexMap.put(joinProcessor, joinProcessorVertex);

    VertexInfo inputVertex0 = vertexMap.get(input0);
    Edge edge0 = Edge.from(inputVertex0.vertex(), inputVertex0.nextOutputOrdinal())
        .to(joinProcessorVertex.vertex(), joinProcessorVertex.nextInputOrdinal());
    dag.edge(edge0);

    VertexInfo inputVertex1 = vertexMap.get(input1);
    Edge edge1 = Edge.from(inputVertex1.vertex(), inputVertex1.nextOutputOrdinal())
        .to(joinProcessorVertex.vertex(), joinProcessorVertex.nextInputOrdinal());
    dag.edge(edge1);

    return joinProcessor;
  }

  private static class VertexInfo {
    private final Vertex vertex;
    private int nextInputOrdinal;
    private int nextOutputOrdinal;

    public VertexInfo(Vertex vertex) {
      this.vertex = vertex;
    }

    public Vertex vertex() {
      return vertex;
    }

    public int nextInputOrdinal() {
      return nextInputOrdinal++;
    }

    public int nextOutputOrdinal() {
      return nextOutputOrdinal++;
    }
  }
}
