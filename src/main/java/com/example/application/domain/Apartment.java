package com.example.application.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

/**
 * Represents an apartment within an entrance.
 * Apartment numbers range from 1-80 per entrance, with door numbers starting from 1 on first floor.
 */
@Entity
@Table(name = "apartment")
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "apartment_id")
    private Long id;

    @Column(name = "door_number", nullable = false)
    private Integer doorNumber;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrance_id", nullable = false)
    private Entrance entrance;

    protected Apartment() {
        // For JPA
    }

    public Apartment(Integer doorNumber, Integer floorNumber, Entrance entrance) {
        this.doorNumber = doorNumber;
        this.floorNumber = floorNumber;
        this.entrance = entrance;
    }

    public @Nullable Long getId() {
        return id;
    }

    public Integer getDoorNumber() {
        return doorNumber;
    }

    public void setDoorNumber(Integer doorNumber) {
        this.doorNumber = doorNumber;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public Entrance getEntrance() {
        return entrance;
    }

    public void setEntrance(Entrance entrance) {
        this.entrance = entrance;
    }

    /**
     * Gets the full apartment identifier including building and entrance info
     */
    public String getFullIdentifier() {
        return entrance.getBuilding().getBuildingNumber() + "-" + 
               entrance.getEntranceNumber() + "-" + doorNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Apartment other = (Apartment) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Apartment " + doorNumber + " (Floor " + floorNumber + ", " + entrance + ")";
    }
}
