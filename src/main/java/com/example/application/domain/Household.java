package com.example.application.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

/**
 * Represents a household living in an apartment.
 */
@Entity
@Table(name = "household")
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "household_id")
    private Long id;

    @Column(name = "household_name", nullable = false, length = 200)
    private String householdName;

    @Column(name = "contact_info", length = 500)
    private String contactInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    protected Household() {
        // For JPA
    }

    public Household(String householdName, Apartment apartment) {
        this.householdName = householdName;
        this.apartment = apartment;
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getHouseholdName() {
        return householdName;
    }

    public void setHouseholdName(String householdName) {
        this.householdName = householdName;
    }

    public @Nullable String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(@Nullable String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Apartment getApartment() {
        return apartment;
    }

    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Household other = (Household) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return householdName + " (" + apartment.getFullIdentifier() + ")";
    }
}
