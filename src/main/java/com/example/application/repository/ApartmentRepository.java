package com.example.application.repository;

import com.example.application.domain.Apartment;
import com.example.application.domain.Entrance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    
    List<Apartment> findByEntrance(Entrance entrance);
    
    List<Apartment> findByEntranceOrderByDoorNumber(Entrance entrance);
    
    Optional<Apartment> findByEntranceAndDoorNumber(Entrance entrance, Integer doorNumber);
    
    @Query("SELECT a FROM Apartment a WHERE a.entrance.building.buildingNumber = :buildingNumber " +
           "AND a.entrance.entranceNumber = :entranceNumber AND a.doorNumber = :doorNumber")
    Optional<Apartment> findByBuildingEntranceAndDoor(@Param("buildingNumber") String buildingNumber,
                                                      @Param("entranceNumber") Integer entranceNumber,
                                                      @Param("doorNumber") Integer doorNumber);
}
