package com.example.application.ui;

import com.example.application.base.ui.component.ViewToolbar;
import com.example.application.domain.*;
import com.example.application.repository.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * View for managing individual households and viewing their payment history.
 */
@Route(value = "households", layout = com.example.application.base.ui.MainAppLayout.class)
@PageTitle("Айлуудын удирдлага")
public class HouseholdManagementView extends Main {

    private final HouseholdRepository householdRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final BuildingRepository buildingRepository;
    private final EntranceRepository entranceRepository;
    private final ApartmentRepository apartmentRepository;

    private final ComboBox<Building> buildingFilter;
    private final ComboBox<Entrance> entranceFilter;
    private final TextField searchField;
    private final Grid<Household> householdGrid;
    private final Div detailsPanel;

    public HouseholdManagementView(HouseholdRepository householdRepository,
                                 PaymentRecordRepository paymentRecordRepository,
                                 BuildingRepository buildingRepository,
                                 EntranceRepository entranceRepository,
                                 ApartmentRepository apartmentRepository) {
        this.householdRepository = householdRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.buildingRepository = buildingRepository;
        this.entranceRepository = entranceRepository;
        this.apartmentRepository = apartmentRepository;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, 
                     LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM, 
                     LumoUtility.Gap.MEDIUM);

        // Filters
        buildingFilter = new ComboBox<>("Building");
        buildingFilter.setItemLabelGenerator(building -> "Building " + building.getBuildingNumber());
        buildingFilter.addValueChangeListener(event -> {
            updateEntranceFilter(event.getValue());
            filterHouseholds();
        });

        entranceFilter = new ComboBox<>("Entrance");
        entranceFilter.setItemLabelGenerator(entrance -> "Entrance " + entrance.getEntranceNumber());
        entranceFilter.addValueChangeListener(event -> filterHouseholds());

        searchField = new TextField("Search");
        searchField.setPlaceholder("Search by household name...");
        searchField.addValueChangeListener(event -> filterHouseholds());

        Button addHouseholdBtn = new Button("Add Household", event -> openHouseholdDialog(null));
        addHouseholdBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout filterLayout = new HorizontalLayout(buildingFilter, entranceFilter, searchField, addHouseholdBtn);
        filterLayout.setAlignItems(HorizontalLayout.Alignment.END);

        // Grid
        householdGrid = new Grid<>(Household.class, false);
        setupHouseholdGrid();

        // Details panel
        detailsPanel = new Div();
        detailsPanel.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM, 
                                  LumoUtility.BorderRadius.MEDIUM);
        detailsPanel.setVisible(false);

        // Layout
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.add(householdGrid, detailsPanel);
        householdGrid.setWidth("60%");
        detailsPanel.setWidth("40%");

        add(filterLayout);
        add(mainLayout);

        loadData();
    }

    private void setupHouseholdGrid() {
        householdGrid.addColumn(household -> household.getApartment().getFullIdentifier())
            .setHeader("Address")
            .setSortable(true);

        householdGrid.addColumn(Household::getHouseholdName)
            .setHeader("Household Name")
            .setSortable(true);

        householdGrid.addColumn(household -> "Floor " + household.getApartment().getFloorNumber())
            .setHeader("Floor")
            .setSortable(true);

        householdGrid.addComponentColumn(this::createActionButtons)
            .setHeader("Actions")
            .setFlexGrow(0);

        householdGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                showHouseholdDetails(event.getValue());
            } else {
                detailsPanel.setVisible(false);
            }
        });

        householdGrid.setSizeFull();
    }

    private HorizontalLayout createActionButtons(Household household) {
        Button editButton = new Button("Edit", event -> openHouseholdDialog(household));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        Button viewButton = new Button("View", event -> {
            householdGrid.select(household);
            showHouseholdDetails(household);
        });
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        return new HorizontalLayout(editButton, viewButton);
    }

    private void openHouseholdDialog(Household household) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(household == null ? "Add New Household" : "Edit Household");
        dialog.setWidth("500px");

        // Form fields
        TextField nameField = new TextField("Household Name");
        nameField.setRequired(true);
        nameField.setMaxLength(200);

        ComboBox<Building> buildingCombo = new ComboBox<>("Building");
        buildingCombo.setItems(buildingRepository.findAll());
        buildingCombo.setItemLabelGenerator(building -> "Building " + building.getBuildingNumber());
        buildingCombo.setRequired(true);

        ComboBox<Entrance> entranceCombo = new ComboBox<>("Entrance");
        entranceCombo.setItemLabelGenerator(entrance -> "Entrance " + entrance.getEntranceNumber());
        entranceCombo.setRequired(true);

        ComboBox<Apartment> apartmentCombo = new ComboBox<>("Apartment");
        apartmentCombo.setItemLabelGenerator(apartment -> "Door " + apartment.getDoorNumber() + 
                                           " (Floor " + apartment.getFloorNumber() + ")");
        apartmentCombo.setRequired(true);

        TextArea contactField = new TextArea("Contact Information");
        contactField.setMaxLength(500);

        // Cascade filters
        buildingCombo.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                entranceCombo.setItems(entranceRepository.findByBuildingOrderByEntranceNumber(event.getValue()));
                entranceCombo.clear();
                apartmentCombo.clear();
            }
        });

        entranceCombo.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                apartmentCombo.setItems(apartmentRepository.findByEntranceOrderByDoorNumber(event.getValue()));
                apartmentCombo.clear();
            }
        });

        // Populate fields if editing
        if (household != null) {
            nameField.setValue(household.getHouseholdName());
            contactField.setValue(household.getContactInfo() != null ? household.getContactInfo() : "");
            
            Building building = household.getApartment().getEntrance().getBuilding();
            Entrance entrance = household.getApartment().getEntrance();
            
            buildingCombo.setValue(building);
            entranceCombo.setItems(entranceRepository.findByBuildingOrderByEntranceNumber(building));
            entranceCombo.setValue(entrance);
            apartmentCombo.setItems(apartmentRepository.findByEntranceOrderByDoorNumber(entrance));
            apartmentCombo.setValue(household.getApartment());
        }

        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, buildingCombo, entranceCombo, apartmentCombo, contactField);

        // Buttons
        Button saveButton = new Button("Save", event -> {
            if (saveHousehold(household, nameField.getValue(), apartmentCombo.getValue(), 
                            contactField.getValue())) {
                dialog.close();
                loadData();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

        dialog.add(formLayout, buttonLayout);
        dialog.open();
    }

    private boolean saveHousehold(Household existingHousehold, String name, Apartment apartment, String contact) {
        if (name == null || name.trim().isEmpty()) {
            Notification.show("Household name is required", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (apartment == null) {
            Notification.show("Please select an apartment", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        // Check if apartment is already occupied (unless editing the same household)
        Optional<Household> existingInApartment = householdRepository.findByApartment(apartment);
        if (existingInApartment.isPresent() && 
            (existingHousehold == null || !existingInApartment.get().getId().equals(existingHousehold.getId()))) {
            Notification.show("This apartment is already occupied by another household", 
                            3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        try {
            Household household;
            if (existingHousehold == null) {
                household = new Household(name.trim(), apartment);
            } else {
                household = existingHousehold;
                household.setHouseholdName(name.trim());
                household.setApartment(apartment);
            }

            household.setContactInfo(contact != null && !contact.trim().isEmpty() ? contact.trim() : null);

            householdRepository.save(household);

            Notification.show(existingHousehold == null ? "Household created successfully" : "Household updated successfully", 
                            3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;

        } catch (Exception e) {
            Notification.show("Error saving household: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
    }

    private void showHouseholdDetails(Household household) {
        detailsPanel.removeAll();
        detailsPanel.setVisible(true);

        H3 title = new H3("Household Details");
        title.addClassNames(LumoUtility.Margin.Top.NONE);

        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);

        // Basic info
        detailsLayout.add(new Paragraph("Name: " + household.getHouseholdName()));
        detailsLayout.add(new Paragraph("Address: " + household.getApartment().getFullIdentifier()));
        detailsLayout.add(new Paragraph("Floor: " + household.getApartment().getFloorNumber()));
        
        if (household.getContactInfo() != null) {
            detailsLayout.add(new Paragraph("Contact: " + household.getContactInfo()));
        }

        // Payment history
        H3 paymentTitle = new H3("Payment History");
        List<PaymentRecord> paymentHistory = paymentRecordRepository.findByHousehold(household);

        if (paymentHistory.isEmpty()) {
            detailsLayout.add(new Paragraph("No payment records found."));
        } else {
            Grid<PaymentRecord> paymentGrid = new Grid<>(PaymentRecord.class, false);
            paymentGrid.addColumn(record -> record.getRecordMonth().format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .setHeader("Month");
            paymentGrid.addColumn(record -> formatCurrency(record.getOutstandingBalance()))
                .setHeader("Outstanding Balance");
            paymentGrid.addColumn(record -> record.getUploadDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .setHeader("Upload Date");
            
            paymentGrid.setItems(paymentHistory);
            paymentGrid.setHeight("300px");
            detailsLayout.add(paymentGrid);
        }

        detailsPanel.add(title, detailsLayout, paymentTitle);
        if (!paymentHistory.isEmpty()) {
            Grid<PaymentRecord> paymentGrid = new Grid<>(PaymentRecord.class, false);
            paymentGrid.addColumn(record -> record.getRecordMonth().format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .setHeader("Month");
            paymentGrid.addColumn(record -> formatCurrency(record.getOutstandingBalance()))
                .setHeader("Outstanding Balance");
            paymentGrid.addColumn(record -> record.getUploadDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .setHeader("Upload Date");
            
            paymentGrid.setItems(paymentHistory);
            paymentGrid.setHeight("300px");
            detailsPanel.add(paymentGrid);
        }
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

    private void filterHouseholds() {
        List<Household> households;

        if (buildingFilter.getValue() != null && entranceFilter.getValue() != null) {
            households = householdRepository.findByBuildingAndEntrance(
                buildingFilter.getValue().getBuildingNumber(),
                entranceFilter.getValue().getEntranceNumber());
        } else if (buildingFilter.getValue() != null) {
            households = householdRepository.findByBuildingNumber(
                buildingFilter.getValue().getBuildingNumber());
        } else {
            households = householdRepository.findAll();
        }

        // Apply search filter
        String searchTerm = searchField.getValue();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            households = households.stream()
                .filter(h -> h.getHouseholdName().toLowerCase().contains(searchTerm.toLowerCase()))
                .toList();
        }

        householdGrid.setItems(households);
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(amount) + " MNT";
    }
}
