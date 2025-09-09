package com.example.application.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing Excel upload history and data
 */
@Entity
@Table(name = "upload_history")
public class UploadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "processed_records")
    private Integer processedRecords;

    @Column(name = "failed_records")
    private Integer failedRecords;

    @Column(name = "upload_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UploadStatus uploadStatus;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Lob
    @Column(name = "file_content")
    private byte[] fileContent;

    @Column(name = "processing_summary", length = 2000)
    private String processingSummary;

    public enum UploadStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        PARTIALLY_COMPLETED
    }

    // Constructors
    public UploadHistory() {}

    public UploadHistory(String fileName, Long fileSize, String uploadedBy) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.uploadDate = LocalDateTime.now();
        this.uploadStatus = UploadStatus.PENDING;
        this.totalRecords = 0;
        this.processedRecords = 0;
        this.failedRecords = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(Integer processedRecords) {
        this.processedRecords = processedRecords;
    }

    public Integer getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(Integer failedRecords) {
        this.failedRecords = failedRecords;
    }

    public UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getProcessingSummary() {
        return processingSummary;
    }

    public void setProcessingSummary(String processingSummary) {
        this.processingSummary = processingSummary;
    }

    // Helper methods
    public void markAsCompleted(String summary) {
        this.uploadStatus = UploadStatus.COMPLETED;
        this.processingSummary = summary;
    }

    public void markAsFailed(String errorMessage) {
        this.uploadStatus = UploadStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markAsPartiallyCompleted(String summary, String errorMessage) {
        this.uploadStatus = UploadStatus.PARTIALLY_COMPLETED;
        this.processingSummary = summary;
        this.errorMessage = errorMessage;
    }

    public void updateProgress(int processed, int failed) {
        this.processedRecords = processed;
        this.failedRecords = failed;
    }

    public String getStatusDisplay() {
        switch (uploadStatus) {
            case PENDING: return "Хүлээгдэж байна";
            case PROCESSING: return "Боловсруулж байна";
            case COMPLETED: return "Амжилттай";
            case FAILED: return "Алдаатай";
            case PARTIALLY_COMPLETED: return "Хэсэгчлэн амжилттай";
            default: return uploadStatus.toString();
        }
    }

    public String getFormattedFileSize() {
        if (fileSize == null) return "Тодорхойгүй";
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
}
