package com.example.application.ui;

import com.example.application.base.ui.component.ViewToolbar;
import com.example.application.service.PaymentAnalysisService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * View for generating reports and viewing statistics.
 */
@Route(value = "reports", layout = com.example.application.base.ui.MainAppLayout.class)
@PageTitle("Тайлан ба статистик")
public class ReportsView extends Main {

    private final PaymentAnalysisService paymentAnalysisService;
    private final Select<String> reportTypeSelect;
    private final ComboBox<String> buildingSelect;
    private final ComboBox<LocalDate> monthFilter;
    private final DatePicker monthPicker;
    private final ComboBox<BigDecimal> thresholdSelect;
    private final Div reportContent;

    public ReportsView(PaymentAnalysisService paymentAnalysisService) {
        this.paymentAnalysisService = paymentAnalysisService;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, 
                     LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM, 
                     LumoUtility.Gap.MEDIUM);

        // Report controls
        reportTypeSelect = new Select<>();
        reportTypeSelect.setLabel("Тайлангийн төрөл");
        reportTypeSelect.setItems("Байрны статистик", "Орцны статистик", "Өндөр эрсдэлтэй айлууд", "Сарын хураангуй");
        reportTypeSelect.setValue("Байрны статистик");
        reportTypeSelect.addValueChangeListener(event -> updateControlsVisibility());

        buildingSelect = new ComboBox<>("Байр");
        buildingSelect.setItems("71", "72", "73", "72А");
        buildingSelect.setVisible(false);

        monthFilter = new ComboBox<>("Сар");
        monthFilter.setItems(
            LocalDate.of(2024, 7, 1),
            LocalDate.of(2024, 8, 1),
            LocalDate.of(2024, 9, 1)
        );
        monthFilter.setItemLabelGenerator(date -> date.format(DateTimeFormatter.ofPattern("yyyy оны M сар")));
        monthFilter.setValue(LocalDate.of(2024, 9, 1)); // Default to September
        monthFilter.addValueChangeListener(event -> generateReport());

        monthPicker = new DatePicker("Сар");
        monthPicker.setValue(LocalDate.now().withDayOfMonth(1));

        thresholdSelect = new ComboBox<>("Босго (₮)");
        thresholdSelect.setItems(
            new BigDecimal("100000"),
            new BigDecimal("500000"),
            new BigDecimal("1000000"),
            new BigDecimal("2000000")
        );
        thresholdSelect.setItemLabelGenerator(this::formatCurrency);
        thresholdSelect.setValue(new BigDecimal("1000000"));
        thresholdSelect.setVisible(false);

        Button generateButton = new Button("Тайлан үүсгэх", event -> generateReport());
        generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button exportButton = new Button("Экспорт", event -> exportReport());
        exportButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        exportButton.setVisible(false); // TODO: Implement export functionality

        HorizontalLayout controlsLayout = new HorizontalLayout(
            reportTypeSelect, buildingSelect, monthFilter, monthPicker, thresholdSelect, generateButton, exportButton);
        controlsLayout.setAlignItems(HorizontalLayout.Alignment.END);

        // Report content area
        reportContent = new Div();
        reportContent.setSizeFull();

        add(controlsLayout);
        add(reportContent);

        updateControlsVisibility();
        generateReport(); // Generate default report
    }

    private void updateControlsVisibility() {
        String reportType = reportTypeSelect.getValue();
        
        buildingSelect.setVisible("Entrance Statistics".equals(reportType));
        thresholdSelect.setVisible("High Risk Households".equals(reportType));
    }

    private void generateReport() {
        reportContent.removeAll();
        
        String reportType = reportTypeSelect.getValue();
        
        switch (reportType) {
            case "Байрны статистик" -> generateBuildingStatisticsReport();
            case "Орцны статистик" -> generateEntranceStatisticsReport();
            case "Өндөр эрсдэлтэй айлууд" -> generateHighRiskHouseholdsReport();
            case "Сарын хураангуй" -> generateMonthlySummaryReport();
        }
    }

    private void generateBuildingStatisticsReport() {
        LocalDate selectedMonth = monthFilter.getValue();
        if (selectedMonth == null) {
            selectedMonth = LocalDate.of(2024, 9, 1);
        }

        H3 title = new H3("Байрны статистик - " + selectedMonth.format(DateTimeFormatter.ofPattern("yyyy оны M сар")));
        title.addClassNames(LumoUtility.Margin.Top.NONE);

        List<PaymentAnalysisService.BuildingStatistics> stats = paymentAnalysisService.getBuildingStatistics(selectedMonth);

        Grid<PaymentAnalysisService.BuildingStatistics> grid = new Grid<>(PaymentAnalysisService.BuildingStatistics.class, false);
        
        grid.addColumn(stat -> stat.getBuildingNumber() + " байр")
            .setHeader("Байр")
            .setSortable(true);

        grid.addColumn(PaymentAnalysisService.BuildingStatistics::getTotalHouseholds)
            .setHeader("Нийт айл")
            .setSortable(true);

        grid.addColumn(PaymentAnalysisService.BuildingStatistics::getHouseholdsWithDebt)
            .setHeader("Өртэй айл")
            .setSortable(true);

        grid.addColumn(stat -> formatCurrency(stat.getTotalOutstanding()))
            .setHeader("Нийт өр")
            .setSortable(true);

        grid.addColumn(stat -> formatCurrency(stat.getAverageDebt()))
            .setHeader("Дундаж өр")
            .setSortable(true);

        grid.setItems(stats);
        grid.setSizeFull();

        // Summary
        int totalHouseholds = stats.stream().mapToInt(PaymentAnalysisService.BuildingStatistics::getTotalHouseholds).sum();
        int totalWithDebt = stats.stream().mapToInt(PaymentAnalysisService.BuildingStatistics::getHouseholdsWithDebt).sum();
        BigDecimal grandTotal = stats.stream()
            .map(PaymentAnalysisService.BuildingStatistics::getTotalOutstanding)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Div summaryDiv = new Div();
        summaryDiv.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM, 
                                LumoUtility.BorderRadius.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);
        
        summaryDiv.add(new Paragraph("Нийт айл: " + totalHouseholds));
        summaryDiv.add(new Paragraph("Өртэй айл: " + totalWithDebt +
                                   " (" + String.format("%.1f%%", (double) totalWithDebt / totalHouseholds * 100) + ")"));
        summaryDiv.add(new Paragraph("Нийт өр: " + formatCurrency(grandTotal)));

        reportContent.add(title, summaryDiv, grid);
    }

    private void generateEntranceStatisticsReport() {
        String building = buildingSelect.getValue();
        if (building == null) {
            reportContent.add(new Paragraph("Please select a building."));
            return;
        }

        H3 title = new H3("Entrance Statistics Report - Building " + building);
        title.addClassNames(LumoUtility.Margin.Top.NONE);

        List<PaymentAnalysisService.EntranceStatistics> stats = paymentAnalysisService.getEntranceStatistics(building);

        Grid<PaymentAnalysisService.EntranceStatistics> grid = new Grid<>(PaymentAnalysisService.EntranceStatistics.class, false);
        
        grid.addColumn(PaymentAnalysisService.EntranceStatistics::getEntranceNumber)
            .setHeader("Entrance")
            .setSortable(true);
        
        grid.addColumn(PaymentAnalysisService.EntranceStatistics::getTotalHouseholds)
            .setHeader("Total Households")
            .setSortable(true);
        
        grid.addColumn(PaymentAnalysisService.EntranceStatistics::getHouseholdsWithDebt)
            .setHeader("Households with Debt")
            .setSortable(true);
        
        grid.addColumn(stat -> formatCurrency(stat.getTotalOutstanding()))
            .setHeader("Total Outstanding")
            .setSortable(true);

        grid.setItems(stats);
        grid.setSizeFull();

        reportContent.add(title, grid);
    }

    private void generateHighRiskHouseholdsReport() {
        LocalDate selectedMonth = monthFilter.getValue();
        if (selectedMonth == null) {
            selectedMonth = LocalDate.of(2024, 9, 1);
        }

        BigDecimal threshold = thresholdSelect.getValue();
        if (threshold == null) {
            threshold = new BigDecimal("1000000");
        }

        H3 title = new H3("Өндөр эрсдэлтэй айлууд - " + selectedMonth.format(DateTimeFormatter.ofPattern("yyyy оны M сар")));
        title.addClassNames(LumoUtility.Margin.Top.NONE);

        Paragraph subtitle = new Paragraph("Өрийн үлдэгдэл ≥ " + formatCurrency(threshold) + " байгаа айлууд");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY);

        List<PaymentAnalysisService.HouseholdPaymentInfo> households =
            paymentAnalysisService.getHouseholdsAboveThreshold(threshold, selectedMonth);

        Grid<PaymentAnalysisService.HouseholdPaymentInfo> grid = new Grid<>(PaymentAnalysisService.HouseholdPaymentInfo.class, false);
        
        grid.addColumn(PaymentAnalysisService.HouseholdPaymentInfo::getFullAddress)
            .setHeader("Address")
            .setSortable(true);
        
        grid.addColumn(info -> info.householdName)
            .setHeader("Household")
            .setSortable(true);
        
        grid.addColumn(info -> "Floor " + info.floorNumber)
            .setHeader("Floor")
            .setSortable(true);
        
        grid.addColumn(info -> formatCurrency(info.outstandingBalance))
            .setHeader("Outstanding Balance")
            .setSortable(true);
        
        grid.addColumn(info -> info.rankCategory)
            .setHeader("Risk Category")
            .setSortable(true);

        grid.setItems(households);
        grid.setSizeFull();

        // Summary
        Div summaryDiv = new Div();
        summaryDiv.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM, 
                                LumoUtility.BorderRadius.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);
        
        BigDecimal totalOutstanding = households.stream()
            .map(h -> h.outstandingBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summaryDiv.add(new Paragraph("Total High Risk Households: " + households.size()));
        summaryDiv.add(new Paragraph("Total Outstanding Amount: " + formatCurrency(totalOutstanding)));

        reportContent.add(title, subtitle, summaryDiv, grid);
    }

    private void generateMonthlySummaryReport() {
        H3 title = new H3("Monthly Summary Report");
        title.addClassNames(LumoUtility.Margin.Top.NONE);

        LocalDate selectedMonth = monthPicker.getValue();
        Paragraph subtitle = new Paragraph("Summary for " + selectedMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")));
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY);

        // Get categorized households
        var categorized = paymentAnalysisService.categorizeHouseholdsByRank();

        VerticalLayout summaryLayout = new VerticalLayout();
        summaryLayout.setPadding(false);
        summaryLayout.setSpacing(false);

        for (var entry : categorized.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                Div categoryDiv = new Div();
                categoryDiv.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM, 
                                         LumoUtility.BorderRadius.MEDIUM, LumoUtility.Margin.Bottom.SMALL);

                H4 categoryTitle = new H4(entry.getKey());
                categoryTitle.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.SMALL);

                BigDecimal categoryTotal = entry.getValue().stream()
                    .map(h -> h.outstandingBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                Paragraph categoryStats = new Paragraph(
                    "Households: " + entry.getValue().size() + 
                    " | Total Outstanding: " + formatCurrency(categoryTotal)
                );

                categoryDiv.add(categoryTitle, categoryStats);
                summaryLayout.add(categoryDiv);
            }
        }

        reportContent.add(title, subtitle, summaryLayout);
    }

    private void exportReport() {
        // TODO: Implement export functionality (CSV, Excel, PDF)
        // This would involve creating export services and file download functionality
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(amount) + " MNT";
    }
}
