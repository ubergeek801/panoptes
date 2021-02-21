package org.slaq.slaqworx.panoptes.pipeline;

import java.util.ArrayList;
import java.util.Collection;

import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.function.TriFunction;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;

import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.event.HeldSecurityEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioCommandEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioDataEvent;
import org.slaq.slaqworx.panoptes.event.PortfolioEvent;
import org.slaq.slaqworx.panoptes.event.RuleEvaluationResult;
import org.slaq.slaqworx.panoptes.event.TransactionEvent;
import org.slaq.slaqworx.panoptes.proto.PanoptesSerialization.RuleEvaluationResultMsg.EvaluationSource;

/**
 * A process function which collects security and portfolio position data and evaluates portfolio
 * compliance using the portfolio-supplied rules.
 *
 * @author jeremy
 */
public class PortfolioRuleEvaluator implements SupplierEx<PortfolioTracker>, TriFunction<
        PortfolioTracker, PortfolioKey, PortfolioEvent, Traverser<RuleEvaluationResult>> {
    private static final long serialVersionUID = 1L;

    private transient PortfolioTracker portfolioTracker;

    /**
     * Creates a new {@code PortfolioRuleEvaluator}.
     */
    public PortfolioRuleEvaluator() {
        // nothing to do
    }

    @Override
    public Traverser<RuleEvaluationResult> applyEx(PortfolioTracker processState,
            PortfolioKey eventKey, PortfolioEvent event) {
        portfolioTracker = processState;

        ArrayList<RuleEvaluationResult> results = new ArrayList<>();
        if (event instanceof HeldSecurityEvent) {
            SecurityKey securityKey = ((HeldSecurityEvent)event).getSecurityKey();
            IMap<SecurityKey, Security> securityMap =
                    PanoptesApp.getAssetCache().getSecurityCache();
            Security security = securityMap.get(securityKey);
            processSecurity(security, results);
        } else {
            processPortfolioEvent(event, results);
        }

        return Traversers.traverseIterable(results);
    }

    @Override
    public PortfolioTracker getEx() {
        return new PortfolioTracker(EvaluationSource.PORTFOLIO);
    }

    protected void processPortfolioEvent(PortfolioEvent portfolioEvent,
            Collection<RuleEvaluationResult> results) {
        boolean isPortfolioProcessable;
        Portfolio portfolio;
        if (portfolioEvent instanceof PortfolioCommandEvent) {
            portfolio = portfolioTracker.getPortfolio();
            // process only if the command refers to the keyed portfolio specifically
            isPortfolioProcessable = (portfolio != null
                    && portfolio.getPortfolioKey().equals(portfolioEvent.getPortfolioKey()));
        } else if (portfolioEvent instanceof PortfolioDataEvent) {
            portfolio = ((PortfolioDataEvent)portfolioEvent).getPortfolio();
            // we shouldn't be seeing benchmarks, but ignore them if we do
            if (!portfolio.isAbstract()) {
                MultiMap<SecurityKey, PortfolioKey> heldSecuritiesMap =
                        PanoptesApp.getAssetCache().getHeldSecuritiesCache();
                portfolioTracker.trackPortfolio(portfolio, heldSecuritiesMap);
                isPortfolioProcessable = true;
            } else {
                isPortfolioProcessable = false;
            }
        } else if (portfolioEvent instanceof TransactionEvent) {
            // FIXME implement; right now just process the portfolio
            portfolio = portfolioTracker.getPortfolio();
            isPortfolioProcessable = true;
        } else {
            // this shouldn't be possible since only the above types of PortfolioEvents exist
            throw new IllegalArgumentException("don't know how to process PortfolioEvent of type "
                    + portfolioEvent.getClass());
        }

        if (isPortfolioProcessable && portfolio != null) {
            IMap<SecurityKey, Security> securityMap =
                    PanoptesApp.getAssetCache().getSecurityCache();
            portfolioTracker.processPortfolio(results, portfolio, null, securityMap,
                    portfolio.getRules()::iterator);
        }
    }

    protected void processSecurity(Security security, Collection<RuleEvaluationResult> results) {
        portfolioTracker.applySecurity(security, (p -> p.getRules()::iterator), results);
    }
}
