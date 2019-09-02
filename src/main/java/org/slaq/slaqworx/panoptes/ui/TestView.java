package org.slaq.slaqworx.panoptes.ui;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import com.hazelcast.core.IMap;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
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
public class TestView extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    public TestView() {
        setSizeFull();

        Grid<Security> securityGrid = new Grid<>();
        AssetCache assetCache = Panoptes.getApplicationContext().getBean(AssetCache.class);
        IMap<SecurityKey, Security> securityCache = assetCache.getSecurityCache();

        // unfortunately there's not a very good way to page through entries of an IMap
        ArrayList<Security> securityList = new ArrayList<>(securityCache.values());
        Collections.sort(securityList, (s1, s2) -> s1.getAssetId().compareTo(s2.getAssetId()));

        CallbackDataProvider<Security, Void> securityProvider =
                DataProvider
                        .fromCallbacks(
                                query -> securityList.subList(query.getOffset(),
                                        Math.min(query.getOffset() + query.getLimit(),
                                                securityList.size()))
                                        .stream(),
                                query -> securityList.size());
        securityGrid.setDataProvider(securityProvider);

        securityGrid.addColumn(Security::getAssetId).setAutoWidth(true).setFrozen(true)
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

        securityGrid.setColumnReorderingAllowed(true);
        securityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_ROW_BORDERS);

        add(securityGrid);

        Grid<Portfolio> portfolioGrid = new Grid<>();
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
        portfolioGrid.setDataProvider(portfolioProvider);

        portfolioGrid.addColumn(Portfolio::getName).setAutoWidth(true).setHeader("Name");
        portfolioGrid
                .addColumn(
                        new NumberRenderer<>(Portfolio::getTotalMarketValue, "$%(,.2f", Locale.US))
                .setAutoWidth(true).setTextAlign(ColumnTextAlign.END).setHeader("Market Value");
        portfolioGrid.addColumn(Portfolio::getBenchmarkKey).setAutoWidth(true)
                .setHeader("Benchmark");

        portfolioGrid.setColumnReorderingAllowed(true);
        portfolioGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_ROW_BORDERS);

        add(portfolioGrid);
    }
}
