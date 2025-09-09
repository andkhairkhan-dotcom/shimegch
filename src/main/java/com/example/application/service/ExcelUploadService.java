package com.example.application.service;

import com.example.application.domain.*;
import com.example.application.entity.UploadHistory;
import com.example.application.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for processing Excel file uploads containing payment balance data.
 */
@Service
public class ExcelUploadService {

    private final HouseholdRepository householdRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final ApartmentRepository apartmentRepository;
    private final UploadHistoryRepository uploadHistoryRepository;

    public ExcelUploadService(HouseholdRepository householdRepository,
                            PaymentRecordRepository paymentRecordRepository,
                            ApartmentRepository apartmentRepository,
                            UploadHistoryRepository uploadHistoryRepository) {
        this.householdRepository = householdRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.apartmentRepository = apartmentRepository;
        this.uploadHistoryRepository = uploadHistoryRepository;
    }

    @Transactional
    public UploadResult processExcelFile(InputStream inputStream, LocalDate recordMonth,
                                       String fileName, Long fileSize, String uploadedBy) {
        // Create upload history record
        UploadHistory uploadHistory = new UploadHistory(fileName, fileSize, uploadedBy);
        uploadHistory.setUploadStatus(UploadHistory.UploadStatus.PROCESSING);

        // Store file content for potential re-processing
        try {
            byte[] fileContent = inputStream.readAllBytes();
            uploadHistory.setFileContent(fileContent);
            uploadHistory = uploadHistoryRepository.save(uploadHistory);

            // Reset input stream for processing
            inputStream = new java.io.ByteArrayInputStream(fileContent);
        } catch (Exception e) {
            uploadHistory.markAsFailed("Файл уншихад алдаа гарлаа: " + e.getMessage());
            uploadHistoryRepository.save(uploadHistory);
            return new UploadResult(false, 0, 0, List.of("Файл уншихад алдаа гарлаа"), List.of());
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int processedRecords = 0;
        int updatedRecords = 0;

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                try {
                    PaymentRecordData recordData = parseRow(row, rowIndex + 1);
                    if (recordData != null) {
                        boolean updated = processPaymentRecord(recordData, recordMonth);
                        processedRecords++;
                        if (updated) updatedRecords++;
                    }
                } catch (Exception e) {
                    errors.add("Row " + (rowIndex + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Failed to process Excel file: " + e.getMessage());
            uploadHistory.markAsFailed("Excel файл боловсруулахад алдаа гарлаа: " + e.getMessage());
            uploadHistoryRepository.save(uploadHistory);
            return new UploadResult(false, processedRecords, updatedRecords, errors, warnings);
        }

        // Update upload history with results
        uploadHistory.setTotalRecords(processedRecords + errors.size());
        uploadHistory.updateProgress(processedRecords, errors.size());

        if (errors.isEmpty()) {
            uploadHistory.markAsCompleted(String.format("Нийт %d бичлэг боловсруулагдлаа, %d шинэчлэгдлээ",
                                                       processedRecords, updatedRecords));
        } else if (processedRecords > 0) {
            uploadHistory.markAsPartiallyCompleted(
                String.format("Нийт %d бичлэг боловсруулагдлаа, %d шинэчлэгдлээ", processedRecords, updatedRecords),
                String.format("%d алдаа гарлаа", errors.size()));
        } else {
            uploadHistory.markAsFailed("Ямар ч бичлэг боловсруулагдсангүй");
        }

        uploadHistoryRepository.save(uploadHistory);
        return new UploadResult(errors.isEmpty(), processedRecords, updatedRecords, errors, warnings);
    }

    private PaymentRecordData parseRow(Row row, int rowNumber) {
        // Expected columns: Building, Entrance, Door, Household Name, Outstanding Balance
        Cell buildingCell = row.getCell(0);
        Cell entranceCell = row.getCell(1);
        Cell doorCell = row.getCell(2);
        Cell householdCell = row.getCell(3);
        Cell balanceCell = row.getCell(4);

        if (buildingCell == null || entranceCell == null || doorCell == null || balanceCell == null) {
            return null; // Skip empty rows
        }

        String building = getCellValueAsString(buildingCell);
        Integer entrance = getCellValueAsInteger(entranceCell);
        Integer door = getCellValueAsInteger(doorCell);
        String householdName = getCellValueAsString(householdCell);
        BigDecimal balance = getCellValueAsBigDecimal(balanceCell);

        if (building == null || entrance == null || door == null || balance == null) {
            throw new RuntimeException("Missing required data in row " + rowNumber);
        }

        return new PaymentRecordData(building, entrance, door, householdName, balance);
    }

    private boolean processPaymentRecord(PaymentRecordData data, LocalDate recordMonth) {
        // Find or create apartment
        Optional<Apartment> apartmentOpt = apartmentRepository.findByBuildingEntranceAndDoor(
            data.building, data.entrance, data.door);
        
        if (apartmentOpt.isEmpty()) {
            throw new RuntimeException("Apartment not found: " + data.building + "-" + data.entrance + "-" + data.door);
        }

        Apartment apartment = apartmentOpt.get();
        
        // Find or create household
        Household household = householdRepository.findByApartment(apartment)
            .orElseGet(() -> {
                Household newHousehold = new Household(
                    data.householdName != null ? data.householdName : "Unknown", 
                    apartment);
                return householdRepository.save(newHousehold);
            });

        // Update household name if provided
        if (data.householdName != null && !data.householdName.trim().isEmpty()) {
            household.setHouseholdName(data.householdName);
            householdRepository.save(household);
        }

        // Create or update payment record
        Optional<PaymentRecord> existingRecord = paymentRecordRepository
            .findByHouseholdAndRecordMonth(household, recordMonth);

        if (existingRecord.isPresent()) {
            PaymentRecord record = existingRecord.get();
            record.setOutstandingBalance(data.balance);
            record.setUploadDate(LocalDate.now());
            paymentRecordRepository.save(record);
            return true; // Updated existing record
        } else {
            PaymentRecord newRecord = new PaymentRecord(household, recordMonth, data.balance);
            paymentRecordRepository.save(newRecord);
            return false; // Created new record
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                try {
                    yield new BigDecimal(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    public static class PaymentRecordData {
        public final String building;
        public final Integer entrance;
        public final Integer door;
        public final String householdName;
        public final BigDecimal balance;

        public PaymentRecordData(String building, Integer entrance, Integer door, 
                               String householdName, BigDecimal balance) {
            this.building = building;
            this.entrance = entrance;
            this.door = door;
            this.householdName = householdName;
            this.balance = balance;
        }
    }

    public static class UploadResult {
        public final boolean success;
        public final int processedRecords;
        public final int updatedRecords;
        public final List<String> errors;
        public final List<String> warnings;

        public UploadResult(boolean success, int processedRecords, int updatedRecords,
                          List<String> errors, List<String> warnings) {
            this.success = success;
            this.processedRecords = processedRecords;
            this.updatedRecords = updatedRecords;
            this.errors = errors;
            this.warnings = warnings;
        }

        public UploadResult(int processedRecords, int updatedRecords,
                          List<String> errors, List<String> warnings) {
            this(errors.isEmpty(), processedRecords, updatedRecords, errors, warnings);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean isSuccessful() {
            return !hasErrors() && processedRecords > 0;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
