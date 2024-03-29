package org.slaq.slaqworx.panoptes.ui.trading;

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
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.PortfolioSummary;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.cache.PortfolioSummarizer;
import org.slaq.slaqworx.panoptes.rule.EvaluationContext;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluator;
import org.slaq.slaqworx.panoptes.util.FakeSet;

/**
 * A container providing tools for entering trade information.
 *
 * @author jeremy
 */
public class TradingPanel extends VerticalLayout {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@link TradingPanel}.
   *
   * @param tradeEvaluator the {@link TradeEvaluator} to use to perform compliance evaluation
   * @param assetCache the {@link AssetCache} to use to resolve cached entities
   */
  public TradingPanel(TradeEvaluator tradeEvaluator, AssetCache assetCache) {
    FixedIncomeTradePanel tradePanel = new FixedIncomeTradePanel(tradeEvaluator, assetCache);
    Details tradePanelDetail = new Details("Trade Fixed Income", tradePanel);
    tradePanelDetail.addThemeVariants(
        DetailsVariant.REVERSE, DetailsVariant.FILLED, DetailsVariant.SMALL);
    add(tradePanelDetail);

    SecurityDataProvider securityProvider = new SecurityDataProvider(assetCache);

    SecurityFilterPanel securityFilter = new SecurityFilterPanel(securityProvider, assetCache);
    Details securityFilterDetail = new Details("Security Filter", securityFilter);
    securityFilterDetail.addThemeVariants(
        DetailsVariant.REVERSE, DetailsVariant.FILLED, DetailsVariant.SMALL);
    add(securityFilterDetail);

    Grid<Security> securityGrid = new Grid<>();
    securityGrid.setColumnReorderingAllowed(true);
    securityGrid.addThemeVariants(
        GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
    securityGrid.setItems(securityProvider);

    securityGrid
        .addColumn(s -> s.getKey().id())
        .setAutoWidth(true)
        .setFrozen(true)
        .setHeader("Asset ID");
    securityGrid
        .addColumn(s -> s.getAttributeValue(SecurityAttribute.cusip, false))
        .setAutoWidth(true)
        .setHeader("CUSIP");
    securityGrid
        .addColumn(s -> s.getAttributeValue(SecurityAttribute.description, false))
        .setAutoWidth(true)
        .setHeader("Description");
    securityGrid
        .addColumn(s -> s.getAttributeValue(SecurityAttribute.country, false))
        .setAutoWidth(true)
        .setHeader("Country");
    securityGrid
        .addColumn(s -> s.getAttributeValue(SecurityAttribute.region, false))
        .setAutoWidth(true)
        .setHeader("Region");
    securityGrid
        .addColumn(s -> s.getAttributeValue(SecurityAttribute.sector, false))
        .setAutoWidth(true)
        .setHeader("Sector");
    securityGrid
        .addColumn(s -> s.getAttributeValue(SecurityAttribute.currency, false))
        .setAutoWidth(true)
        .setHeader("Currency");
    securityGrid
        .addColumn(
            new NumberRenderer<>(
                s -> s.getAttributeValue(SecurityAttribute.coupon, false), "%(,.3f"))
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.END)
        .setHeader("Coupon");
    securityGrid
        .addColumn(
            new LocalDateRenderer<>(
                s -> s.getAttributeValue(SecurityAttribute.maturityDate, false)))
        .setAutoWidth(true)
        .setHeader("Maturity Date");
    securityGrid
        .addColumn(
            s -> getRatingText(s, SecurityAttribute.rating1Symbol, SecurityAttribute.rating1Value))
        .setAutoWidth(true)
        .setHeader("Rating 1");
    securityGrid
        .addColumn(
            s -> getRatingText(s, SecurityAttribute.rating2Symbol, SecurityAttribute.rating2Value))
        .setAutoWidth(true)
        .setHeader("Rating 2");
    securityGrid
        .addColumn(
            s -> getRatingText(s, SecurityAttribute.rating3Symbol, SecurityAttribute.rating3Value))
        .setAutoWidth(true)
        .setHeader("Rating 3");
    securityGrid
        .addColumn(
            new NumberRenderer<>(
                s -> s.getAttributeValue(SecurityAttribute.yield, false), "%(,.2f"))
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.END)
        .setHeader("Yield");
    securityGrid
        .addColumn(
            new NumberRenderer<>(
                s -> s.getAttributeValue(SecurityAttribute.duration, false), "%(,.3f"))
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.END)
        .setHeader("Duration");
    securityGrid
        .addColumn(s -> s.getAttributeValue(SecurityAttribute.issuer, false))
        .setAutoWidth(true)
        .setHeader("Issuer");
    securityGrid
        .addColumn(
            new NumberRenderer<>(
                s -> s.getAttributeValue(SecurityAttribute.price, false), "$%(,.4f", Locale.US))
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.END)
        .setHeader("Price");

    add(securityGrid);

    IMap<PortfolioKey, Portfolio> portfolioCache = assetCache.getPortfolioCache();

    // TODO make portfolios sortable
    List<PortfolioKey> portfolioKeys = new ArrayList<>(portfolioCache.keySet());
    portfolioKeys.sort(PortfolioKey::compareTo);

    CallbackDataProvider<PortfolioSummary, Void> portfolioProvider =
        DataProvider.fromCallbacks(
            query ->
                portfolioCache
                    .executeOnKeys(
                        new FakeSet<>(
                            portfolioKeys.subList(
                                query.getOffset(),
                                Math.min(
                                    query.getOffset() + query.getLimit(), portfolioKeys.size()))),
                        new PortfolioSummarizer(new EvaluationContext()))
                    .values()
                    .stream(),
            query -> portfolioKeys.size());

    Grid<PortfolioSummary> portfolioGrid = new Grid<>();
    portfolioGrid.setColumnReorderingAllowed(true);
    portfolioGrid.addThemeVariants(
        GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
    portfolioGrid.setItems(portfolioProvider);

    portfolioGrid
        .addColumn(p -> p.getKey().getId())
        .setAutoWidth(true)
        .setFrozen(true)
        .setHeader("ID");
    portfolioGrid.addColumn(PortfolioSummary::name).setAutoWidth(true).setHeader("Name");
    portfolioGrid
        .addColumn(new NumberRenderer<>(PortfolioSummary::totalMarketValue, "$%(,.2f", Locale.US))
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.END)
        .setHeader("Market Value");
    portfolioGrid
        .addColumn(PortfolioSummary::benchmarkKey)
        .setAutoWidth(true)
        .setHeader("Benchmark");

    add(portfolioGrid);
  }

  /**
   * Formats the given rating information for table display.
   *
   * @param security the {@link Security} from which to obtain rating information
   * @param symbolAttribute the {@link SecurityAttribute} corresponding to the desired rating symbol
   * @param valueAttribute the {@link SecurityAttribute} corresponding to the desired rating value
   * @return a {@link String} representing the specified rating data, or {@code null} if the value
   *     of the specified symbol attribute is {@code null}
   */
  protected String getRatingText(
      Security security,
      SecurityAttribute<String> symbolAttribute,
      SecurityAttribute<Double> valueAttribute) {
    String symbol = security.getAttributeValue(symbolAttribute, false);
    return (symbol == null
        ? null
        : symbol
            + " ["
            + String.format("%(,.4f", security.getAttributeValue(valueAttribute))
            + "]");
  }
}
