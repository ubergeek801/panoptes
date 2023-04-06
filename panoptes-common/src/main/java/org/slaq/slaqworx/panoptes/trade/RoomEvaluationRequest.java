package org.slaq.slaqworx.panoptes.trade;

import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.concurrent.Callable;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.ApplicationContextAware;

/**
 * A {@link Callable} which facilitates clustered room-in-name evaluation by serializing the
 * evaluation parameters for execution on a remote cluster node.
 *
 * @author jeremy
 */
public class RoomEvaluationRequest
    implements Callable<Double>, ApplicationContextAware, ProtobufSerializable {
  private final PortfolioKey portfolioKey;
  private final SecurityKey securityKey;
  private final double targetValue;

  private ApplicationContext applicationContext;

  /**
   * Creates a new {@link RoomEvaluationRequest} with the given parameters.
   *
   * @param portfolioKey the {@link PortfolioKey} identifying the {@link Portfolio} in which room is
   *     to be found
   * @param securityKey the {@link SecurityKey} identifying the {@link Security} for which to find
   *     room in the specified {@link Portfolio}
   * @param targetValue the target (maximum) amount of room to find
   */
  public RoomEvaluationRequest(
      PortfolioKey portfolioKey, SecurityKey securityKey, double targetValue) {
    this.portfolioKey = portfolioKey;
    this.securityKey = securityKey;
    this.targetValue = targetValue;
  }

  @Override
  public Double call() throws Exception {
    // note that this code executes on the server side; thus it needs to bootstrap the resources
    // it needs (namely the AssetCache and a local PortfolioEvaluator)

    TradeEvaluator evaluator =
        applicationContext.getBean(TradeEvaluator.class, Qualifiers.byName("local"));
    return evaluator.evaluateRoom(portfolioKey, securityKey, targetValue).join();
  }

  /**
   * Obtains the {@link PortfolioKey} identifying the {@link Portfolio} for which to calculate room.
   *
   * @return a {@link PortfolioKey}
   */
  public PortfolioKey getPortfolioKey() {
    return portfolioKey;
  }

  /**
   * Obtains the {@link SecurityKey} identifying the {@link Security} for which to calculate room.
   *
   * @return a {@link SecurityKey}
   */
  public SecurityKey getSecurityKey() {
    return securityKey;
  }

  /**
   * Obtains the target (maximum) amount of room to seek in the specified {@link Portfolio}.
   *
   * @return the target value
   */
  public double getTargetValue() {
    return targetValue;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
}
