package com.example.application.ui;

import com.example.application.base.ui.component.ViewToolbar;
import com.example.application.service.PaymentAnalysisService;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main dashboard showing payment statistics and household rankings.
 */
@Route(value = "dashboard", layout = com.example.application.base.ui.MainAppLayout.class)
@PageTitle("Хяналтын самбар")
public class DashboardView extends Main {

    private final PaymentAnalysisService paymentAnalysisService;
    private final VerticalLayout statisticsLayout;
    private final VerticalLayout rankingsLayout;
    private final Grid<PaymentAnalysisService.HouseholdPaymentInfo> householdGrid;

    public DashboardView(PaymentAnalysisService paymentAnalysisService) {
        this.paymentAnalysisService = paymentAnalysisService;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, 
                     LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM, 
                     LumoUtility.Gap.MEDIUM);

        // Statistics section
        statisticsLayout = new VerticalLayout();
        statisticsLayout.setPadding(false);
        statisticsLayout.setSpacing(false);

        // Rankings section
        rankingsLayout = new VerticalLayout();
        rankingsLayout.setPadding(false);
        rankingsLayout.setSpacing(false);

        // Household grid
        householdGrid = new Grid<>(PaymentAnalysisService.HouseholdPaymentInfo.class, false);
        setupHouseholdGrid();

        // Filter controls
        HorizontalLayout filterLayout = createFilterControls();

        add(filterLayout);
        add(createStatisticsSection());
        add(createRankingsSection());
        add(createHouseholdSection());

        loadData();
    }

    private HorizontalLayout createFilterControls() {
        Select<String> viewSelect = new Select<>();
        viewSelect.setLabel("Харах");
        viewSelect.setItems("Бүх айлууд", "Зэрэглэлээр", "Босгоос дээш");
        viewSelect.setValue("Зэрэглэлээр");
        viewSelect.addValueChangeListener(event -> updateView(event.getValue()));

        ComboBox<BigDecimal> thresholdCombo = new ComboBox<>("Босго (₮)");
        thresholdCombo.setItems(
            new BigDecimal("100000"),
            new BigDecimal("500000"),
            new BigDecimal("1000000"),
            new BigDecimal("2000000")
        );
        thresholdCombo.setItemLabelGenerator(this::formatCurrency);
        thresholdCombo.setValue(new BigDecimal("1000000"));
        thresholdCombo.addValueChangeListener(event -> {
            if ("Босгоос дээш".equals(viewSelect.getValue()) && event.getValue() != null) {
                loadHouseholdsAboveThreshold(event.getValue());
            }
        });

        HorizontalLayout filterLayout = new HorizontalLayout(viewSelect, thresholdCombo);
        filterLayout.setAlignItems(HorizontalLayout.Alignment.END);
        return filterLayout;
    }

    private Div createStatisticsSection() {
        Div section = new Div();
        section.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM, 
                             LumoUtility.BorderRadius.MEDIUM);

        H3 title = new H3("Байрны статистик");
        title.addClassNames(LumoUtility.Margin.Top.NONE);

        section.add(title, statisticsLayout);
        return section;
    }

    private Div createRankingsSection() {
        Div section = new Div();
        section.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM,
                             LumoUtility.BorderRadius.MEDIUM, LumoUtility.Margin.Top.MEDIUM);

        H3 title = new H3("Айлуудын зэрэглэл");
        title.addClassNames(LumoUtility.Margin.Top.NONE);

        section.add(title, rankingsLayout);
        return section;
    }

    private Div createHouseholdSection() {
        Div section = new Div();
        section.addClassNames(LumoUtility.Margin.Top.MEDIUM);

        H3 title = new H3("Айлуудын дэлгэрэнгүй");
        title.addClassNames(LumoUtility.Margin.Top.NONE);

        section.add(title, householdGrid);
        return section;
    }

    private void setupHouseholdGrid() {
        householdGrid.addColumn(PaymentAnalysisService.HouseholdPaymentInfo::getFullAddress)
            .setHeader("Хаяг")
            .setSortable(true);

        householdGrid.addColumn(info -> info.householdName)
            .setHeader("Айлын нэр")
            .setSortable(true);

        householdGrid.addColumn(info -> info.floorNumber + " давхар")
            .setHeader("Давхар")
            .setSortable(true);

        householdGrid.addColumn(info -> formatCurrency(info.outstandingBalance))
            .setHeader("Өрийн үлдэгдэл")
            .setSortable(true);

        householdGrid.addColumn(info -> info.rankCategory)
            .setHeader("Зэрэглэл")
            .setSortable(true);

        householdGrid.setSizeFull();
        householdGrid.setHeight("400px");
    }

    private void updateView(String viewType) {
        switch (viewType) {
            case "Бүх айлууд" -> loadAllHouseholds();
            case "Зэрэглэлээр" -> loadHouseholdsByRank();
            case "Босгоос дээш" -> loadHouseholdsAboveThreshold(new BigDecimal("1000000"));
        }
    }

    private void loadData() {
        loadBuildingStatistics();
        loadHouseholdsByRank();
    }

    private void loadBuildingStatistics() {
        statisticsLayout.removeAll();

        List<PaymentAnalysisService.BuildingStatistics> stats = paymentAnalysisService.getBuildingStatistics();

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.Padding.SMALL);
        headerLayout.add(
            createStatCell("Байр", "120px"),
            createStatCell("Нийт", "80px"),
            createStatCell("Өртэй", "80px"),
            createStatCell("Нийт өр", "150px"),
            createStatCell("Дундаж өр", "120px")
        );
        statisticsLayout.add(headerLayout);

        for (PaymentAnalysisService.BuildingStatistics stat : stats) {
            HorizontalLayout rowLayout = new HorizontalLayout();
            rowLayout.addClassNames(LumoUtility.Padding.SMALL);
            rowLayout.add(
                createStatCell(stat.getBuildingNumber() + " байр", "120px"),
                createStatCell(String.valueOf(stat.getTotalHouseholds()), "80px"),
                createStatCell(String.valueOf(stat.getHouseholdsWithDebt()), "80px"),
                createStatCell(formatCurrency(stat.getTotalOutstanding()), "150px"),
                createStatCell(formatCurrency(stat.getAverageDebt()), "120px")
            );
            statisticsLayout.add(rowLayout);
        }
    }

    private void loadHouseholdsByRank() {
        rankingsLayout.removeAll();
        
        Map<String, List<PaymentAnalysisService.HouseholdPaymentInfo>> categorized = 
            paymentAnalysisService.categorizeHouseholdsByRank();

        for (Map.Entry<String, List<PaymentAnalysisService.HouseholdPaymentInfo>> entry : categorized.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                H4 categoryTitle = new H4(entry.getKey() + " (" + entry.getValue().size() + " айл)");
                categoryTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL);
                
                if ("Хувалз".equals(entry.getKey())) {
                    categoryTitle.addClassNames(LumoUtility.TextColor.ERROR);
                }
                
                rankingsLayout.add(categoryTitle);

                // Show top 5 households in each category
                List<PaymentAnalysisService.HouseholdPaymentInfo> topHouseholds = 
                    entry.getValue().stream().limit(5).toList();

                for (PaymentAnalysisService.HouseholdPaymentInfo household : topHouseholds) {
                    Paragraph householdInfo = new Paragraph(
                        household.getFullAddress() + " - " + household.householdName + 
                        ": " + formatCurrency(household.outstandingBalance)
                    );
                    householdInfo.addClassNames(LumoUtility.Margin.Left.MEDIUM, LumoUtility.FontSize.SMALL);
                    rankingsLayout.add(householdInfo);
                }

                if (entry.getValue().size() > 5) {
                    Paragraph moreInfo = new Paragraph("... болон " + (entry.getValue().size() - 5) + " айл");
                    moreInfo.addClassNames(LumoUtility.Margin.Left.MEDIUM, LumoUtility.FontSize.SMALL,
                                         LumoUtility.TextColor.SECONDARY);
                    rankingsLayout.add(moreInfo);
                }
            }
        }

        // Update grid with all households
        List<PaymentAnalysisService.HouseholdPaymentInfo> allHouseholds = categorized.values().stream()
            .flatMap(List::stream)
            .sorted((a, b) -> b.outstandingBalance.compareTo(a.outstandingBalance))
            .toList();
        householdGrid.setItems(allHouseholds);
    }

    private void loadAllHouseholds() {
        Map<String, List<PaymentAnalysisService.HouseholdPaymentInfo>> categorized = 
            paymentAnalysisService.categorizeHouseholdsByRank();
        
        List<PaymentAnalysisService.HouseholdPaymentInfo> allHouseholds = categorized.values().stream()
            .flatMap(List::stream)
            .sorted((a, b) -> b.outstandingBalance.compareTo(a.outstandingBalance))
            .toList();
        
        householdGrid.setItems(allHouseholds);
    }

    private void loadHouseholdsAboveThreshold(BigDecimal threshold) {
        List<PaymentAnalysisService.HouseholdPaymentInfo> households = 
            paymentAnalysisService.getHouseholdsAboveThreshold(threshold);
        householdGrid.setItems(households);
    }

    private Div createStatCell(String text, String width) {
        Div cell = new Div(new Span(text));
        cell.setWidth(width);
        return cell;
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(amount) + " MNT";
    }
}
