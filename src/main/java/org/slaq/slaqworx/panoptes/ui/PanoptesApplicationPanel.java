package org.slaq.slaqworx.panoptes.ui;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import com.hazelcast.core.IMap;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import org.slaq.slaqworx.panoptes.Panoptes;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;

@Route("")
@Push
@Theme(value = Lumo.class, variant = Lumo.DARK)
public class PanoptesApplicationPanel extends AppLayout {
    private static final long serialVersionUID = 1L;

    public PanoptesApplicationPanel() {
        Icon applicationIcon = new Icon(VaadinIcon.EYE);
        applicationIcon.getStyle().set("position", "relative").set("top", "-0.05em");
        Span applicationTitle = new Span("Panoptes");
        applicationTitle.getStyle().set("font-weight", "bold").set("font-size", "120%")
                .set("padding-left", "0.3em").set("padding-right", "0.3em");
        Span applicationTagline = new Span("watchful portfolio compliance");
        applicationTagline.getStyle().set("font-style", "italic").set("font-size", "80%");
        Span applicationInfo = new Span();
        applicationInfo.getStyle().set("vertical-align", "baseline");
        applicationInfo.add(applicationTitle, applicationTagline);

        addToNavbar(new DrawerToggle(), applicationIcon, applicationInfo);

        Tabs tabs = new Tabs(new Tab("Home"), new Tab("About"));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        addToDrawer(tabs);

        VerticalLayout mainLayout = new VerticalLayout();
        setContent(mainLayout);
        mainLayout.setSizeFull();

        FixedIncomeTradePanel tradePanel = new FixedIncomeTradePanel();
        Details tradePanelDetail = new Details("Trade Fixed Income", tradePanel);
        tradePanelDetail.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED,
                DetailsVariant.SMALL);
        mainLayout.add(tradePanelDetail);

        SecurityFilterPanel securityFilter = new SecurityFilterPanel();
        Details securityFilterDetail = new Details("Security Filter", securityFilter);
        securityFilterDetail.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED,
                DetailsVariant.SMALL);
        mainLayout.add(securityFilterDetail);

        AssetCache assetCache = Panoptes.getApplicationContext().getBean(AssetCache.class);
        IMap<SecurityKey, Security> securityCache = assetCache.getSecurityCache();

        // unfortunately there's not a very good way to page through entries of an IMap
        ArrayList<Security> securityList = new ArrayList<>(securityCache.values());
        Collections.sort(securityList, (s1, s2) -> s1.getKey().compareTo(s2.getKey()));

        CallbackDataProvider<Security, Void> securityProvider =
                DataProvider
                        .fromCallbacks(
                                query -> securityList.subList(query.getOffset(),
                                        Math.min(query.getOffset() + query.getLimit(),
                                                securityList.size()))
                                        .stream(),
                                query -> securityList.size());

        Grid<Security> securityGrid = new Grid<>();
        securityGrid.setColumnReorderingAllowed(true);
        securityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_COMPACT);
        securityGrid.setDataProvider(securityProvider);

        securityGrid.addColumn(s -> s.getKey().getId()).setAutoWidth(true).setFrozen(true)
                .setHeader("Asset ID");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.cusip)).setAutoWidth(true)
                .setHeader("CUSIP");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.description))
                .setAutoWidth(true).setHeader("Description");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.country))
                .setAutoWidth(true).setHeader("Country");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.region))
                .setAutoWidth(true).setHeader("Region");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.sector))
                .setAutoWidth(true).setHeader("Sector");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.currency))
                .setAutoWidth(true).setHeader("Currency");
        securityGrid
                .addColumn(new NumberRenderer<>(s -> s.getAttributeValue(SecurityAttribute.coupon),
                        "%(,.3f"))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setHeader("Coupon");
        securityGrid
                .addColumn(new LocalDateRenderer<>(
                        s -> s.getAttributeValue(SecurityAttribute.maturityDate),
                        DateTimeFormatter.ISO_LOCAL_DATE))
                .setAutoWidth(true).setHeader("Maturity Date");
        securityGrid
                .addColumn(s -> s.getAttributeValue(SecurityAttribute.ratingSymbol) + " ["
                        + String.format("%(,.4f",
                                s.getAttributeValue(SecurityAttribute.ratingValue))
                        + "]")
                .setAutoWidth(true).setHeader("Rating");
        securityGrid
                .addColumn(new NumberRenderer<>(s -> s.getAttributeValue(SecurityAttribute.yield),
                        "%(,.2f"))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setHeader("Yield");
        securityGrid
                .addColumn(new NumberRenderer<>(
                        s -> s.getAttributeValue(SecurityAttribute.duration), "%(,.3f"))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setHeader("Duration");
        securityGrid.addColumn(s -> s.getAttributeValue(SecurityAttribute.issuer))
                .setAutoWidth(true).setHeader("Issuer");
        securityGrid
                .addColumn(new NumberRenderer<>(s -> s.getAttributeValue(SecurityAttribute.price),
                        "$%(,.4f", Locale.US))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setHeader("Price");

        mainLayout.add(securityGrid);

        IMap<PortfolioKey, Portfolio> portfolioCache = assetCache.getPortfolioCache();

        // unfortunately there's not a very good way to page through entries of an IMap
        ArrayList<Portfolio> portfolioList = new ArrayList<>(portfolioCache.values());
        Collections.sort(portfolioList, (p1, p2) -> p1.getName().compareTo(p2.getName()));

        CallbackDataProvider<Portfolio, Void> portfolioProvider =
                DataProvider.fromCallbacks(
                        query -> portfolioList.subList(query.getOffset(),
                                Math.min(query.getOffset() + query.getLimit(),
                                        portfolioList.size()))
                                .stream(),
                        query -> portfolioList.size());

        Grid<Portfolio> portfolioGrid = new Grid<>();
        portfolioGrid.setColumnReorderingAllowed(true);
        portfolioGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        portfolioGrid.setDataProvider(portfolioProvider);

        portfolioGrid.addColumn(p -> p.getKey().getId()).setAutoWidth(true).setFrozen(true)
                .setHeader("ID");
        portfolioGrid.addColumn(p -> p.getName()).setAutoWidth(true).setHeader("Name");
        portfolioGrid
                .addColumn(new NumberRenderer<>(p -> p.getTotalMarketValue(), "$%(,.2f", Locale.US))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setHeader("Market Value");
        portfolioGrid.addColumn(p -> p.getBenchmarkKey()).setAutoWidth(true).setHeader("Benchmark");

        mainLayout.add(portfolioGrid);
    }
}
