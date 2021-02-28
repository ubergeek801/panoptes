package org.slaq.slaqworx.panoptes.ui.trading;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.cache.SecurityFilter;
import org.slaq.slaqworx.panoptes.ui.ComponentUtil;
import org.slaq.slaqworx.panoptes.ui.MinMaxField;

/**
 * A component of the experimental user interface, providing the means to filter the master {@link
 * Security} list by a variety of attributes.
 *
 * @author jeremy
 */
public class SecurityFilterPanel extends FormLayout {
  private static final long serialVersionUID = 1L;

  private static final int NUM_COLUMNS = 7; // TODO this isn't very "responsive"

  private final SecurityDataProvider securityProvider;

  private final TextField assetIdTextField;
  private final TextField cusipTextField;
  private final TextField descriptionTextField;
  private final Select<String> countryTextField;
  private final Select<String> regionTextField;
  private final Select<String> sectorTextField;
  private final Select<String> currencyTextField;
  private final MinMaxField<BigDecimal> couponMinMaxField;
  private final MinMaxField<LocalDate> maturityDateMinMaxField;
  private final MinMaxField<BigDecimal> ratingMinMaxField;
  private final MinMaxField<BigDecimal> yieldMinMaxField;
  private final MinMaxField<BigDecimal> durationMinMaxField;
  private final TextField issuerTextField;
  private final MinMaxField<BigDecimal> priceMinMaxField;

  /**
   * Creates a new {@link SecurityFilterPanel}.
   *
   * @param securityProvider
   *     the {@link SecurityDataProvider} to use to query {@link Security} data
   * @param assetCache
   *     the {@link AssetCache} to use to obtain other data
   */
  public SecurityFilterPanel(SecurityDataProvider securityProvider, AssetCache assetCache) {
    this.securityProvider = securityProvider;

    setResponsiveSteps(new ResponsiveStep("1em", NUM_COLUMNS));

    assetIdTextField = ComponentUtil.createTextField("Asset ID");
    add(assetIdTextField);
    cusipTextField = ComponentUtil.createTextField("CUSIP");
    add(cusipTextField);
    descriptionTextField = ComponentUtil.createTextField("Description");
    add(descriptionTextField, 2);
    countryTextField = ComponentUtil.createSelect("Country", true, assetCache.getCountries());
    add(countryTextField);
    regionTextField = ComponentUtil.createSelect("Region", true, assetCache.getRegions());
    add(regionTextField);
    sectorTextField = ComponentUtil.createSelect("Sector", true, assetCache.getSectors());
    add(sectorTextField);
    currencyTextField = ComponentUtil.createSelect("Currency", true, assetCache.getCurrencies());
    add(currencyTextField);
    couponMinMaxField = ComponentUtil.createMinMaxNumberField("Coupon");
    add(couponMinMaxField, 2);
    maturityDateMinMaxField = ComponentUtil.createMinMaxDateField("Maturity");
    add(maturityDateMinMaxField, 2);
    ratingMinMaxField = ComponentUtil.createMinMaxNumberField("Rating");
    add(ratingMinMaxField, 2);
    yieldMinMaxField = ComponentUtil.createMinMaxNumberField("Yield");
    add(yieldMinMaxField, 2);
    durationMinMaxField = ComponentUtil.createMinMaxNumberField("Duration");
    add(durationMinMaxField, 2);
    issuerTextField = ComponentUtil.createTextField("Issuer");
    add(issuerTextField);
    priceMinMaxField = ComponentUtil.createMinMaxNumberField("Price");
    add(priceMinMaxField, 2);

    Button filterButton = ComponentUtil.createButton("Filter", event -> filter());
    Button resetButton = ComponentUtil.createButton("Reset", event -> {
      // clear the value of every child element that has a value
      getElement().getChildren().forEach(e -> e.getComponent().ifPresent(c -> {
        if (c instanceof HasValue) {
          ((HasValue<?, ?>) c).clear();
        }
      }));
      securityProvider.setFilter(null);
    });

    HorizontalLayout actions = new HorizontalLayout();
    actions.add(filterButton, resetButton);
    filterButton.getStyle().set("margin-right", "0.3em");

    add(actions, NUM_COLUMNS);
  }

  @Override
  public void add(Component... components) {
    super.add(components);
    for (Component component : components) {
      if (component instanceof AbstractField) {
        ((AbstractField<?, ?>) component).addValueChangeListener(e -> filter());
      }
    }
  }

  /**
   * Creates a {@link SecurityFilter} based on the currently chosen parameters and instructs the
   * {@link SecurityDataProvider} to apply that filter.
   */
  protected void filter() {
    SecurityFilter filter = new SecurityFilter();

    filter.add(SecurityAttribute.isin, assetIdTextField.getValue())
        .add(SecurityAttribute.cusip, cusipTextField.getValue())
        .add(SecurityAttribute.description, descriptionTextField.getValue())
        .add(SecurityAttribute.country, countryTextField.getValue())
        .add(SecurityAttribute.region, regionTextField.getValue())
        .add(SecurityAttribute.sector, sectorTextField.getValue())
        .add(SecurityAttribute.currency, currencyTextField.getValue())
        .add(SecurityAttribute.coupon, toDouble(couponMinMaxField.getMinValue()),
            toDouble(couponMinMaxField.getMaxValue()))
        .add(SecurityAttribute.maturityDate, maturityDateMinMaxField.getMinValue(),
            maturityDateMinMaxField.getMaxValue())
        .add(SecurityAttribute.rating1Value, toDouble(ratingMinMaxField.getMinValue()),
            toDouble(ratingMinMaxField.getMaxValue()))
        .add(SecurityAttribute.yield, toDouble(yieldMinMaxField.getMinValue()),
            toDouble(yieldMinMaxField.getMaxValue()))
        .add(SecurityAttribute.duration, toDouble(durationMinMaxField.getMinValue()),
            toDouble(durationMinMaxField.getMaxValue()))
        .add(SecurityAttribute.issuer, issuerTextField.getValue())
        .add(SecurityAttribute.price, toDouble(priceMinMaxField.getMinValue()),
            toDouble(priceMinMaxField.getMaxValue()));

    securityProvider.setFilter(filter);
  }

  /**
   * Converts a {@link BigDecimal} to a {@link Double}, allowing for {@code null} values.
   *
   * @param bigDecimal
   *     the {@link BigDecimal} value to be converted
   *
   * @return a {@link Double} representation of the given {@link BigDecimal}, or {@code null} if
   *     given {@code null}
   */
  protected Double toDouble(BigDecimal bigDecimal) {
    return (bigDecimal == null ? null : bigDecimal.doubleValue());
  }
}
