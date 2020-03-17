package org.slaq.slaqworx.panoptes.ui.trading;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Predicate;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.asset.SecurityAttributes;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.ui.ComponentUtil;
import org.slaq.slaqworx.panoptes.ui.MinMaxField;

/**
 * {@code SecurityFilterPanel} is a component of the experimental user interface, providing the
 * means to filter the master {@code Security} list by a variety of attributes.
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
     * Creates a new {@code SecurityFilterPanel}.
     *
     * @param securityProvider
     *            the {@code SecurityDataProvider} to use to query {@code Security} data
     * @param assetCache
     *            the {@code AssetCache} to use to obtain other data
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
        currencyTextField =
                ComponentUtil.createSelect("Currency", true, assetCache.getCurrencies());
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

        Button filterButton = ComponentUtil.createButton("Filter", event -> {
            filter();
        });
        Button resetButton = ComponentUtil.createButton("Reset", event -> {
            // clear the value of every child element that has a value
            getElement().getChildren().forEach(e -> e.getComponent().ifPresent(c -> {
                if (c instanceof HasValue) {
                    ((HasValue<?, ?>)c).clear();
                }
            }));
            securityProvider.setFilter(s -> true);
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
                ((AbstractField<?, ?>)component).addValueChangeListener(e -> filter());
            }
        }
    }

    protected Predicate<SecurityAttributes> append(Predicate<SecurityAttributes> p1,
            Predicate<SecurityAttributes> p2) {
        if (p1 == null) {
            return p2;
        }

        if (p2 == null) {
            return p1;
        }

        return p1.and(p2);
    }

    protected Predicate<SecurityAttributes> append(Predicate<SecurityAttributes> predicate,
            SecurityAttribute<String> attribute, String filterValue) {
        if (filterValue == null || filterValue.isBlank()) {
            // nothing new to add
            return predicate;
        }

        Predicate<SecurityAttributes> attributeFilter = (a -> {
            String attributeValue = a.getValue(attribute);
            if (attributeValue != null) {
                return attributeValue.toLowerCase().contains(filterValue.toLowerCase());
            }

            return false;
        });

        return append(predicate, attributeFilter);
    }

    protected <T extends Comparable<? super T>> Predicate<SecurityAttributes> append(
            Predicate<SecurityAttributes> predicate, SecurityAttribute<T> attribute, T minValue,
            T maxValue) {
        if (minValue == null && maxValue == null) {
            // nothing new to add
            return predicate;
        }

        Predicate<SecurityAttributes> attributeFilter = (a -> {
            T attributeValue = a.getValue(attribute);
            if (attributeValue != null) {
                boolean isMinValueMet =
                        (minValue == null || attributeValue.compareTo(minValue) >= 0);
                boolean isMaxValueMet =
                        (maxValue == null || attributeValue.compareTo(maxValue) <= 0);

                return isMinValueMet && isMaxValueMet;
            }

            return false;
        });

        return append(predicate, attributeFilter);
    }

    protected void filter() {
        Predicate<SecurityAttributes> filter =
                append(null, SecurityAttribute.isin, assetIdTextField.getValue());
        filter = append(filter, SecurityAttribute.cusip, cusipTextField.getValue());
        filter = append(filter, SecurityAttribute.description, descriptionTextField.getValue());
        filter = append(filter, SecurityAttribute.country, countryTextField.getValue());
        filter = append(filter, SecurityAttribute.region, regionTextField.getValue());
        filter = append(filter, SecurityAttribute.sector, sectorTextField.getValue());
        filter = append(filter, SecurityAttribute.currency, currencyTextField.getValue());
        filter = append(filter, SecurityAttribute.coupon, toDouble(couponMinMaxField.getMinValue()),
                toDouble(couponMinMaxField.getMaxValue()));
        filter = append(filter, SecurityAttribute.maturityDate,
                maturityDateMinMaxField.getMinValue(), maturityDateMinMaxField.getMaxValue());
        filter = append(filter, SecurityAttribute.rating1Value,
                toDouble(ratingMinMaxField.getMinValue()),
                toDouble(ratingMinMaxField.getMaxValue()));
        filter = append(filter, SecurityAttribute.yield, toDouble(yieldMinMaxField.getMinValue()),
                toDouble(yieldMinMaxField.getMaxValue()));
        filter = append(filter, SecurityAttribute.duration,
                toDouble(durationMinMaxField.getMinValue()),
                toDouble(durationMinMaxField.getMaxValue()));
        filter = append(filter, SecurityAttribute.issuer, issuerTextField.getValue());
        filter = append(filter, SecurityAttribute.price, toDouble(priceMinMaxField.getMinValue()),
                toDouble(priceMinMaxField.getMaxValue()));
        Predicate<SecurityAttributes> finalFilter = filter;

        Predicate<Security> securityFilter =
                (filter == null ? (s -> true) : (s -> finalFilter.test(s.getAttributes())));
        securityProvider.setFilter(securityFilter);
    }

    protected Double toDouble(BigDecimal bigDecimal) {
        return (bigDecimal == null ? null : bigDecimal.doubleValue());
    }
}
