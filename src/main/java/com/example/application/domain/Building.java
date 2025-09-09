package com.example.application.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a building in the apartment complex.
 * Each building has a number and contains multiple entrances.
 */
@Entity
@Table(name = "building")
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "building_id")
    private Long id;

    @Column(name = "building_number", nullable = false, unique = true, length = 10)
    private String buildingNumber;

    @Column(name = "total_entrances", nullable = false)
    private Integer totalEntrances;

    @Column(name = "apartments_per_entrance", nullable = false)
    private Integer apartmentsPerEntrance;

    @Column(name = "floors", nullable = false)
    private Integer floors;

    @Column(name = "apartments_per_floor", nullable = false)
    private Integer apartmentsPerFloor;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Entrance> entrances = new ArrayList<>();

    protected Building() {
        // For JPA
    }

    public Building(String buildingNumber, Integer totalEntrances) {
        this.buildingNumber = buildingNumber;
        this.totalEntrances = totalEntrances;
        this.apartmentsPerEntrance = 80; // Fixed: 80 apartments per entrance
        this.floors = 16; // Fixed: 16 floors
        this.apartmentsPerFloor = 6; // Fixed: 6 apartments per floor
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public Integer getTotalEntrances() {
        return totalEntrances;
    }

    public void setTotalEntrances(Integer totalEntrances) {
        this.totalEntrances = totalEntrances;
    }

    public Integer getApartmentsPerEntrance() {
        return apartmentsPerEntrance;
    }

    public void setApartmentsPerEntrance(Integer apartmentsPerEntrance) {
        this.apartmentsPerEntrance = apartmentsPerEntrance;
    }

    public Integer getFloors() {
        return floors;
    }

    public void setFloors(Integer floors) {
        this.floors = floors;
    }

    public Integer getApartmentsPerFloor() {
        return apartmentsPerFloor;
    }

    public void setApartmentsPerFloor(Integer apartmentsPerFloor) {
        this.apartmentsPerFloor = apartmentsPerFloor;
    }

    public List<Entrance> getEntrances() {
        return entrances;
    }

    public void setEntrances(List<Entrance> entrances) {
        this.entrances = entrances;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Building other = (Building) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Building " + buildingNumber;
    }
}
