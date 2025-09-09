package com.example.application.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Configuration for payment ranking thresholds.
 * Allows admin to set different ranks based on outstanding balance amounts.
 */
@Entity
@Table(name = "rank_configuration")
public class RankConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "rank_config_id")
    private Long id;

    @Column(name = "rank_name", nullable = false, unique = true, length = 100)
    private String rankName;

    @Column(name = "threshold_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal thresholdAmount;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "color_code", length = 7)
    private String colorCode;

    protected RankConfiguration() {
        // For JPA
    }

    public RankConfiguration(String rankName, BigDecimal thresholdAmount) {
        this.rankName = rankName;
        this.thresholdAmount = thresholdAmount;
    }

    public RankConfiguration(String rankName, BigDecimal thresholdAmount, String colorCode) {
        this.rankName = rankName;
        this.thresholdAmount = thresholdAmount;
        this.colorCode = colorCode;
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getRankName() {
        return rankName;
    }

    public void setRankName(String rankName) {
        this.rankName = rankName;
    }

    public BigDecimal getThresholdAmount() {
        return thresholdAmount;
    }

    public void setThresholdAmount(BigDecimal thresholdAmount) {
        this.thresholdAmount = thresholdAmount;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public @Nullable String getColorCode() {
        return colorCode;
    }

    public void setColorCode(@Nullable String colorCode) {
        this.colorCode = colorCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        RankConfiguration other = (RankConfiguration) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return rankName + " (≥ " + thresholdAmount + " MNT)";
    }
}
