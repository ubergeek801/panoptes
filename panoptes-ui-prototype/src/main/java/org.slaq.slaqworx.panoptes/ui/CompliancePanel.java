package org.slaq.slaqworx.panoptes.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Creates a new {@link CompliancePanel}.
 */
public class CompliancePanel extends VerticalLayout {
  public CompliancePanel() {
    add(createTradeReviewPanel("2021000001", "30m", "1VKB8GXX", 1_000_000, 1_234_567.89));
    add(createTradeReviewPanel("2021000002", "28m", "FNI03010", 10_000_000, 98_765_432.10));
    add(createTradeReviewPanel("2021000008", "15m", "434IDGXX", 2_000_000, 2_345_678.90));
    add(createTradeReviewPanel("2021000012", "8m", "XBRL1904", 5_000_000, 5_678_901.23));
    add(createTradeReviewPanel("2021000025", "30s", "04WJL0XX", 3_000_000, 3_456_789.01));
  }

  protected FormLayout createTradeReviewPanel(String tradeId, String age, String cusip,
      double amount, double marketValue) {
    final int NUM_COLUMNS = 6;
    FormLayout panel = new FormLayout();
    panel.setResponsiveSteps(new FormLayout.ResponsiveStep("1em", NUM_COLUMNS));

    panel.add(new Button("Accept", new Icon(VaadinIcon.EDIT)));

    TextField tradeIdField = new TextField("trade", tradeId, "trade");
    tradeIdField.setReadOnly(true);
    panel.add(tradeIdField);

    TextField ageField = new TextField("age", age, "age");
    ageField.setReadOnly(true);
    panel.add(ageField);

    TextField cusipField = new TextField("cusip", cusip, "cusip");
    cusipField.setReadOnly(true);
    panel.add(cusipField);

    BigDecimalField amountField =
        new BigDecimalField("amount", BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP),
            "amount");
    amountField.setReadOnly(true);
    amountField.addThemeVariants(TextFieldVariant.LUMO_SMALL, TextFieldVariant.LUMO_ALIGN_RIGHT);
    panel.add(amountField);

    BigDecimalField marketValueField =
        new BigDecimalField("mv", BigDecimal.valueOf(marketValue).setScale(2, RoundingMode.HALF_UP),
            "mv");
    marketValueField.setReadOnly(true);
    marketValueField
        .addThemeVariants(TextFieldVariant.LUMO_SMALL, TextFieldVariant.LUMO_ALIGN_RIGHT);
    panel.add(marketValueField);

    HorizontalLayout allocationSummary = new HorizontalLayout();
    allocationSummary.setPadding(true);

    allocationSummary.add("allocations");

    Icon compliantIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
    compliantIcon.setSize("1em");
    compliantIcon.setColor("lime");
    allocationSummary.add(compliantIcon);
    allocationSummary.add(new Span("10"));

    Icon approvedIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);
    approvedIcon.setSize("1em");
    approvedIcon.setColor("cyan");
    allocationSummary.add(approvedIcon);
    allocationSummary.add(new Span("0"));

    Icon warnIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE);
    warnIcon.setSize("1em");
    warnIcon.setColor("yellow");
    allocationSummary.add(warnIcon);
    allocationSummary.add(new Span("3"));

    Icon violationIcon = new Icon(VaadinIcon.CLOSE_CIRCLE);
    violationIcon.setSize("1em");
    violationIcon.setColor("red");
    allocationSummary.add(violationIcon);
    allocationSummary.add(new Span("2"));

    allocationSummary
        .setVerticalComponentAlignment(Alignment.END, compliantIcon, approvedIcon, warnIcon,
            violationIcon);

    VerticalLayout allocations = new VerticalLayout();
    allocations.add(createAllocationReviewPanel(0, "1234", "Acme Inc.", "jlr", amount * 0.4,
        marketValue * 0.4));
    allocations.add(createAllocationReviewPanel(1, "5678", "Widgets LLC", "jlr", amount * 0.2,
        marketValue * 0.2));
    allocations.add(
        createAllocationReviewPanel(2, "9012", "BigCo Retirement Fund", "jlr", amount * 0.2,
            marketValue * 0.2));
    allocations.add(
        createAllocationReviewPanel(3, "3456", "Ivy Design Group LLC Master Investment", "jlr",
            amount * 0.1, marketValue * 0.1));
    allocations.add(
        createAllocationReviewPanel(4, "7890", "Sovereign Wealth Fund of Elbonia", "jlr",
            amount * 0.1, marketValue * 0.1));

    Details allocationDetails = new Details(allocationSummary, allocations);
    panel.add(allocationDetails);
    panel.setColspan(allocationDetails, NUM_COLUMNS);

    return panel;
  }

  protected FormLayout createAllocationReviewPanel(int index, String portfolioId,
      String portfolioName, String portfolioManager, double amount, double marketValue) {
    final int NUM_COLUMNS = 6;
    FormLayout panel = new FormLayout();
    if (index % 2 == 1) {
      panel.getStyle().set("background-color", "#2c3d52"); // FIXME get from theme somehow
    }
    panel.setResponsiveSteps(new FormLayout.ResponsiveStep("1em", NUM_COLUMNS));

    TextField portfolioIdField = new TextField("id", portfolioId, "id");
    portfolioIdField.setReadOnly(true);
    panel.add(portfolioIdField);

    TextField portfolioNameField = new TextField("name", portfolioName, "name");
    portfolioNameField.setReadOnly(true);
    panel.add(portfolioNameField);
    panel.setColspan(portfolioNameField, 2);

    TextField portfolioManagerField = new TextField("pm", portfolioManager, "pm");
    portfolioManagerField.setReadOnly(true);
    panel.add(portfolioManagerField);
    BigDecimalField amountField =
        new BigDecimalField("amount", BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP),
            "amount");
    amountField.setReadOnly(true);
    amountField.addThemeVariants(TextFieldVariant.LUMO_SMALL, TextFieldVariant.LUMO_ALIGN_RIGHT);
    panel.add(amountField);

    BigDecimalField marketValueField =
        new BigDecimalField("mv", BigDecimal.valueOf(marketValue).setScale(2, RoundingMode.HALF_UP),
            "mv");
    marketValueField.setReadOnly(true);
    marketValueField
        .addThemeVariants(TextFieldVariant.LUMO_SMALL, TextFieldVariant.LUMO_ALIGN_RIGHT);
    panel.add(marketValueField);

    Component violationReviewPanel =
        createViolationReviewPanel(VaadinIcon.CLOSE_CIRCLE, "10012468", "Restricted issuer ACME",
            new BigDecimal("0"), new BigDecimal("400000"), VaadinIcon.TRENDING_UP);
    panel.add(violationReviewPanel);
    panel.setColspan(violationReviewPanel, NUM_COLUMNS);

    violationReviewPanel = createViolationReviewPanel(VaadinIcon.EXCLAMATION_CIRCLE, "10013579",
        "Average quality must be ≥ 90 (warning threshold)", new BigDecimal("90.234"),
        new BigDecimal("89.985"), VaadinIcon.TRENDING_DOWN);
    panel.add(violationReviewPanel);
    panel.setColspan(violationReviewPanel, NUM_COLUMNS);

    return panel;
  }

  protected HorizontalLayout createViolationReviewPanel(VaadinIcon allocationStatusIcon,
      String ruleId, String ruleDescription, BigDecimal preOrderValue, BigDecimal postOrderValue,
      VaadinIcon orderImpactIcon) {
    HorizontalLayout panel = new HorizontalLayout();
    panel.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

    Icon statusIcon = new Icon(allocationStatusIcon);
    statusIcon.setSize("1em");
    String color;
    if (allocationStatusIcon == VaadinIcon.CLOSE_CIRCLE) {
      color = "red";
    } else if (allocationStatusIcon == VaadinIcon.EXCLAMATION_CIRCLE) {
      color = "yellow";
    } else {
      color = "white";
    }
    statusIcon.setColor(color);
    panel.add(statusIcon);

    TextField ruleIdField = new TextField("rule", ruleId, "rule");
    ruleIdField.setReadOnly(true);
    panel.add(ruleIdField);

    TextField ruleDescriptionField = new TextField("description", ruleDescription, "description");
    ruleDescriptionField.setReadOnly(true);
    panel.addAndExpand(ruleDescriptionField);

    BigDecimalField preOrderValueField = new BigDecimalField("pre", preOrderValue, "pre");
    preOrderValueField.setReadOnly(true);
    panel.add(preOrderValueField);

    BigDecimalField postOrderValueField = new BigDecimalField("post", postOrderValue, "post");
    postOrderValueField.setReadOnly(true);
    panel.add(postOrderValueField);

    Icon orderIcon = new Icon(orderImpactIcon);
    orderIcon.setSize("1em");
    if (orderImpactIcon == VaadinIcon.TRENDING_UP) {
      color = "red";
    } else if (orderImpactIcon == VaadinIcon.TRENDIND_DOWN) {
      color = "yellow";
    } else {
      color = "white";
    }
    orderIcon.setColor(color);
    panel.add(orderIcon);

    panel.add(new Button("Approve", new Icon(VaadinIcon.CHECK_CIRCLE_O)));
    panel.add(new Button("Reject", new Icon(VaadinIcon.CLOSE_CIRCLE)));

    return panel;
  }
}
