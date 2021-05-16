package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.core.AbstractProcessor;
import java.util.ArrayList;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;

public class TestPortfolioSourceP extends AbstractProcessor {
  private final Traverser<PortfolioKey> traverser;

  public TestPortfolioSourceP() {
    ArrayList<PortfolioKey> portfolioKeys = new ArrayList<>();
    for (int i = 1; i <= 600; i++) {
      portfolioKeys.add(new PortfolioKey("portfolio" + i, 1));
    }
    traverser = Traversers.traverseIterable(portfolioKeys);
  }

  @Override
  public boolean complete() {
    return emitFromTraverser(traverser);
  }
}
