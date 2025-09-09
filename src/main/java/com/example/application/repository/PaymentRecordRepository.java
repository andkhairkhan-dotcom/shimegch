package com.example.application.repository;

import com.example.application.domain.Household;
import com.example.application.domain.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    
    List<PaymentRecord> findByHousehold(Household household);
    
    @Query("SELECT pr FROM PaymentRecord pr " +
           "JOIN FETCH pr.household h " +
           "JOIN FETCH h.apartment a " +
           "JOIN FETCH a.entrance e " +
           "JOIN FETCH e.building b " +
           "WHERE pr.recordMonth = :recordMonth")
    List<PaymentRecord> findByRecordMonth(@Param("recordMonth") LocalDate recordMonth);

    Optional<PaymentRecord> findByHouseholdAndRecordMonth(Household household, LocalDate recordMonth);

    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.household.id = :householdId ORDER BY pr.recordMonth")
    List<PaymentRecord> findByHouseholdIdOrderByRecordMonth(@Param("householdId") Long householdId);
    
    @Query("SELECT pr FROM PaymentRecord pr WHERE pr.outstandingBalance >= :threshold")
    List<PaymentRecord> findByOutstandingBalanceGreaterThanEqual(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT pr FROM PaymentRecord pr " +
           "JOIN FETCH pr.household h " +
           "JOIN FETCH h.apartment a " +
           "JOIN FETCH a.entrance e " +
           "JOIN FETCH e.building b " +
           "WHERE pr.recordMonth = :month AND pr.outstandingBalance >= :threshold " +
           "ORDER BY pr.outstandingBalance DESC")
    List<PaymentRecord> findByMonthAndBalanceThreshold(@Param("month") LocalDate month,
                                                      @Param("threshold") BigDecimal threshold);
    
    @Query("SELECT pr FROM PaymentRecord pr " +
           "JOIN FETCH pr.household h " +
           "JOIN FETCH h.apartment a " +
           "JOIN FETCH a.entrance e " +
           "JOIN FETCH e.building b " +
           "WHERE pr.recordMonth = (SELECT MAX(pr2.recordMonth) FROM PaymentRecord pr2)")
    List<PaymentRecord> findLatestRecords();
}
