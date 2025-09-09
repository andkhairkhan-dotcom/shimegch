package com.example.application.repository;

import com.example.application.entity.UploadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UploadHistoryRepository extends JpaRepository<UploadHistory, Long> {

    /**
     * Find all uploads ordered by upload date descending
     */
    List<UploadHistory> findAllByOrderByUploadDateDesc();

    /**
     * Find uploads by status
     */
    List<UploadHistory> findByUploadStatusOrderByUploadDateDesc(UploadHistory.UploadStatus status);

    /**
     * Find uploads by date range
     */
    @Query("SELECT u FROM UploadHistory u WHERE u.uploadDate BETWEEN :startDate AND :endDate ORDER BY u.uploadDate DESC")
    List<UploadHistory> findByUploadDateBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Find uploads by uploaded by user
     */
    List<UploadHistory> findByUploadedByOrderByUploadDateDesc(String uploadedBy);

    /**
     * Find recent uploads (last 30 days)
     */
    @Query("SELECT u FROM UploadHistory u WHERE u.uploadDate >= :thirtyDaysAgo ORDER BY u.uploadDate DESC")
    List<UploadHistory> findRecentUploads(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    /**
     * Count uploads by status
     */
    long countByUploadStatus(UploadHistory.UploadStatus status);

    /**
     * Get total file size uploaded
     */
    @Query("SELECT COALESCE(SUM(u.fileSize), 0) FROM UploadHistory u")
    Long getTotalUploadedFileSize();

    /**
     * Get upload statistics
     */
    @Query("SELECT " +
           "COUNT(u) as totalUploads, " +
           "SUM(CASE WHEN u.uploadStatus = 'COMPLETED' THEN 1 ELSE 0 END) as successfulUploads, " +
           "SUM(CASE WHEN u.uploadStatus = 'FAILED' THEN 1 ELSE 0 END) as failedUploads, " +
           "SUM(CASE WHEN u.uploadStatus = 'PARTIALLY_COMPLETED' THEN 1 ELSE 0 END) as partialUploads, " +
           "COALESCE(SUM(u.totalRecords), 0) as totalRecords, " +
           "COALESCE(SUM(u.processedRecords), 0) as processedRecords, " +
           "COALESCE(SUM(u.failedRecords), 0) as failedRecords " +
           "FROM UploadHistory u")
    Object[] getUploadStatistics();

    /**
     * Find uploads with file content (for re-processing)
     */
    @Query("SELECT u FROM UploadHistory u WHERE u.fileContent IS NOT NULL ORDER BY u.uploadDate DESC")
    List<UploadHistory> findUploadsWithFileContent();

    /**
     * Delete old uploads (older than specified date)
     */
    void deleteByUploadDateBefore(LocalDateTime cutoffDate);
}
