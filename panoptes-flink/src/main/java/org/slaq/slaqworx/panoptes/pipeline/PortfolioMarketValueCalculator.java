package org.slaq.slaqworx.panoptes.pipeline;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.asset.Position;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;

public class PortfolioMarketValueCalculator
        extends KeyedBroadcastProcessFunction<PortfolioKey, Portfolio, Security, PortfolioSummary> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(PortfolioMarketValueCalculator.class);

    private static final TypeInformation<Portfolio> PORTFOLIO_TYPE_INFO =
            TypeInformation.of(new TypeHint<Portfolio>() {
                // trivial
            });
    private static final TypeInformation<Boolean> BOOLEAN_TYPE_INFO =
            TypeInformation.of(new TypeHint<Boolean>() {
                // trivial
            });

    private static final ValueStateDescriptor<Portfolio> PORTFOLIO_STATE_DESCRIPTOR =
            new ValueStateDescriptor<>("portfolio", PORTFOLIO_TYPE_INFO);
    // we don't really need the value here, but there is no such thing as a SetState
    private static final MapStateDescriptor<SecurityKey, Boolean> HELD_SECURITY_STATE_DESCRIPTOR =
            new MapStateDescriptor<>("heldSecurity", PanoptesPipeline.SECURITY_KEY_TYPE_INFO,
                    BOOLEAN_TYPE_INFO);

    private ValueState<Portfolio> portfolioState;
    private MapState<SecurityKey, Boolean> heldSecurityState;

    @Override
    public void open(Configuration config) throws Exception {
        portfolioState = getRuntimeContext().getState(PORTFOLIO_STATE_DESCRIPTOR);
        heldSecurityState = getRuntimeContext().getMapState(HELD_SECURITY_STATE_DESCRIPTOR);
    }

    @Override
    public void processBroadcastElement(Security security,
            KeyedBroadcastProcessFunction<PortfolioKey, Portfolio, Security,
                    PortfolioSummary>.Context context,
            Collector<PortfolioSummary> out) throws Exception {
        BroadcastState<SecurityKey, Security> securityState =
                context.getBroadcastState(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR);
        securityState.put(security.getKey(), security);

        HashSet<PortfolioKey> completedPortfolios = new HashSet<>();
        context.applyToKeyedState(HELD_SECURITY_STATE_DESCRIPTOR,
                (portfolioKey, outstandingSecurities) -> {
                    // remove the security key from the set of remaining keys
                    boolean wasEmpty = outstandingSecurities.isEmpty();
                    if (!wasEmpty) {
                        outstandingSecurities.remove(security.getKey());
                        if (outstandingSecurities.isEmpty()) {
                            // we now have all the information we need for this portfolio; mark it
                            // as complete
                            completedPortfolios.add(portfolioKey);
                        }
                    }
                });

        context.applyToKeyedState(PORTFOLIO_STATE_DESCRIPTOR, (portfolioKey,
                state) -> emitPortfolio(out, state.value(), securityState, completedPortfolios));
    }

    @Override
    public void processElement(Portfolio portfolio,
            KeyedBroadcastProcessFunction<PortfolioKey, Portfolio, Security,
                    PortfolioSummary>.ReadOnlyContext context,
            Collector<PortfolioSummary> out) throws Exception {
        LOG.info("processing portfolio {} (\"{}\")", portfolio.getKey(), portfolio.getName());
        portfolioState.update(portfolio);
        ReadOnlyBroadcastState<SecurityKey, Security> securityState =
                context.getBroadcastState(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR);

        // determine whether we have all held securities for the portfolio
        Map<SecurityKey, Boolean> unmatchedSecurities = portfolio.getPositions()
                .map(Position::getSecurityKey).collect(Collectors.toMap(k -> k, k -> true));
        securityState.immutableEntries().forEach(e -> unmatchedSecurities.remove(e.getKey()));
        if (unmatchedSecurities.isEmpty()) {
            // we've already encountered all the securities we need
            emitPortfolio(out, portfolio, securityState, null);
        } else {
            // haven't seen everything yet; remember what we still need
            heldSecurityState.putAll(unmatchedSecurities);
        }
    }

    /**
     * Emits Portfolio information iff security data is complete.
     * <p>
     * FIXME handle subsequent security updates after completeness is achieved
     *
     * @param out
     *            the {@code Collector} to which to output portfolio information encountered
     * @param portfolio
     *            the portfolio being processed, or {@code null} if it has not been encountered yet
     * @param securityState
     *            the security information currently held in broadcast state
     * @param completedPortfolios
     *            the portfolios previously determined to have all securities accounted for, or
     *            {@code null} to bypass checking
     */
    protected void emitPortfolio(Collector<PortfolioSummary> out, Portfolio portfolio,
            ReadOnlyBroadcastState<SecurityKey, Security> securityState,
            Set<PortfolioKey> completedPortfolios) {
        if (completedPortfolios != null && !completedPortfolios.contains(portfolio.getKey())) {
            // this one isn't ready yet
            return;
        }

        out.collect(PortfolioSummary.fromPortfolio(portfolio,
                p -> portfolio.getPositions().collect(Collectors.summingDouble(pos -> {
                    try {
                        return pos.getAmount() * securityState.get(pos.getSecurityKey())
                                .getAttributeValue(SecurityAttribute.price);
                    } catch (Exception e) {
                        // FIXME throw a real exception
                        throw new RuntimeException(
                                "could not calculate market value for " + portfolio.getKey(), e);
                    }
                }))));
    }
}
