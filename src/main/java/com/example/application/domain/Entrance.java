package com.example.application.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

/**
 * Represents an entrance within a building.
 * Each entrance has 80 apartments across 16 floors with 6 apartments per floor.
 */
@Entity
@Table(name = "entrance")
public class Entrance {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "entrance_id")
    private Long id;

    @Column(name = "entrance_number", nullable = false)
    private Integer entranceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    protected Entrance() {
        // For JPA
    }

    public Entrance(Integer entranceNumber, Building building) {
        this.entranceNumber = entranceNumber;
        this.building = building;
    }

    public @Nullable Long getId() {
        return id;
    }

    public Integer getEntranceNumber() {
        return entranceNumber;
    }

    public void setEntranceNumber(Integer entranceNumber) {
        this.entranceNumber = entranceNumber;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Entrance other = (Entrance) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Entrance " + entranceNumber + " (Building " + building.getBuildingNumber() + ")";
    }
}
