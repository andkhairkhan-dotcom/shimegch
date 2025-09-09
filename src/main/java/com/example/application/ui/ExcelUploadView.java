package com.example.application.ui;

import com.example.application.base.ui.component.ViewToolbar;
import com.example.application.service.ExcelUploadService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.LocalDate;

/**
 * View for uploading Excel files containing payment balance data.
 */
@Route(value = "excel-upload", layout = com.example.application.base.ui.MainAppLayout.class)
@PageTitle("Excel файл оруулах")
public class ExcelUploadView extends Main {

    private final ExcelUploadService excelUploadService;
    private final DatePicker recordMonthPicker;
    private final Upload upload;
    private final Div resultDiv;

    public ExcelUploadView(ExcelUploadService excelUploadService) {
        this.excelUploadService = excelUploadService;

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, 
                     LumoUtility.FlexDirection.COLUMN, LumoUtility.Padding.MEDIUM, 
                     LumoUtility.Gap.MEDIUM);

        // Month selection
        recordMonthPicker = new DatePicker("Тайлангийн сар");
        recordMonthPicker.setValue(LocalDate.now().withDayOfMonth(1)); // First day of current month
        recordMonthPicker.setHelperText("Төлбөрийн мэдээлэл хамаарах сарыг сонгоно уу");

        // File upload
        MemoryBuffer buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".xlsx", ".xls");
        upload.setMaxFiles(1);
        upload.setDropLabel(new Paragraph("Excel файлыг энд тавих эсвэл товшиж оруулах"));

        // Result display
        resultDiv = new Div();
        resultDiv.addClassNames(LumoUtility.Margin.Top.MEDIUM);

        // Upload handler
        upload.addSucceededListener(event -> {
            try {
                LocalDate recordMonth = recordMonthPicker.getValue();
                if (recordMonth == null) {
                    Notification.show("Тайлангийн сарыг сонгоно уу", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                // Get file information
                String fileName = event.getFileName();
                long fileSize = buffer.getFileData().getFile().length();
                String uploadedBy = "Admin"; // TODO: Get from security context

                ExcelUploadService.UploadResult result = excelUploadService.processExcelFile(
                    buffer.getInputStream(), recordMonth, fileName, fileSize, uploadedBy);

                displayResult(result);

                if (result.isSuccess()) {
                    Notification.show("Файл амжилттай оруулагдлаа!", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    Notification.show("Файл оруулахад алдаа гарлаа", 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }

            } catch (Exception e) {
                Notification.show("Файл оруулах үед алдаа: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFailedListener(event -> {
            Notification.show("Файл оруулах амжилтгүй: " + event.getReason().getMessage(),
                            5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // Instructions
        Div instructionsDiv = createInstructions();

        add(recordMonthPicker);
        add(upload);
        add(instructionsDiv);
        add(resultDiv);
    }

    private Div createInstructions() {
        Div instructionsDiv = new Div();
        instructionsDiv.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM, 
                                    LumoUtility.BorderRadius.MEDIUM);

        H3 title = new H3("Excel файлын формат");
        title.addClassNames(LumoUtility.Margin.Top.NONE);

        Paragraph instructions = new Paragraph(
            "Excel файл дараах багануудыг дарааллаар агуулсан байх ёстой:\n" +
            "1. Байрны дугаар (71, 72, 73, 72А)\n" +
            "2. Орцны дугаар (1, 2, 3)\n" +
            "3. Хаалганы дугаар (1-80)\n" +
            "4. Айлын нэр (заавал биш)\n" +
            "5. Өрийн үлдэгдэл (төгрөгөөр)\n\n" +
            "Эхний мөр толгой байх ба боловсруулахад алгасагдана."
        );
        instructions.getStyle().set("white-space", "pre-line");

        instructionsDiv.add(title, instructions);
        return instructionsDiv;
    }

    private void displayResult(ExcelUploadService.UploadResult result) {
        resultDiv.removeAll();

        VerticalLayout resultLayout = new VerticalLayout();
        resultLayout.setPadding(false);
        resultLayout.setSpacing(false);

        // Summary
        H3 summaryTitle = new H3("Оруулалтын үр дүн");
        Paragraph summary = new Paragraph(String.format(
            "Боловсруулсан: %d бичлэг, Шинэчилсэн: %d одоо байгаа бичлэг",
            result.processedRecords, result.updatedRecords));

        resultLayout.add(summaryTitle, summary);

        // Errors
        if (!result.errors.isEmpty()) {
            H3 errorsTitle = new H3("Алдаанууд (" + result.errors.size() + ")");
            errorsTitle.addClassNames(LumoUtility.TextColor.ERROR);
            resultLayout.add(errorsTitle);

            for (String error : result.errors) {
                Paragraph errorParagraph = new Paragraph(error);
                errorParagraph.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.FontSize.SMALL);
                resultLayout.add(errorParagraph);
            }
        }

        // Warnings
        if (!result.warnings.isEmpty()) {
            H3 warningsTitle = new H3("Анхааруулгууд (" + result.warnings.size() + ")");
            warningsTitle.addClassNames(LumoUtility.TextColor.WARNING);
            resultLayout.add(warningsTitle);

            for (String warning : result.warnings) {
                Paragraph warningParagraph = new Paragraph(warning);
                warningParagraph.addClassNames(LumoUtility.TextColor.WARNING, LumoUtility.FontSize.SMALL);
                resultLayout.add(warningParagraph);
            }
        }

        resultDiv.add(resultLayout);
    }
}
