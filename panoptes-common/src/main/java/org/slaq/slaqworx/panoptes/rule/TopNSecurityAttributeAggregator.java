package org.slaq.slaqworx.panoptes.rule;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.PositionSet;
import org.slaq.slaqworx.panoptes.asset.PositionSupplier;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.util.JsonConfigurable;

/**
 * A {@link SecurityAttributeGroupClassifier} which classifies {@link Position}s based on a
 * specified {@link SecurityAttribute}, and also (as a {@link GroupAggregator}) aggregates the "top
 * n" resulting groups into a composite group. The number of classified groups, including this
 * composite group, is limited to n + 1 (and will be smaller if the number of distinct attribute
 * values is smaller than n).
 *
 * @author jeremy
 */
public class TopNSecurityAttributeAggregator extends SecurityAttributeGroupClassifier
    implements GroupAggregator {
  private final int count;

  /**
   * Creates a new {@link TopNSecurityAttributeAggregator} which aggregates {@link Position}s on the
   * given {@link SecurityAttribute}.
   *
   * @param securityAttribute the {@link SecurityAttribute} on which to aggregate {@link Position}s
   * @param count the number of groups to collect into the "top n" metagroup
   */
  public TopNSecurityAttributeAggregator(SecurityAttribute<?> securityAttribute, int count) {
    super(securityAttribute);
    this.count = count;
  }

  /**
   * Creates a new {@link TopNSecurityAttributeAggregator} which aggregates {@link Position}s on the
   * {@link SecurityAttribute} specified in the JSON configuration.
   *
   * @param jsonConfiguration a JSON configuration specifying the {@link SecurityAttribute} on which
   *     to aggregate {@link Position}s
   */
  @Nonnull
  public static TopNSecurityAttributeAggregator fromJson(@Nonnull String jsonConfiguration) {
    Configuration configuration;
    try {
      configuration =
          JsonConfigurable.defaultObjectMapper().readValue(jsonConfiguration, Configuration.class);
    } catch (Exception e) {
      // TODO throw a better exception
      throw new RuntimeException("could not parse JSON configuration " + jsonConfiguration, e);
    }

    return new TopNSecurityAttributeAggregator(
        SecurityAttribute.of(configuration.attribute), configuration.count);
  }

  @Override
  @Nonnull
  public Map<EvaluationGroup, PositionSupplier> aggregate(
      @Nonnull Map<EvaluationGroup, PositionSupplier> classifiedPositions,
      @Nonnull EvaluationContext evaluationContext) {
    if (classifiedPositions.isEmpty()) {
      return Collections.emptyMap();
    }

    HashMap<EvaluationGroup, PositionSupplier> filteredClassifiedPositions =
        new HashMap<>(count + 1);

    ArrayList<Position> aggregatePositions = new ArrayList<>();
    // if we already have fewer groups than the desired, then just collect it all
    if (classifiedPositions.size() <= count) {
      filteredClassifiedPositions.putAll(classifiedPositions);
      classifiedPositions
          .values()
          .forEach(positions -> positions.getPositions().forEach(aggregatePositions::add));
    } else {
      // create a list of PositionSuppliers and sort it by total amount, descending; also
      // build an IdentityHashMap to relate the PositionSuppliers to their EvaluationGroups
      ArrayList<PositionSupplier> sortedClassifiedPositions =
          new ArrayList<>(classifiedPositions.size());
      IdentityHashMap<PositionSupplier, EvaluationGroup> supplierGroupMap =
          new IdentityHashMap<>(classifiedPositions.size());
      classifiedPositions.forEach(
          (g, positions) -> {
            sortedClassifiedPositions.add(positions);
            supplierGroupMap.put(positions, g);
          });
      sortedClassifiedPositions.sort(
          (s1, s2) ->
              Double.compare(
                  evaluationContext.getMarketValue(s2), evaluationContext.getMarketValue(s1)));

      // collect the first "count" PositionSuppliers into a single supplier, and also into the
      // filtered map
      for (int i = 0; i < count; i++) {
        PositionSupplier positionSupplier = sortedClassifiedPositions.get(i);
        aggregatePositions.addAll(positionSupplier.getPositions().collect(Collectors.toList()));
        filteredClassifiedPositions.put(supplierGroupMap.get(positionSupplier), positionSupplier);
      }
    }

    // the Positions are presumed to be from the same Portfolio, so just grab the first
    PositionSupplier aPositionSupplier = classifiedPositions.values().iterator().next();

    filteredClassifiedPositions.put(
        new EvaluationGroup(
            "top(" + count + "," + getSecurityAttribute().getName() + ")",
            getSecurityAttribute().getName()),
        new PositionSet<>(
            aggregatePositions,
            aPositionSupplier.getPortfolioKey(),
            evaluationContext.getMarketValue(aPositionSupplier)));

    return filteredClassifiedPositions;
  }

  @Nonnull
  @Override
  public String getJsonConfiguration() {
    Configuration configuration = new Configuration(getSecurityAttribute().getName(), count);

    try {
      return JsonConfigurable.defaultObjectMapper().writeValueAsString(configuration);
    } catch (JsonProcessingException e) {
      // TODO throw a better exception
      throw new RuntimeException("could not serialize JSON configuration", e);
    }
  }

  /** Mirrors the JSON configuration. */
  static record Configuration(@Nonnull String attribute, int count) {
    // trivial
  }
}
