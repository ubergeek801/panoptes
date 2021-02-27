package org.slaq.slaqworx.panoptes.event;

import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.serializer.ProtobufSerializable;
import org.slaq.slaqworx.panoptes.util.Keyed;

/**
 * The common interface of events which occur on portfolios; such events may include updating
 * portfolio data, requesting portfolio compliance evaluation, etc.
 *
 * @author jeremy
 */
public interface PortfolioEvent extends Keyed<PortfolioKey>, ProtobufSerializable {
  /**
   * Obtains the key, if applicable, of the benchmark associated with the target portfolio.
   * <p>
   * FIXME it's not clear that this belongs at the PortfolioEvent level
   *
   * @return a key identifying the portfolio's benchmark, or {@code null} if there is no
   * associated
   *     benchmark
   */
  PortfolioKey getBenchmarkKey();

  /**
   * Synonymous with {@code getKey()}. Exists because Flink can't figure out {@code Keyed<T>} even
   * when given type information.
   *
   * @return the event/portfolio key
   */
  default PortfolioKey getPortfolioKey() {
    return getKey();
  }
}
