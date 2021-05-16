package org.slaq.slaqworx.panoptes.pipeline;

import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.core.AbstractProcessor;
import java.util.ArrayList;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

public class TestSecuritySourceP extends AbstractProcessor {
  private final Traverser<SecurityKey> traverser;

  public TestSecuritySourceP() {
    ArrayList<SecurityKey> securityKeys = new ArrayList<>();
    for (int i = 1; i <= 14000; i++) {
      securityKeys.add(new SecurityKey("sec" + i));
    }
    traverser = Traversers.traverseIterable(securityKeys);
  }

  @Override
  public boolean complete() {
    return emitFromTraverser(traverser);
  }
}
