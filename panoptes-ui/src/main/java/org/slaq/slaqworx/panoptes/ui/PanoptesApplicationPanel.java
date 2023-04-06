package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import io.micronaut.context.annotation.Prototype;
import java.util.Map;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluator;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluator;
import org.slaq.slaqworx.panoptes.ui.compliance.CompliancePanel;
import org.slaq.slaqworx.panoptes.ui.trading.TradingPanel;

/**
 * The top-level layout for an experimental user interface.
 *
 * @author jeremy
 */
@Route("")
@Push
@Theme(themeClass = Lumo.class, variant = Lumo.DARK)
@Prototype
public class PanoptesApplicationPanel extends AppLayout implements AppShellConfigurator {
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new {@link PanoptesApplicationPanel}.
   *
   * @param portfolioEvaluator the {@link PortfolioEvaluator} to use to perform compliance
   *     evaluation
   * @param tradeEvaluator the {@link TradeEvaluator} to use to perform compliance evaluation
   * @param assetCache the {@link AssetCache} to use to resolve cached entities
   */
  protected PanoptesApplicationPanel(
      PortfolioEvaluator portfolioEvaluator, TradeEvaluator tradeEvaluator, AssetCache assetCache) {
    Icon applicationIcon = new Icon(VaadinIcon.EYE);
    Span applicationTitle = new Span("Panoptes");
    applicationTitle
        .getStyle()
        .set("font-weight", "bold")
        .set("font-size", "120%")
        .set("padding-left", "0.3em")
        .set("padding-right", "0.3em");
    Span applicationTagline = new Span("watchful portfolio compliance");
    applicationTagline.getStyle().set("font-style", "italic").set("font-size", "80%");
    Span applicationInfo = new Span();
    applicationInfo.add(applicationTitle, applicationTagline);

    addToNavbar(new DrawerToggle(), applicationIcon, applicationInfo);

    TradingPanel tradingPanel = new TradingPanel(tradeEvaluator, assetCache);
    tradingPanel.setSizeFull();
    setContent(tradingPanel);

    CompliancePanel compliancePanel = new CompliancePanel(portfolioEvaluator, assetCache);
    compliancePanel.setSizeFull();

    VerticalLayout aboutPanel = new VerticalLayout();
    Icon aboutApplicationIcon = new Icon(VaadinIcon.EYE);
    aboutApplicationIcon.setSize("5em");
    aboutApplicationIcon.getStyle().set("position", "relative").set("top", "-0.9em");
    Span aboutApplicationTitle = new Span("Panoptes");
    aboutApplicationTitle
        .getStyle()
        .set("font-weight", "bold")
        .set("font-size", "300%")
        .set("padding-left", "0.3em");
    Span aboutApplicationInfo = new Span();
    aboutApplicationInfo.add(aboutApplicationIcon, aboutApplicationTitle);
    aboutPanel.add(aboutApplicationInfo);
    Span aboutApplicationTagline = new Span("watchful portfolio compliance");
    aboutApplicationTagline.getStyle().set("font-style", "italic");
    aboutPanel.add(aboutApplicationTagline);
    Span aboutApplicationDescription = new Span("© Jeremy Rosenberger | slaq.org slaqworx");
    aboutPanel.add(aboutApplicationDescription);
    aboutPanel.setSizeFull();

    Tab tradingTab = new Tab("Trading");
    Tab complianceTab = new Tab("Compliance");
    Tab aboutTab = new Tab("About");
    Tabs tabs = new Tabs(tradingTab, complianceTab, aboutTab);
    Map<Tab, Component> tabPageMap =
        Map.of(tradingTab, tradingPanel, complianceTab, compliancePanel, aboutTab, aboutPanel);
    tabs.setOrientation(Tabs.Orientation.VERTICAL);
    tabs.addSelectedChangeListener(
        e -> {
          setContent(tabPageMap.get(e.getSelectedTab()));
        });
    addToDrawer(tabs);
    tabs.setSelectedTab(tradingTab);
  }
}
