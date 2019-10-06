package org.slaq.slaqworx.panoptes.ui;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slaq.slaqworx.panoptes.ApplicationContextProvider;
import org.slaq.slaqworx.panoptes.asset.Portfolio;
import org.slaq.slaqworx.panoptes.asset.PortfolioKey;
import org.slaq.slaqworx.panoptes.asset.Security;
import org.slaq.slaqworx.panoptes.asset.SecurityKey;
import org.slaq.slaqworx.panoptes.cache.AssetCache;
import org.slaq.slaqworx.panoptes.evaluator.ClusterPortfolioEvaluator;
import org.slaq.slaqworx.panoptes.evaluator.PortfolioEvaluator;
import org.slaq.slaqworx.panoptes.trade.TradeEvaluator;

public class FixedIncomeTradePanel extends FormLayout {
    class AllocationPanel extends HorizontalLayout {
        private static final long serialVersionUID = 1L;

        private final TextField portfolioIdField;
        private final TextField portfolioNameField;
        private final NumberField amountField;
        private final NumberField marketValueField;

        private Portfolio portfolio;

        AllocationPanel(HasComponents parent) {
            portfolioIdField = ComponentUtil.createTextField("Portfolio ID");
            portfolioIdField.setValueChangeMode(ValueChangeMode.EAGER);
            add(portfolioIdField);

            portfolioNameField = ComponentUtil.createTextField(null);
            portfolioNameField.setReadOnly(true);
            addAndExpand(portfolioNameField);

            amountField = ComponentUtil.createNumberField("Amount");
            add(amountField);

            marketValueField = ComponentUtil.createNumberField("Market Value");
            add(marketValueField);

            Button room = ComponentUtil.createButton("Room", event -> {
                if (portfolio == null || security == null || tradeMarketValue == null) {
                    return;
                }

                TradeEvaluator tradeEvaluator =
                        new TradeEvaluator(portfolioEvaluator, assetCache, assetCache, assetCache);

                try {
                    double roomMarketValue =
                            tradeEvaluator.evaluateRoom(portfolio, security, tradeMarketValue);
                    marketValueField.setValue(roomMarketValue);
                    amountField.setValue(tradePrice == null ? null : roomMarketValue / tradePrice);
                } catch (InterruptedException | ExecutionException e) {
                    // FIXME handle this
                }
            });
            // Room will be enabled when a Portfolio ID is entered
            room.setEnabled(false);
            add(room);

            add(ComponentUtil.createButton("Delete", event -> {
                parent.remove(this);
            }));

            setWidthFull();

            // add event listeners

            portfolioIdField.addValueChangeListener(event -> {
                // FIXME use a proper version
                portfolio =
                        assetCache.getPortfolio(new PortfolioKey(portfolioIdField.getValue(), 1));
                if (portfolio == null) {
                    portfolioIdField.setErrorMessage("not found");
                    portfolioIdField.setInvalid(true);
                    room.setEnabled(false);
                    return;
                }

                portfolioIdField.setInvalid(false);
                portfolioNameField.setValue(portfolio.getName());
                room.setEnabled(true);
            });
        }
    }

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FixedIncomeTradePanel.class);

    // TODO this isn't very "responsive"
    private static final int NUM_COLUMNS = 7;

    private final PortfolioEvaluator portfolioEvaluator;
    private final AssetCache assetCache;

    private final NumberField tradeMarketValueField;

    private Security security;
    private Double tradeAmount;
    private Double tradePrice;
    private Double tradeMarketValue;

    public FixedIncomeTradePanel() {
        portfolioEvaluator = ApplicationContextProvider.getApplicationContext()
                .getBean(ClusterPortfolioEvaluator.class);
        assetCache = ApplicationContextProvider.getApplicationContext().getBean(AssetCache.class);

        setResponsiveSteps(new ResponsiveStep("1em", NUM_COLUMNS));

        add(ComponentUtil.createSelect("Transaction", "Buy", "Sell"));

        TextField assetIdTextField = ComponentUtil.createTextField("Asset ID");
        assetIdTextField.setValueChangeMode(ValueChangeMode.EAGER);
        add(assetIdTextField);

        NumberField amountField = ComponentUtil.createNumberField("Amount");
        add(amountField);

        NumberField priceField = ComponentUtil.createNumberField("Price");
        add(priceField);

        tradeMarketValueField = ComponentUtil.createNumberField("Market Value");
        tradeMarketValueField.setReadOnly(true);
        add(tradeMarketValueField);

        add(ComponentUtil.createDatePicker("Trade Date", LocalDate.now()));

        add(ComponentUtil.createDatePicker("Settlement Date"));

        VerticalLayout allocations = new VerticalLayout();
        Label allocationsLabel = ComponentUtil.createLabel("Allocations");
        allocationsLabel.setWidthFull();
        allocations.add(allocationsLabel);
        AllocationPanel allocation = new AllocationPanel(allocations);
        allocations.add(allocation);
        Button newAllocation = ComponentUtil.createButton("New", event -> {
            // add a new allocation row just before the last component (allocationActions)
            allocations.addComponentAtIndex(allocations.getComponentCount() - 1,
                    new AllocationPanel(allocations));
        });
        Button room = ComponentUtil.createButton("Room", event -> {
            if (security == null || tradeMarketValue == null) {
                return;
            }

            UI ui = getUI().get();

            int allocationIndex[] = new int[] { 1 };
            int numPortfolios[] = new int[] { assetCache.getPortfolioCache().size() };
            TradeEvaluator tradeEvaluator =
                    new TradeEvaluator(portfolioEvaluator, assetCache, assetCache, assetCache);
            // execute on a separate thread to allow UI to update
            new Thread(() -> {
                assetCache.getPortfolioCache().forEach(e -> {
                    Portfolio portfolio = e.getValue();
                    if (portfolio.isAbstract()) {
                        // don't evaluate benchmarks
                        return;
                    }
                    double roomMarketValue;
                    try {
                        roomMarketValue =
                                tradeEvaluator.evaluateRoom(portfolio, security, tradeMarketValue);
                    } catch (Exception ex) {
                        // FIXME handle this
                        LOG.error("could not evaluate room for Portfolio {}", portfolio.getKey(),
                                ex);
                        return;
                    }
                    ui.access(() -> {
                        numPortfolios[0]--;
                        if (roomMarketValue != 0) {
                            AllocationPanel allocationPanel = new AllocationPanel(allocations);
                            // add at the next position
                            allocations.addComponentAtIndex(allocationIndex[0]++, allocationPanel);
                            allocationPanel.portfolioIdField.setValue(portfolio.getKey().getId());
                            allocationPanel.portfolioNameField.setValue(portfolio.getName());
                            allocationPanel.amountField.setValue(
                                    tradePrice == null ? null : roomMarketValue / tradePrice);
                            allocationPanel.marketValueField.setValue(roomMarketValue);
                        }
                        event.getSource().setText(numPortfolios[0] + " to process");
                    });
                });

                // reset button label when complete
                ui.access(() -> event.getSource().setText("Room"));
            }, "ui-room-evaluator").start();
        });
        // Room will be enabled when an Asset ID is entered
        room.setEnabled(false);

        HorizontalLayout allocationActions = new HorizontalLayout();
        allocationActions.add(newAllocation, room);
        newAllocation.getStyle().set("margin-right", "0.3em");
        allocations.add(allocationActions);
        allocations.setWidthFull();

        add(allocations, NUM_COLUMNS);

        Button submit = ComponentUtil.createButton("Submit", event -> {
            // FIXME implement listener
        });
        Button cancel = ComponentUtil.createButton("Cancel", event -> {
            // FIXME implement listener
        });
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(submit, cancel);
        submit.getStyle().set("margin-right", "0.3em");
        add(actions, NUM_COLUMNS);

        // add event listeners

        amountField.addValueChangeListener(event -> {
            tradeAmount = event.getValue();
            updateTradeMarketValue();
        });

        priceField.addValueChangeListener(event -> {
            tradePrice = event.getValue();
            updateTradeMarketValue();
        });

        assetIdTextField.addValueChangeListener(event -> {
            security = assetCache.getSecurity(new SecurityKey(event.getValue()));
            if (security == null) {
                assetIdTextField.setErrorMessage("not found");
                assetIdTextField.setInvalid(true);
                room.setEnabled(false);
                return;
            }

            assetIdTextField.setInvalid(false);
            Double price = priceField.getValue();
            if (price == null) {
                priceField.setValue(null);
            } else {
                priceField.setValue(price);
                tradeMarketValueField.setValue(price * amountField.getValue());
            }
            room.setEnabled(true);
        });
    }

    protected void updateTradeMarketValue() {
        if (tradeAmount == null || tradePrice == null) {
            tradeMarketValue = null;
        } else {
            tradeMarketValue = tradeAmount * tradePrice;
        }

        tradeMarketValueField.setValue(tradeMarketValue);
    }
}
