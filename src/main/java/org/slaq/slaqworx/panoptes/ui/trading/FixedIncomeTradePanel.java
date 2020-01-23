package org.slaq.slaqworx.panoptes.ui.trading;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

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
import org.slaq.slaqworx.panoptes.ui.ComponentUtil;
import org.slaq.slaqworx.panoptes.ui.PortfolioSummary;
import org.slaq.slaqworx.panoptes.util.ForkJoinPoolFactory;

/**
 * {@code FixedIncomeTradePanel} is a component of the experimental user interface, used to enter
 * parameters of a simple fixed income security trade and calculate room in portfolios for that
 * security name.
 *
 * @author jeremy
 */
public class FixedIncomeTradePanel extends FormLayout {
    /**
     * {@code AllocationPanel} summarizes the details of an allocation (portfolio, amount and market
     * value) and provides actions that can be taken on that allocation.
     */
    class AllocationPanel extends HorizontalLayout {
        private static final long serialVersionUID = 1L;

        private final TextField portfolioIdField;
        private final TextField portfolioNameField;
        private final NumberField amountField;
        private final NumberField marketValueField;

        private Portfolio portfolio;

        /**
         * Creates a new {@code AllocationPanel} with the given parent.
         *
         * @param parent
         *            the parent of the {@code AllocationPanel} component
         */
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

                TradeEvaluator tradeEvaluator = new TradeEvaluator(portfolioEvaluator, assetCache);
                try {
                    double roomMarketValue = tradeEvaluator.evaluateRoom(portfolio.getKey(),
                            security, tradeMarketValue);
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

    private final ForkJoinPool roomEvaluatorExecutor =
            ForkJoinPoolFactory.newForkJoinPool(4, "ui-room-evaluator");

    private final PortfolioEvaluator portfolioEvaluator;
    private final AssetCache assetCache;

    private final NumberField tradeMarketValueField;

    private Security security;
    private Double tradeAmount;
    private Double tradePrice;
    private Double tradeMarketValue;

    /**
     * Creates a new {@code FixedIncomeTradePanel}.
     */
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
            ui.access(() -> {
                event.getSource().setEnabled(false);
            });

            int allocationIndex[] = new int[] { 1 };
            int numPortfolios = assetCache.getPortfolioCache().size();
            int numRemaining[] = new int[] { numPortfolios };
            TradeEvaluator tradeEvaluator = new TradeEvaluator(portfolioEvaluator, assetCache);
            Set<PortfolioKey> portfolioKeys = assetCache.getPortfolioCache().keySet();
            long startTime = System.currentTimeMillis();
            ForkJoinTask<?> future = roomEvaluatorExecutor
                    .submit(() -> portfolioKeys.parallelStream().forEach(portfolioKey -> {
                        try {
                            PortfolioSummary portfolio =
                                    assetCache.getPortfolioCache().executeOnKey(portfolioKey,
                                            e -> PortfolioSummary.fromPortfolio(e.getValue()));
                            if (portfolio.isAbstract()) {
                                // don't evaluate benchmarks
                                return;
                            }

                            double roomMarketValue;
                            try {
                                roomMarketValue = tradeEvaluator.evaluateRoom(portfolioKey,
                                        security, tradeMarketValue);
                            } catch (Exception ex) {
                                // FIXME handle this
                                LOG.error("could not evaluate room for Portfolio {}", portfolioKey,
                                        ex);
                                return;
                            }
                            ui.access(() -> {
                                if (roomMarketValue != 0) {
                                    AllocationPanel allocationPanel =
                                            new AllocationPanel(allocations);
                                    // add at the next position
                                    allocations.addComponentAtIndex(allocationIndex[0]++,
                                            allocationPanel);
                                    allocationPanel.portfolioIdField.setValue(portfolioKey.getId());
                                    allocationPanel.portfolioNameField
                                            .setValue(portfolio.getName());
                                    allocationPanel.amountField.setValue(tradePrice == null ? null
                                            : roomMarketValue / tradePrice);
                                    allocationPanel.marketValueField.setValue(roomMarketValue);
                                }
                            });
                        } finally {
                            ui.access(() -> {
                                event.getSource().setText(--numRemaining[0] + " to process");
                            });
                        }
                    }));
            // create a thread to reset the Room button label when finished
            new Thread(() -> {
                try {
                    future.get();
                    LOG.info("found room in {}/{} Portfolios in {} ms", allocationIndex[0] - 1,
                            numPortfolios, System.currentTimeMillis() - startTime);
                } catch (Exception e) {
                    // FIXME handle this
                    LOG.error("could not evaluate room for Portfolios", e);
                }
                ui.access(() -> {
                    event.getSource().setText("Room");
                    event.getSource().setEnabled(true);
                });
            }, "ui-room-button-resetter").start();
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

    /**
     * Calculates and displays the total market value of a trade when trade amount and price are
     * both specified.
     */
    protected void updateTradeMarketValue() {
        if (tradeAmount == null || tradePrice == null) {
            tradeMarketValue = null;
        } else {
            tradeMarketValue = tradeAmount * tradePrice;
        }

        tradeMarketValueField.setValue(tradeMarketValue);
    }
}