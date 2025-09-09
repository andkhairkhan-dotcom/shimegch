package com.example.application.ui;

import com.example.application.base.ui.component.ViewToolbar;
import com.example.application.domain.RankConfiguration;
import com.example.application.entity.MemeConfiguration;
import com.example.application.repository.RankConfigurationRepository;
import com.example.application.repository.MemeConfigurationRepository;
import com.example.application.service.ConfigurationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.List;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Admin view for configuring payment threshold ranks.
 */
@Route(value = "admin-config", layout = com.example.application.base.ui.MainAppLayout.class)
@PageTitle("Админ тохиргоо")
public class AdminConfigView extends Main {

    private final RankConfigurationRepository rankConfigurationRepository;
    private final MemeConfigurationRepository memeConfigurationRepository;
    private final ConfigurationService configurationService;
    private final Grid<RankConfiguration> grid;

    public AdminConfigView(RankConfigurationRepository rankConfigurationRepository,
                          MemeConfigurationRepository memeConfigurationRepository,
                          ConfigurationService configurationService) {
        this.rankConfigurationRepository = rankConfigurationRepository;
        this.memeConfigurationRepository = memeConfigurationRepository;
        this.configurationService = configurationService;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, 
                     LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM, 
                     LumoUtility.Gap.MEDIUM);

        // Top bar configuration section
        createTopBarConfigSection();

        // Create toolbar with add button
        Button addButton = new Button("Зэрэглэл нэмэх", event -> openRankDialog(null));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Create grid
        grid = new Grid<>(RankConfiguration.class, false);
        setupGrid();
        refreshGrid();

        HorizontalLayout toolbar = new HorizontalLayout(addButton);
        toolbar.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        toolbar.setWidthFull();

        add(toolbar);
        add(grid);

        // Add meme configuration section
        createMemeConfigSection();
    }

    private void setupGrid() {
        grid.addColumn(RankConfiguration::getRankName)
            .setHeader("Зэрэглэлийн нэр")
            .setSortable(true);

        grid.addColumn(rank -> formatCurrency(rank.getThresholdAmount()))
            .setHeader("Босгын дүн")
            .setSortable(true);

        grid.addColumn(RankConfiguration::getDescription)
            .setHeader("Тайлбар")
            .setFlexGrow(1);

        grid.addComponentColumn(this::createColorCell)
            .setHeader("Өнгө")
            .setFlexGrow(0);

        grid.addColumn(rank -> rank.getIsActive() ? "Идэвхтэй" : "Идэвхгүй")
            .setHeader("Төлөв")
            .setSortable(true);

        grid.addComponentColumn(this::createActionButtons)
            .setHeader("Үйлдлүүд")
            .setFlexGrow(0);

        grid.setSizeFull();
    }

    private HorizontalLayout createActionButtons(RankConfiguration rank) {
        Button editButton = new Button("Засах", event -> openRankDialog(rank));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        Button toggleButton = new Button(rank.getIsActive() ? "Идэвхгүй болгох" : "Идэвхжүүлэх",
            event -> toggleRankStatus(rank));
        toggleButton.addThemeVariants(ButtonVariant.LUMO_SMALL,
            rank.getIsActive() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS);

        Button deleteButton = new Button("Устгах", event -> deleteRank(rank));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

        HorizontalLayout actions = new HorizontalLayout(editButton, toggleButton, deleteButton);
        actions.setSpacing(true);
        return actions;
    }

    private Div createColorCell(RankConfiguration rank) {
        Div colorDiv = new Div();
        colorDiv.setWidth("30px");
        colorDiv.setHeight("20px");
        colorDiv.getStyle().set("border-radius", "4px");
        colorDiv.getStyle().set("border", "1px solid #ccc");

        if (rank.getColorCode() != null) {
            colorDiv.getStyle().set("background-color", rank.getColorCode());
        } else {
            colorDiv.getStyle().set("background-color", "#f0f0f0");
        }

        return colorDiv;
    }

    private void openRankDialog(RankConfiguration rank) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(rank == null ? "Add New Rank" : "Edit Rank");
        dialog.setWidth("400px");

        // Form fields
        TextField nameField = new TextField("Rank Name");
        nameField.setRequired(true);
        nameField.setMaxLength(100);

        BigDecimalField thresholdField = new BigDecimalField("Threshold Amount (MNT)");
        thresholdField.setRequired(true);

        TextArea descriptionField = new TextArea("Тайлбар");
        descriptionField.setMaxLength(500);

        TextField colorField = new TextField("Өнгөний код");
        colorField.setPlaceholder("#FF0000");
        colorField.setHelperText("Hex өнгөний код оруулна уу (жишээ: #FF0000)");
        colorField.setMaxLength(7);

        Checkbox activeCheckbox = new Checkbox("Идэвхтэй");
        activeCheckbox.setValue(true);

        // Populate fields if editing
        if (rank != null) {
            nameField.setValue(rank.getRankName());
            thresholdField.setValue(rank.getThresholdAmount());
            descriptionField.setValue(rank.getDescription() != null ? rank.getDescription() : "");
            colorField.setValue(rank.getColorCode() != null ? rank.getColorCode() : "");
            activeCheckbox.setValue(rank.getIsActive());
        }

        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, thresholdField, descriptionField, colorField, activeCheckbox);

        // Buttons
        Button saveButton = new Button("Хадгалах", event -> {
            if (saveRank(rank, nameField.getValue(), thresholdField.getValue(),
                        descriptionField.getValue(), colorField.getValue(), activeCheckbox.getValue())) {
                dialog.close();
                refreshGrid();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Цуцлах", event -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

        dialog.add(formLayout, buttonLayout);
        dialog.open();
    }

    private boolean saveRank(RankConfiguration existingRank, String name, BigDecimal threshold,
                           String description, String colorCode, Boolean isActive) {
        if (name == null || name.trim().isEmpty()) {
            Notification.show("Rank name is required", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        if (threshold == null || threshold.compareTo(BigDecimal.ZERO) < 0) {
            Notification.show("Valid threshold amount is required", 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }

        try {
            RankConfiguration rank;
            if (existingRank == null) {
                rank = new RankConfiguration(name.trim(), threshold);
            } else {
                rank = existingRank;
                rank.setRankName(name.trim());
                rank.setThresholdAmount(threshold);
            }

            rank.setDescription(description != null && !description.trim().isEmpty() ?
                              description.trim() : null);
            rank.setColorCode(colorCode != null && !colorCode.trim().isEmpty() ?
                            colorCode.trim() : null);
            rank.setIsActive(isActive != null ? isActive : true);

            rankConfigurationRepository.save(rank);

            Notification.show(existingRank == null ? "Rank created successfully" : "Rank updated successfully", 
                            3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            return true;

        } catch (Exception e) {
            Notification.show("Error saving rank: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
    }

    private void toggleRankStatus(RankConfiguration rank) {
        try {
            rank.setIsActive(!rank.getIsActive());
            rankConfigurationRepository.save(rank);
            refreshGrid();

            Notification.show("Rank status updated", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show("Error updating rank status: " + e.getMessage(), 
                            5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteRank(RankConfiguration rank) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirm Delete");
        confirmDialog.add("Are you sure you want to delete the rank '" + rank.getRankName() + "'?");

        Button confirmButton = new Button("Delete", event -> {
            try {
                rankConfigurationRepository.delete(rank);
                refreshGrid();
                confirmDialog.close();

                Notification.show("Rank deleted successfully", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (Exception e) {
                Notification.show("Error deleting rank: " + e.getMessage(), 
                                5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancel", event -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

        confirmDialog.add(buttonLayout);
        confirmDialog.open();
    }

    private void refreshGrid() {
        grid.setItems(rankConfigurationRepository.findAll());
    }

    private void createTopBarConfigSection() {
        VerticalLayout topBarSection = new VerticalLayout();
        topBarSection.setPadding(false);
        topBarSection.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM,
                                   LumoUtility.BorderRadius.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);

        com.vaadin.flow.component.html.H3 title = new com.vaadin.flow.component.html.H3("Top Bar тохиргоо");
        title.addClassNames(LumoUtility.Margin.Top.NONE);

        TextField topBarTextField = new TextField("Top Bar текст");
        topBarTextField.setWidth("100%");
        topBarTextField.setValue(configurationService.getTopBarText());
        topBarTextField.setHelperText("Энэ текст нь хуудасны дээд хэсэгт харагдана");

        Button saveButton = new Button("Хадгалах", event -> {
            try {
                configurationService.setTopBarText(topBarTextField.getValue());
                Notification.show("Top Bar текст амжилттай хадгалагдлаа!", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception e) {
                Notification.show("Алдаа гарлаа: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton);
        buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

        topBarSection.add(title, topBarTextField, buttonLayout);
        add(topBarSection);
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(amount) + " MNT";
    }

    private void createMemeConfigSection() {
        H3 memeTitle = new H3("🎭 Meme тохиргоо");
        memeTitle.addClassNames(LumoUtility.Margin.Top.LARGE);

        // Meme images section
        H4 imageTitle = new H4("Зургууд (URL)");
        TextArea imageTextArea = new TextArea();
        imageTextArea.setPlaceholder("Meme зургийн URL-ууд (мөр бүрт нэг URL)");
        imageTextArea.setWidthFull();
        imageTextArea.setHeight("150px");

        // Load existing image URLs
        List<MemeConfiguration> images = memeConfigurationRepository.findByMemeTypeAndIsActiveTrueOrderByDisplayOrder(MemeConfiguration.MemeType.IMAGE_URL);
        imageTextArea.setValue(images.stream().map(MemeConfiguration::getContent).collect(java.util.stream.Collectors.joining("\n")));

        Button saveImagesButton = new Button("Зургууд хадгалах", event -> {
            saveMemeConfigurations(imageTextArea.getValue(), MemeConfiguration.MemeType.IMAGE_URL);
            com.vaadin.flow.component.notification.Notification.show("Зургууд амжилттай хадгалагдлаа!", 3000, com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
        });
        saveImagesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Meme texts section
        H4 textTitle = new H4("Хошин текстүүд");
        TextArea textTextArea = new TextArea();
        textTextArea.setPlaceholder("Хошин текстүүд (мөр бүрт нэг текст)");
        textTextArea.setWidthFull();
        textTextArea.setHeight("150px");

        // Load existing texts
        List<MemeConfiguration> texts = memeConfigurationRepository.findByMemeTypeAndIsActiveTrueOrderByDisplayOrder(MemeConfiguration.MemeType.TEXT);
        textTextArea.setValue(texts.stream().map(MemeConfiguration::getContent).collect(java.util.stream.Collectors.joining("\n")));

        Button saveTextsButton = new Button("Текстүүд хадгалах", event -> {
            saveMemeConfigurations(textTextArea.getValue(), MemeConfiguration.MemeType.TEXT);
            com.vaadin.flow.component.notification.Notification.show("Текстүүд амжилттай хадгалагдлаа!", 3000, com.vaadin.flow.component.notification.Notification.Position.MIDDLE);
        });
        saveTextsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Example section
        Div exampleDiv = new Div();
        exampleDiv.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.MEDIUM);
        exampleDiv.add(new H5("Жишээ:"));
        exampleDiv.add(new Paragraph("Зургийн URL: https://example.com/meme1.jpg"));
        exampleDiv.add(new Paragraph("Хошин текст: Өрөө төлөхгүй бол интернэт тасална! 😄"));
        exampleDiv.add(new Paragraph("Хошин текст: Шимэгчлэхээ болиод, өрөө төлөөрэй! 🤣"));

        add(memeTitle, imageTitle, imageTextArea, saveImagesButton, textTitle, textTextArea, saveTextsButton, exampleDiv);
    }

    private void saveMemeConfigurations(String content, MemeConfiguration.MemeType memeType) {
        // Delete existing configurations of this type
        List<MemeConfiguration> existing = memeConfigurationRepository.findByMemeTypeAndIsActiveTrueOrderByDisplayOrder(memeType);
        existing.forEach(config -> config.setIsActive(false));
        memeConfigurationRepository.saveAll(existing);

        // Save new configurations
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                MemeConfiguration config = new MemeConfiguration(memeType, line);
                config.setDisplayOrder(i + 1);
                memeConfigurationRepository.save(config);
            }
        }
    }
}
