package com.example.application.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a monthly payment record for a household.
 * Contains the outstanding balance for СӨХ payments.
 */
@Entity
@Table(name = "payment_record")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "payment_record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(name = "record_month", nullable = false)
    private LocalDate recordMonth;

    @Column(name = "outstanding_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal outstandingBalance;

    @Column(name = "upload_date", nullable = false)
    private LocalDate uploadDate;

    protected PaymentRecord() {
        // For JPA
    }

    public PaymentRecord(Household household, LocalDate recordMonth, BigDecimal outstandingBalance) {
        this.household = household;
        this.recordMonth = recordMonth;
        this.outstandingBalance = outstandingBalance;
        this.uploadDate = LocalDate.now();
    }

    public @Nullable Long getId() {
        return id;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public LocalDate getRecordMonth() {
        return recordMonth;
    }

    public void setRecordMonth(LocalDate recordMonth) {
        this.recordMonth = recordMonth;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public LocalDate getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDate uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        PaymentRecord other = (PaymentRecord) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return household.getHouseholdName() + " - " + recordMonth + ": " + outstandingBalance + " MNT";
    }
}
