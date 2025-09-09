package com.example.application.repository;

import com.example.application.domain.AppConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppConfigurationRepository extends JpaRepository<AppConfiguration, Long> {
    
    Optional<AppConfiguration> findByConfigKey(String configKey);
}
