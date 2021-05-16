package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.jet.core.DAG;
import com.hazelcast.jet.core.ProcessorMetaSupplier;
import com.hazelcast.jet.core.processor.DiagnosticProcessors;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slaq.slaqworx.dag.TypedDAG;
import org.slaq.slaqworx.dag.TypedSource;
import org.slaq.slaqworx.dag.TypedStream;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;

@Singleton
public class PanoptesTestPipeline {
  private final DAG dag;

  protected PanoptesTestPipeline(@Named(PanoptesPipelineConfig.SECURITY_SOURCE_PROCESSOR)
      ProcessorMetaSupplier securitySourceP,
      @Named(PanoptesPipelineConfig.BENCHMARK_SOURCE_PROCESSOR)
          ProcessorMetaSupplier benchmarkSourceP,
      @Named(PanoptesPipelineConfig.PORTFOLIO_SOURCE_PROCESSOR)
          ProcessorMetaSupplier portfolioSourceP) {
    TypedDAG dag = new TypedDAG();

    TypedSource<PortfolioEvent> portfolioSource =
        dag.addSource("portfolioSource", portfolioSourceP);

    TypedSource<PortfolioEvent> benchmarkSource =
        dag.addSource("benchmarkSource", benchmarkSourceP);

    TypedStream<PortfolioEvent> portfolioAndBenchmarkStream =
        dag.merge("portfolioBenchmarkJoiner", portfolioSource, benchmarkSource);

    TypedSource<Security> securitySource = dag.addSource("securitySource", securitySourceP);

    TypedStream<PortfolioEvaluationInput> evaluationInput =
        dag.connect("readinessEvaluator", portfolioAndBenchmarkStream, securitySource,
            new ReadinessEvaluator());

    dag.addSink("loggingSink", evaluationInput, DiagnosticProcessors.writeLoggerP());

    this.dag = dag.toDAG();
  }

  /**
   * Obtains the Jet {@link DAG} corresponding to this {@link PanoptesTestPipeline}.
   *
   * @return a Jet {@link DAG}
   */
  public DAG getJetPipeline() {
    return dag;
  }
}
