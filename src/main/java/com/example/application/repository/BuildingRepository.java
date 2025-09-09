package com.example.application.repository;

import com.example.application.domain.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    
    Optional<Building> findByBuildingNumber(String buildingNumber);
    
    @Query("SELECT COUNT(b) FROM Building b")
    long count();
}
