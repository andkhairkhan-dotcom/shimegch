package com.example.application.repository;

import com.example.application.domain.Building;
import com.example.application.domain.Entrance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntranceRepository extends JpaRepository<Entrance, Long> {
    
    List<Entrance> findByBuilding(Building building);
    
    List<Entrance> findByBuildingOrderByEntranceNumber(Building building);
}
