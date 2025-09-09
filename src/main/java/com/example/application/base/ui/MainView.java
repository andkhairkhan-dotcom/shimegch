package com.example.application.base.ui;

import com.example.application.base.ui.component.ViewToolbar;
import com.example.application.domain.*;
import com.example.application.repository.*;
import com.example.application.service.PaymentAnalysisService;
import com.example.application.service.ConfigurationService;
import com.example.application.service.PosterGeneratorService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This view shows up when a user navigates to the root ('/') of the application.
 */
@Route(value = "", layout = MainAppLayout.class)
@PageTitle("Айлуудын жагсаалт")
public final class MainView extends Main {

    private final PaymentAnalysisService paymentAnalysisService;
    private final BuildingRepository buildingRepository;
    private final EntranceRepository entranceRepository;
    private final RankConfigurationRepository rankConfigurationRepository;
    private final ConfigurationService configurationService;
    private final PosterGeneratorService posterGeneratorService;

    private final ComboBox<Building> buildingFilter;
    private final ComboBox<Entrance> entranceFilter;
    private final ComboBox<Integer> floorFilter;
    private final ComboBox<String> rankFilter;
    private final ComboBox<LocalDate> monthFilter;
    private final NumberField minAmountFilter;
    private final NumberField maxAmountFilter;
    private final Grid<PaymentAnalysisService.HouseholdPaymentInfo> householdGrid;

    MainView(PaymentAnalysisService paymentAnalysisService,
             BuildingRepository buildingRepository,
             EntranceRepository entranceRepository,
             RankConfigurationRepository rankConfigurationRepository,
             ConfigurationService configurationService,
             PosterGeneratorService posterGeneratorService) {
        this.paymentAnalysisService = paymentAnalysisService;
        this.buildingRepository = buildingRepository;
        this.entranceRepository = entranceRepository;
        this.rankConfigurationRepository = rankConfigurationRepository;
        this.configurationService = configurationService;
        this.posterGeneratorService = posterGeneratorService;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX,
                     LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM,
                     LumoUtility.Gap.MEDIUM);

        // Filters
        buildingFilter = new ComboBox<>("Байр");
        buildingFilter.setItemLabelGenerator(building -> building.getBuildingNumber() + " байр");
        buildingFilter.addValueChangeListener(event -> {
            updateEntranceFilter(event.getValue());
            updateFloorFilter();
            filterHouseholds();
        });

        entranceFilter = new ComboBox<>("Орц");
        entranceFilter.setItemLabelGenerator(entrance -> entrance.getEntranceNumber() + " орц");
        entranceFilter.addValueChangeListener(event -> {
            updateFloorFilter();
            filterHouseholds();
        });

        floorFilter = new ComboBox<>("Давхар");
        floorFilter.setItemLabelGenerator(floor -> floor + " давхар");
        floorFilter.addValueChangeListener(event -> filterHouseholds());

        rankFilter = new ComboBox<>("Өрийн зэрэглэл");
        rankFilter.setItems("Хувалз", "Өндөр эрсдэлтэй", "Дунд эрсдэлтэй", "Normal");
        rankFilter.addValueChangeListener(event -> filterHouseholds());

        monthFilter = new ComboBox<>("Сар");
        monthFilter.setItems(
            LocalDate.of(2024, 7, 1),
            LocalDate.of(2024, 8, 1),
            LocalDate.of(2024, 9, 1)
        );
        monthFilter.setItemLabelGenerator(date -> date.format(DateTimeFormatter.ofPattern("yyyy оны M сар")));
        monthFilter.setValue(LocalDate.of(2024, 9, 1)); // Default to September
        monthFilter.addValueChangeListener(event -> filterHouseholds());

        minAmountFilter = new NumberField("Хамгийн бага өр (₮)");
        minAmountFilter.addValueChangeListener(event -> filterHouseholds());

        maxAmountFilter = new NumberField("Хамгийн их өр (₮)");
        maxAmountFilter.addValueChangeListener(event -> filterHouseholds());

        // Filter button and active filters display
        HorizontalLayout filterControlLayout = createFilterControlLayout();

        // Grid
        householdGrid = new Grid<>(PaymentAnalysisService.HouseholdPaymentInfo.class, false);
        setupHouseholdGrid();

        add(filterControlLayout);
        add(householdGrid);

        loadData();
    }

    private HorizontalLayout createFilterControlLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.addClassNames("filter-control-layout");

        // Filter button
        Button filterButton = new Button("🔍 Шүүлтүүр", event -> openFilterModal());
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterButton.addClassNames("filter-button");

        // Active filters display
        Div activeFiltersDiv = new Div();
        activeFiltersDiv.addClassNames("active-filters");
        updateActiveFiltersDisplay(activeFiltersDiv);

        layout.add(filterButton, activeFiltersDiv);
        return layout;
    }

    private void updateActiveFiltersDisplay(Div activeFiltersDiv) {
        activeFiltersDiv.removeAll();

        List<String> activeFilters = new ArrayList<>();

        if (buildingFilter.getValue() != null) {
            try {
                activeFilters.add("Байр: " + buildingFilter.getValue().getBuildingNumber() + " байр");
            } catch (Exception e) {
                activeFilters.add("Байр: " + buildingFilter.getValue().toString());
            }
        }
        if (entranceFilter.getValue() != null) {
            try {
                activeFilters.add("Орц: " + entranceFilter.getValue().getEntranceNumber() + " орц");
            } catch (Exception e) {
                activeFilters.add("Орц: " + entranceFilter.getValue().toString());
            }
        }
        if (floorFilter.getValue() != null) {
            activeFilters.add("Давхар: " + floorFilter.getValue() + " давхар");
        }
        if (rankFilter.getValue() != null) {
            activeFilters.add("Зэрэглэл: " + rankFilter.getValue());
        }
        if (monthFilter.getValue() != null) {
            activeFilters.add("Сар: " + monthFilter.getValue().getMonthValue() + "/" + monthFilter.getValue().getYear());
        }
        if (minAmountFilter.getValue() != null) {
            activeFilters.add("Хамгийн бага: " + formatCurrency(BigDecimal.valueOf(minAmountFilter.getValue())));
        }
        if (maxAmountFilter.getValue() != null) {
            activeFilters.add("Хамгийн их: " + formatCurrency(BigDecimal.valueOf(maxAmountFilter.getValue())));
        }

        if (activeFilters.isEmpty()) {
            Span noFilters = new Span("Шүүлтүүр байхгүй");
            noFilters.addClassNames(LumoUtility.TextColor.SECONDARY);
            activeFiltersDiv.add(noFilters);
        } else {
            for (int i = 0; i < activeFilters.size(); i++) {
                Span filterSpan = new Span(activeFilters.get(i));
                filterSpan.addClassNames(LumoUtility.Background.PRIMARY_10,
                                       LumoUtility.Padding.Horizontal.SMALL,
                                       LumoUtility.Padding.Vertical.XSMALL,
                                       LumoUtility.BorderRadius.SMALL,
                                       LumoUtility.FontSize.SMALL);
                activeFiltersDiv.add(filterSpan);

                if (i < activeFilters.size() - 1) {
                    activeFiltersDiv.add(new Span(" "));
                }
            }
        }
    }

    private void openFilterModal() {
        Dialog filterDialog = new Dialog();
        filterDialog.setHeaderTitle("🔍 Шүүлтүүр");
        filterDialog.setWidth("min(95vw, 400px)");
        filterDialog.setHeight("min(70vh, 500px)");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassNames(LumoUtility.Gap.SMALL);

        // Add all filter components with compact layout
        H5 locationHeader = new H5("Байршил");
        locationHeader.addClassNames(LumoUtility.Margin.Bottom.XSMALL, LumoUtility.Margin.Top.SMALL);

        H5 debtHeader = new H5("Өрийн мэдээлэл");
        debtHeader.addClassNames(LumoUtility.Margin.Bottom.XSMALL, LumoUtility.Margin.Top.MEDIUM);

        content.add(
            locationHeader,
            buildingFilter,
            entranceFilter,
            floorFilter,
            debtHeader,
            rankFilter,
            monthFilter,
            minAmountFilter,
            maxAmountFilter
        );

        // Buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button clearButton = new Button("Цэвэрлэх", event -> {
            clearAllFilters();
            filterDialog.close();
        });
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button applyButton = new Button("Хэрэглэх", event -> {
            filterHouseholds();
            updateActiveFiltersDisplay((Div) getChildren()
                .filter(component -> component.getClass().equals(HorizontalLayout.class))
                .findFirst()
                .map(layout -> ((HorizontalLayout) layout).getChildren()
                    .filter(child -> child.getClass().equals(Div.class))
                    .findFirst().orElse(null))
                .orElse(null));
            filterDialog.close();
        });
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        buttonLayout.add(clearButton, applyButton);
        content.add(buttonLayout);

        filterDialog.add(content);
        filterDialog.open();
    }

    private void clearAllFilters() {
        buildingFilter.clear();
        entranceFilter.clear();
        floorFilter.clear();
        rankFilter.clear();
        monthFilter.clear();
        minAmountFilter.clear();
        maxAmountFilter.clear();
        filterHouseholds();
    }

    private void setupHouseholdGrid() {
        // Configure grid columns with responsive design
        // Combined address column
        householdGrid.addColumn(info -> String.format("%s байр, %s орц, %s давхар, %s тоот",
                                                     info.buildingNumber,
                                                     info.entranceNumber,
                                                     info.floorNumber,
                                                     info.doorNumber))
            .setHeader("Хаяг")
            .setSortable(true)
            .setFlexGrow(2)
            .setWidth("200px");



        householdGrid.addColumn(info -> formatCurrency(info.outstandingBalance))
            .setHeader("Өрийн хэмжээ")
            .setSortable(true)
            .setFlexGrow(1)
            .setWidth("120px");

        householdGrid.addComponentColumn(this::createRankCell)
            .setHeader("Өрийн зэрэглэл")
            .setSortable(true)
            .setFlexGrow(1)
            .setWidth("120px");

        householdGrid.setSizeFull();

        // Make grid responsive
        householdGrid.addClassNames("responsive-grid");
        householdGrid.getStyle().set("--lumo-font-size-s", "0.8rem");

        // Add click listener to open modal
        householdGrid.addItemClickListener(event -> {
            if (event.getItem() != null) {
                openHouseholdDetailModal(event.getItem());
            }
        });
    }

    private void loadData() {
        buildingFilter.setItems(buildingRepository.findAll());
        filterHouseholds();
    }



    private void updateEntranceFilter(Building building) {
        if (building != null) {
            entranceFilter.setItems(entranceRepository.findByBuildingOrderByEntranceNumber(building));
        } else {
            entranceFilter.clear();
            entranceFilter.setItems();
        }
    }

    private void updateFloorFilter() {
        // Generate floor options 1-16
        List<Integer> floors = java.util.stream.IntStream.rangeClosed(1, 16)
            .boxed()
            .collect(Collectors.toList());
        floorFilter.setItems(floors);
    }

    private void filterHouseholds() {
        LocalDate selectedMonth = monthFilter.getValue();
        if (selectedMonth == null) {
            selectedMonth = LocalDate.of(2024, 9, 1); // Default to September
        }

        var categorized = paymentAnalysisService.categorizeHouseholdsByRank(selectedMonth);
        List<PaymentAnalysisService.HouseholdPaymentInfo> allHouseholds = categorized.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());

        // Apply filters
        List<PaymentAnalysisService.HouseholdPaymentInfo> filteredHouseholds = allHouseholds.stream()
            .filter(household -> {
                // Building filter
                if (buildingFilter.getValue() != null &&
                    !household.buildingNumber.equals(buildingFilter.getValue().getBuildingNumber())) {
                    return false;
                }

                // Entrance filter
                if (entranceFilter.getValue() != null &&
                    !household.entranceNumber.equals(entranceFilter.getValue().getEntranceNumber())) {
                    return false;
                }

                // Floor filter
                if (floorFilter.getValue() != null &&
                    !household.floorNumber.equals(floorFilter.getValue())) {
                    return false;
                }

                // Rank filter
                if (rankFilter.getValue() != null &&
                    !household.rankCategory.equals(rankFilter.getValue())) {
                    return false;
                }

                // Amount filters
                if (minAmountFilter.getValue() != null &&
                    household.outstandingBalance.compareTo(BigDecimal.valueOf(minAmountFilter.getValue())) < 0) {
                    return false;
                }

                if (maxAmountFilter.getValue() != null &&
                    household.outstandingBalance.compareTo(BigDecimal.valueOf(maxAmountFilter.getValue())) > 0) {
                    return false;
                }

                return true;
            })
            .sorted((a, b) -> {
                // Sort by building, then entrance, then door number
                int buildingCompare = a.buildingNumber.compareTo(b.buildingNumber);
                if (buildingCompare != 0) return buildingCompare;

                int entranceCompare = a.entranceNumber.compareTo(b.entranceNumber);
                if (entranceCompare != 0) return entranceCompare;

                return a.doorNumber.compareTo(b.doorNumber);
            })
            .collect(Collectors.toList());

        householdGrid.setItems(filteredHouseholds);

        // Update active filters display
        updateActiveFiltersInLayout();
    }

    private void updateActiveFiltersInLayout() {
        // Find the active filters div and update it
        getChildren()
            .filter(component -> component instanceof HorizontalLayout)
            .map(layout -> (HorizontalLayout) layout)
            .filter(layout -> layout.getClassName().contains("filter-control-layout"))
            .findFirst()
            .ifPresent(layout -> {
                layout.getChildren()
                    .filter(child -> child instanceof Div)
                    .map(div -> (Div) div)
                    .filter(div -> div.getClassName().contains("active-filters"))
                    .findFirst()
                    .ifPresent(this::updateActiveFiltersDisplay);
            });
    }

    private void openHouseholdDetailModal(PaymentAnalysisService.HouseholdPaymentInfo householdInfo) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("🏠 " + householdInfo.householdName);

        // Make dialog responsive but simpler
        dialog.setWidth("min(90vw, 800px)");
        dialog.setHeight("min(85vh, 600px)");
        dialog.setResizable(true);

        // Create simple vertical layout
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setSizeFull();

        // Header with household info - simplified
        createSimpleHouseholdHeader(content, householdInfo);

        // Payment history section
        createPaymentHistorySection(content, householdInfo);

        // Chart section
        createChartSection(content, householdInfo);

        // Add poster generation button
        createPosterButton(content, householdInfo);

        dialog.add(content);
        dialog.open();
    }

    private void createSimpleHouseholdHeader(VerticalLayout content, PaymentAnalysisService.HouseholdPaymentInfo householdInfo) {
        // Simple header card
        Div headerCard = new Div();
        headerCard.addClassNames(LumoUtility.Background.PRIMARY_10, LumoUtility.Padding.MEDIUM,
                               LumoUtility.BorderRadius.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);

        // Address
        H3 addressTitle = new H3("📍 " + householdInfo.getFullAddress());
        addressTitle.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        addressTitle.getStyle().set("margin-bottom", "10px");

        // Floor info
        Paragraph floorInfo = new Paragraph("🏢 " + householdInfo.floorNumber + " давхар");
        floorInfo.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.MEDIUM);
        floorInfo.getStyle().set("margin-bottom", "15px");

        // Debt amount
        H2 debtAmount = new H2("💰 " + formatCurrency(householdInfo.outstandingBalance));
        debtAmount.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.LARGE);
        String rankColor = getRankColor(householdInfo.rankCategory);
        debtAmount.getStyle().set("color", rankColor);
        debtAmount.getStyle().set("margin-bottom", "10px");

        // Rank badge
        Div rankBadge = new Div();
        rankBadge.addClassNames(LumoUtility.Padding.Horizontal.SMALL, LumoUtility.Padding.Vertical.XSMALL,
                               LumoUtility.BorderRadius.MEDIUM, LumoUtility.FontWeight.BOLD);
        rankBadge.getStyle().set("background-color", rankColor);
        rankBadge.getStyle().set("color", "white");
        rankBadge.getStyle().set("display", "inline-block");
        rankBadge.add(new Span("🏷️ " + householdInfo.rankCategory));

        headerCard.add(addressTitle, floorInfo, debtAmount, rankBadge);
        content.add(headerCard);
    }





    private void createPaymentHistorySection(VerticalLayout content, PaymentAnalysisService.HouseholdPaymentInfo householdInfo) {
        Div historyDiv = new Div();
        historyDiv.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.LARGE,
                                LumoUtility.BorderRadius.MEDIUM);
        historyDiv.setSizeFull();

        H3 historyTitle = new H3("📊 Төлбөрийн түүх");
        historyTitle.addClassNames(LumoUtility.Margin.Top.NONE);

        // Get payment history for this household (last 6 months)
        List<PaymentAnalysisService.PaymentHistoryInfo> paymentHistory =
            paymentAnalysisService.getHouseholdPaymentHistory(householdInfo.householdId);

        if (paymentHistory.isEmpty()) {
            Paragraph noData = new Paragraph("Төлбөрийн түүх олдсонгүй.");
            noData.addClassNames(LumoUtility.TextColor.SECONDARY);
            historyDiv.add(historyTitle, noData);
        } else {
            // Create payment history grid
            Grid<PaymentAnalysisService.PaymentHistoryInfo> historyGrid = new Grid<>();
            historyGrid.setItems(paymentHistory);
            historyGrid.setSizeFull();
            historyGrid.setHeight("400px");
            historyGrid.addClassNames("payment-history-grid");

            historyGrid.addColumn(history -> history.getMonth().format(DateTimeFormatter.ofPattern("yyyy оны M сар")))
                .setHeader("Сар")
                .setSortable(true)
                .setFlexGrow(1);

            historyGrid.addColumn(history -> formatCurrency(history.getOutstandingBalance()))
                .setHeader("Өрийн үлдэгдэл")
                .setSortable(true)
                .setFlexGrow(1);

            historyGrid.addComponentColumn(this::createHistoryRankCell)
                .setHeader("Зэрэглэл")
                .setSortable(true)
                .setFlexGrow(1);

            // Calculate change from previous month
            historyGrid.addComponentColumn(history -> createChangeIndicator(history, paymentHistory))
                .setHeader("Өөрчлөлт")
                .setSortable(false)
                .setFlexGrow(1);

            historyDiv.add(historyTitle, historyGrid);
        }

        content.add(historyDiv);
    }

    private void createChartSection(VerticalLayout content, PaymentAnalysisService.HouseholdPaymentInfo householdInfo) {
        Div chartDiv = new Div();
        chartDiv.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.LARGE,
                              LumoUtility.BorderRadius.MEDIUM);
        chartDiv.setSizeFull();

        H3 chartTitle = new H3("📈 Өрийн өсөлт/бууралт");
        chartTitle.addClassNames(LumoUtility.Margin.Top.NONE);

        // Get payment history for chart (last 6 months)
        List<PaymentAnalysisService.PaymentHistoryInfo> paymentHistory =
            paymentAnalysisService.getHouseholdPaymentHistory(householdInfo.householdId);

        // Limit to last 6 months
        List<PaymentAnalysisService.PaymentHistoryInfo> recentHistory = paymentHistory.stream()
            .sorted((a, b) -> b.getMonth().compareTo(a.getMonth())) // Sort by month descending
            .limit(6)
            .sorted((a, b) -> a.getMonth().compareTo(b.getMonth())) // Sort back to ascending
            .collect(Collectors.toList());

        if (recentHistory.isEmpty()) {
            Paragraph noData = new Paragraph("📊 График үүсгэх мэдээлэл олдсонгүй");
            noData.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.LARGE);
            chartDiv.add(chartTitle, noData);
        } else {
            // Create simple text-based chart visualization
            Div chartContainer = createSimpleChart(recentHistory);
            chartDiv.add(chartTitle, chartContainer);
        }

        content.add(chartDiv);
    }

    private Div createRankCell(PaymentAnalysisService.HouseholdPaymentInfo householdInfo) {
        Div rankDiv = new Div();
        rankDiv.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Gap.SMALL);

        // Find rank configuration for color
        String colorCode = getRankColor(householdInfo.rankCategory);

        // Color indicator
        Div colorIndicator = new Div();
        colorIndicator.setWidth("12px");
        colorIndicator.setHeight("12px");
        colorIndicator.addClassNames("rank-indicator");
        colorIndicator.getStyle().set("border-radius", "50%");
        colorIndicator.getStyle().set("background-color", colorCode);
        colorIndicator.getStyle().set("border", "1px solid #ccc");

        // Rank text
        Span rankText = new Span(householdInfo.rankCategory);
        rankText.getStyle().set("color", colorCode);
        rankText.getStyle().set("font-weight", "bold");

        rankDiv.add(colorIndicator, rankText);
        return rankDiv;
    }

    private String getRankColor(String rankName) {
        return rankConfigurationRepository.findAll().stream()
            .filter(rank -> rank.getRankName().equals(rankName))
            .map(RankConfiguration::getColorCode)
            .filter(color -> color != null && !color.isEmpty())
            .findFirst()
            .orElse("#666666"); // Default gray color
    }

    private Div createHistoryRankCell(PaymentAnalysisService.PaymentHistoryInfo historyInfo) {
        Div rankDiv = new Div();
        rankDiv.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Gap.SMALL);

        // Find rank configuration for color
        String colorCode = getRankColor(historyInfo.getRankCategory());

        // Color indicator
        Div colorIndicator = new Div();
        colorIndicator.setWidth("8px");
        colorIndicator.setHeight("8px");
        colorIndicator.getStyle().set("border-radius", "50%");
        colorIndicator.getStyle().set("background-color", colorCode);

        // Rank text
        Span rankText = new Span(historyInfo.getRankCategory());
        rankText.getStyle().set("color", colorCode);
        rankText.getStyle().set("font-weight", "bold");
        rankText.getStyle().set("font-size", "0.8em");

        rankDiv.add(colorIndicator, rankText);
        return rankDiv;
    }

    private Div createChangeIndicator(PaymentAnalysisService.PaymentHistoryInfo current,
                                     List<PaymentAnalysisService.PaymentHistoryInfo> allHistory) {
        Div changeDiv = new Div();
        changeDiv.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Gap.SMALL);

        // Find previous month's data
        int currentIndex = allHistory.indexOf(current);
        if (currentIndex > 0) {
            PaymentAnalysisService.PaymentHistoryInfo previous = allHistory.get(currentIndex - 1);
            BigDecimal change = current.getOutstandingBalance().subtract(previous.getOutstandingBalance());

            String changeText;
            String color;
            String icon;

            if (change.compareTo(BigDecimal.ZERO) > 0) {
                // Increased debt (bad)
                changeText = "+" + formatCurrency(change);
                color = "#ff4444";
                icon = "📈";
            } else if (change.compareTo(BigDecimal.ZERO) < 0) {
                // Decreased debt (good)
                changeText = formatCurrency(change);
                color = "#44aa44";
                icon = "📉";
            } else {
                // No change
                changeText = "Өөрчлөлт байхгүй";
                color = "#666666";
                icon = "➖";
            }

            Span iconSpan = new Span(icon);
            Span changeSpan = new Span(changeText);
            changeSpan.getStyle().set("color", color);
            changeSpan.getStyle().set("font-weight", "bold");
            changeSpan.getStyle().set("font-size", "0.8em");

            changeDiv.add(iconSpan, changeSpan);
        } else {
            Span noChangeSpan = new Span("Эхний сар");
            noChangeSpan.addClassNames(LumoUtility.TextColor.SECONDARY);
            noChangeSpan.getStyle().set("font-size", "0.8em");
            changeDiv.add(noChangeSpan);
        }

        return changeDiv;
    }

    private Div createSimpleChart(List<PaymentAnalysisService.PaymentHistoryInfo> paymentHistory) {
        Div chartContainer = new Div();
        chartContainer.setSizeFull();
        chartContainer.setHeight("400px");
        chartContainer.addClassNames(LumoUtility.Background.CONTRAST_10, LumoUtility.BorderRadius.MEDIUM,
                                    LumoUtility.Padding.MEDIUM, "chart-container");

        // Find min and max values for scaling
        BigDecimal minAmount = paymentHistory.stream()
            .map(PaymentAnalysisService.PaymentHistoryInfo::getOutstandingBalance)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        BigDecimal maxAmount = paymentHistory.stream()
            .map(PaymentAnalysisService.PaymentHistoryInfo::getOutstandingBalance)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        // Create horizontal layout for chart bars
        HorizontalLayout chartLayout = new HorizontalLayout();
        chartLayout.setWidthFull();
        chartLayout.setHeight("320px");
        chartLayout.setAlignItems(HorizontalLayout.Alignment.END);
        chartLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.EVENLY);
        chartLayout.setPadding(true);
        chartLayout.setSpacing(true);

        for (PaymentAnalysisService.PaymentHistoryInfo history : paymentHistory) {
            VerticalLayout barContainer = new VerticalLayout();
            barContainer.setPadding(false);
            barContainer.setSpacing(false);
            barContainer.setAlignItems(HorizontalLayout.Alignment.CENTER);
            barContainer.setFlexGrow(1);

            // Calculate bar height (percentage of max)
            double percentage = maxAmount.compareTo(BigDecimal.ZERO) > 0 ?
                history.getOutstandingBalance().divide(maxAmount, 4, RoundingMode.HALF_UP).doubleValue() : 0;
            int barHeight = Math.max(20, (int) (percentage * 250)); // Min 20px, max 250px

            // Create bar
            Div bar = new Div();
            bar.setWidth("60px");
            bar.setHeight(barHeight + "px");
            bar.addClassNames("chart-bar");
            bar.getStyle().set("background-color", getRankColor(history.getRankCategory()));
            bar.getStyle().set("border-radius", "6px 6px 0 0");
            bar.getStyle().set("margin-bottom", "8px");
            bar.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

            // Amount label (on top of bar)
            Span amountLabel = new Span(formatCurrency(history.getOutstandingBalance()));
            amountLabel.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.BOLD, "chart-label");
            amountLabel.getStyle().set("color", getRankColor(history.getRankCategory()));
            amountLabel.getStyle().set("margin-bottom", "4px");

            // Month label (bottom)
            Span monthLabel = new Span(history.getMonth().format(DateTimeFormatter.ofPattern("yyyy/M")));
            monthLabel.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY, "chart-label");
            monthLabel.getStyle().set("margin-top", "4px");

            // Rank label (below month)
            Span rankLabel = new Span(history.getRankCategory());
            rankLabel.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.FontWeight.BOLD, "chart-label");
            rankLabel.getStyle().set("color", getRankColor(history.getRankCategory()));

            barContainer.add(amountLabel, bar, monthLabel, rankLabel);
            chartLayout.add(barContainer);
        }

        chartContainer.add(chartLayout);
        return chartContainer;
    }

    private void createPosterButton(VerticalLayout content, PaymentAnalysisService.HouseholdPaymentInfo householdInfo) {
        // Add some spacing before the button
        Div spacer = new Div();
        spacer.getStyle().set("height", "20px");
        content.add(spacer);

        Button posterButton = new Button("📄 Постер үүсгэх", event -> generatePoster(householdInfo));
        posterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        posterButton.addClassNames(LumoUtility.Width.FULL);
        posterButton.getStyle().set("font-size", "16px");
        posterButton.getStyle().set("padding", "12px 24px");
        posterButton.getStyle().set("margin-top", "10px");

        content.add(posterButton);
    }

    private void generatePoster(PaymentAnalysisService.HouseholdPaymentInfo householdInfo) {
        try {
            byte[] pdfBytes = posterGeneratorService.generatePoster(
                householdInfo.buildingNumber,
                String.valueOf(householdInfo.entranceNumber),
                String.valueOf(householdInfo.floorNumber),
                String.valueOf(householdInfo.doorNumber),
                householdInfo.rankCategory,
                householdInfo.outstandingBalance
            );

            // Create filename
            String filename = String.format("poster_%s-%s-%s-%s.pdf",
                householdInfo.buildingNumber,
                householdInfo.entranceNumber,
                householdInfo.floorNumber,
                householdInfo.doorNumber);

            // Create download using anchor element
            getUI().ifPresent(ui -> {
                // Convert bytes to base64
                String base64 = java.util.Base64.getEncoder().encodeToString(pdfBytes);
                String dataUrl = "data:application/pdf;base64," + base64;

                // Create and trigger download using JavaScript
                ui.getPage().executeJs(
                    "const link = document.createElement('a');" +
                    "link.href = $0;" +
                    "link.download = $1;" +
                    "document.body.appendChild(link);" +
                    "link.click();" +
                    "document.body.removeChild(link);",
                    dataUrl, filename
                );
            });

            // Show success notification
            com.vaadin.flow.component.notification.Notification.show(
                "Постер амжилттай үүсгэгдлээ!",
                3000,
                com.vaadin.flow.component.notification.Notification.Position.MIDDLE
            );

        } catch (Exception e) {
            // Show error notification
            com.vaadin.flow.component.notification.Notification.show(
                "Постер үүсгэхэд алдаа гарлаа: " + e.getMessage(),
                3000,
                com.vaadin.flow.component.notification.Notification.Position.MIDDLE
            );
        }
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("mn", "MN"));
        return formatter.format(amount) + " ₮";
    }
}
