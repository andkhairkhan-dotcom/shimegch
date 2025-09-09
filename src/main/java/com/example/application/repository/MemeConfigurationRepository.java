package com.example.application.repository;

import com.example.application.entity.MemeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemeConfigurationRepository extends JpaRepository<MemeConfiguration, Long> {

    /**
     * Find all active meme configurations by type
     */
    List<MemeConfiguration> findByMemeTypeAndIsActiveTrueOrderByDisplayOrder(MemeConfiguration.MemeType memeType);

    /**
     * Find all active meme configurations
     */
    List<MemeConfiguration> findByIsActiveTrueOrderByDisplayOrder();

    /**
     * Get random meme by type
     */
    @Query(value = "SELECT * FROM meme_configurations WHERE meme_type = :memeType AND is_active = true ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    MemeConfiguration findRandomByMemeType(@Param("memeType") String memeType);

    /**
     * Get random active meme text
     */
    @Query(value = "SELECT * FROM meme_configurations WHERE meme_type = 'TEXT' AND is_active = true ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    MemeConfiguration findRandomMemeText();

    /**
     * Get random active meme image
     */
    @Query(value = "SELECT * FROM meme_configurations WHERE meme_type = 'IMAGE_URL' AND is_active = true ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    MemeConfiguration findRandomMemeImage();
}
