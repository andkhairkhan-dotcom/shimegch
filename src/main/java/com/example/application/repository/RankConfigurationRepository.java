package com.example.application.repository;

import com.example.application.domain.RankConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankConfigurationRepository extends JpaRepository<RankConfiguration, Long> {
    
    List<RankConfiguration> findByIsActiveTrue();
    
    @Query("SELECT rc FROM RankConfiguration rc WHERE rc.isActive = true ORDER BY rc.thresholdAmount DESC")
    List<RankConfiguration> findActiveRanksOrderByThresholdDesc();
}
