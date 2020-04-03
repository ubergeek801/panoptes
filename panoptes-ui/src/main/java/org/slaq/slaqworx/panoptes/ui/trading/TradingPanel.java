package org.slaq.slaqworx.panoptes.ui.trading;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.hazelcast.map.IMap;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;

import org.slaq.slaqworx.panoptes.ApplicationContextProvider;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.cache.PortfolioSummarizer;
import org.slaq.slaqworx.panoptes.util.FakeSet;

/**
 * {@code TradingPanel} is a container providing tools for entering trade information.
 *
 * @author jeremy
 */
public class TradingPanel extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@code TradingPanel}.
     */
    public TradingPanel() {
        FixedIncomeTradePanel tradePanel = new FixedIncomeTradePanel();
        Details tradePanelDetail = new Details("Trade Fixed Income", tradePanel);
        tradePanelDetail.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED,
                DetailsVariant.SMALL);
        add(tradePanelDetail);

        AssetCache assetCache =
                ApplicationContextProvider.getApplicationContext().getBean(AssetCache.class);
        SecurityDataProvider securityProvider = new SecurityDataProvider(assetCache);

        SecurityFilterPanel securityFilter = new SecurityFilterPanel(securityProvider, assetCache);
        Details securityFilterDetail = new Details("Security Filter", securityFilter);
        securityFilterDetail.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED,
                DetailsVariant.SMALL);
        add(securityFilterDetail);

        Grid<Security> securityGrid = new Grid<>();
        securityGrid.setColumnReorderingAllowed(true);
        securityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_COMPACT);
        securityGrid.setDataProvider(securityProvider);

        securityGrid.addColumn(s -> s.getKey().getId()).setAutoWidth(true).setFrozen(true)
                .setHeader("Asset ID");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.cusip, false))
                .setAutoWidth(true).setHeader("CUSIP");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.description, false))
                .setAutoWidth(true).setHeader("Description");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.country, false))
                .setAutoWidth(true).setHeader("Country");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.region, false))
                .setAutoWidth(true).setHeader("Region");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.sector, false))
                .setAutoWidth(true).setHeader("Sector");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.currency, false))
                .setAutoWidth(true).setHeader("Currency");
        securityGrid
                .addColumn(new NumberRenderer<>(
                        s -> s.getAttributeValue(SecurityAttribute.coupon, false), "%(,.3f"))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setHeader("Coupon");
        securityGrid
                .addColumn(new LocalDateRenderer<>(
                        s -> s.getAttributeValue(SecurityAttribute.maturityDate, false),
                        DateTimeFormatter.ISO_LOCAL_DATE))
                .setAutoWidth(true).setHeader("Maturity Date");
        securityGrid.addColumn(s -> getRatingText(s, SecurityAttribute.rating1Symbol,
                SecurityAttribute.rating1Value)).setAutoWidth(true).setHeader("Rating 1");
        securityGrid.addColumn(s -> getRatingText(s, SecurityAttribute.rating2Symbol,
                SecurityAttribute.rating2Value)).setAutoWidth(true).setHeader("Rating 2");
        securityGrid.addColumn(s -> getRatingText(s, SecurityAttribute.rating3Symbol,
                SecurityAttribute.rating3Value)).setAutoWidth(true).setHeader("Rating 3");
        securityGrid
                .addColumn(new NumberRenderer<>(
                        s -> s.getAttributeValue(SecurityAttribute.yield, false), "%(,.2f"))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setHeader("Yield");
        securityGrid
                .addColumn(new NumberRenderer<>(
                        s -> s.getAttributeValue(SecurityAttribute.duration, false), "%(,.3f"))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setHeader("Duration");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.issuer, false))
                .setAutoWidth(true).setHeader("Issuer");
        securityGrid
                .addColumn(new NumberRenderer<>(
                        s -> s.getAttributeValue(SecurityAttribute.price, false), "$%(,.4f",
                        Locale.US))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setHeader("Price");

        add(securityGrid);

        IMap<PortfolioKey, Portfolio> portfolioCache = assetCache.getPortfolioCache();

        // TODO make portfolios sortable
        List<PortfolioKey> portfolioKeys = new ArrayList<>(portfolioCache.keySet());
        Collections.sort(portfolioKeys, PortfolioKey::compareTo);

        CallbackDataProvider<PortfolioSummary,
                Void> portfolioProvider =
                        DataProvider
                                .fromCallbacks(
                                        query -> portfolioCache.executeOnKeys(
                                                new FakeSet<>(
                                                        portfolioKeys.subList(query.getOffset(),
                                                                Math.min(
                                                                        query.getOffset()
                                                                                + query.getLimit(),
                                                                        portfolioKeys.size()))),
                                                new PortfolioSummarizer()).values().stream(),
                                        query -> portfolioKeys.size());

        Grid<PortfolioSummary> portfolioGrid = new Grid<>();
        portfolioGrid.setColumnReorderingAllowed(true);
        portfolioGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        portfolioGrid.setDataProvider(portfolioProvider);

        portfolioGrid.addColumn(p -> p.getKey().getId()).setAutoWidth(true).setFrozen(true)
                .setHeader("ID");
        portfolioGrid.addColumn(PortfolioSummary::getName).setAutoWidth(true).setHeader("Name");
        portfolioGrid
                .addColumn(new NumberRenderer<>(PortfolioSummary::getTotalMarketValue, "$%(,.2f",
                        Locale.US))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setHeader("Market Value");
        portfolioGrid.addColumn(PortfolioSummary::getBenchmarkKey).setAutoWidth(true)
                .setHeader("Benchmark");

        add(portfolioGrid);
    }

    /**
     * Formats the given rating information for table display.
     *
     * @param security
     *            the {@code Security} from which to obtain rating information
     * @param symbolAttribute
     *            the {@code SecurityAttribute} corresponding to the desired rating symbol
     * @param valueAttribute
     *            the {@code SecurityAttribute} corresponding to the desired rating value
     * @return a {@code String} representing the specified rating data, or {@code null} if the value
     *         of the specified symbol attribute is {@code null}
     */
    protected String getRatingText(Security security, SecurityAttribute<String> symbolAttribute,
            SecurityAttribute<Double> valueAttribute) {
        String symbol = security.getAttributeValue(symbolAttribute, false);
        return (symbol == null ? null
                : symbol + " ["
                        + String.format("%(,.4f", security.getAttributeValue(valueAttribute))
                        + "]");
    }
}
