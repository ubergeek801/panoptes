package org.slaq.slaqworx.panoptes.pipeline;

import java.util.stream.Collectors;

import org.apache.flink.api.common.state.BroadcastState;
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

    private static final ValueStateDescriptor<Portfolio> PORTFOLIO_STATE_DESCRIPTOR =
            new ValueStateDescriptor<>("portfolio", PORTFOLIO_TYPE_INFO);

    private transient ValueState<Portfolio> portfolioState;

    @Override
    public void open(Configuration config) throws Exception {
        portfolioState = getRuntimeContext().getState(PORTFOLIO_STATE_DESCRIPTOR);
    }

    @Override
    public void processBroadcastElement(Security security,
            KeyedBroadcastProcessFunction<PortfolioKey, Portfolio, Security,
                    PortfolioSummary>.Context context,
            Collector<PortfolioSummary> out) throws Exception {
        BroadcastState<SecurityKey, Security> securityState =
                context.getBroadcastState(PanoptesPipeline.SECURITY_STATE_DESCRIPTOR);
        securityState.put(security.getKey(), security);

        context.applyToKeyedState(PORTFOLIO_STATE_DESCRIPTOR,
                (portfolioKey, state) -> emitPortfolio(out, state.value(), securityState));
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

        emitPortfolio(out, portfolio, securityState);
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
     */
    protected void emitPortfolio(Collector<PortfolioSummary> out, Portfolio portfolio,
            ReadOnlyBroadcastState<SecurityKey, Security> securityState) {
        // determine whether we have all held securities for the portfolio
        boolean isComplete = portfolio.getPositions().allMatch(p -> {
            try {
                return securityState.contains(p.getSecurityKey());
            } catch (Exception e) {
                // FIXME throw a real exception
                throw new RuntimeException(
                        "could not determine completeness for " + portfolio.getKey(), e);
            }
        });
        if (!isComplete) {
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
