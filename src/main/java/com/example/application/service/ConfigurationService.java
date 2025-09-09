package com.example.application.service;

import com.example.application.domain.AppConfiguration;
import com.example.application.repository.AppConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Service for managing application configuration.
 * Stores configuration in both database and local file for persistence.
 */
@Service
public class ConfigurationService {

    public static final String TOP_BAR_TEXT_KEY = "top_bar_text";
    private static final String CONFIG_FILE_NAME = "shimegch-config.properties";
    private static final String DEFAULT_TOP_BAR_TEXT = "Shimegch - СӨХ Төлбөрийн Удирдлагын Систем";

    private final AppConfigurationRepository configRepository;
    private final Path configFilePath;

    public ConfigurationService(AppConfigurationRepository configRepository) {
        this.configRepository = configRepository;
        this.configFilePath = Paths.get(System.getProperty("user.home"), CONFIG_FILE_NAME);
        initializeConfiguration();
    }

    @Transactional
    public void initializeConfiguration() {
        // Load from file first, then database
        loadConfigurationFromFile();
        
        // Ensure default values exist
        if (!configRepository.findByConfigKey(TOP_BAR_TEXT_KEY).isPresent()) {
            AppConfiguration topBarConfig = new AppConfiguration(
                TOP_BAR_TEXT_KEY, 
                DEFAULT_TOP_BAR_TEXT,
                "Top bar дээр харагдах текст"
            );
            configRepository.save(topBarConfig);
            saveConfigurationToFile();
        }
    }

    @Transactional
    public void setTopBarText(String text) {
        AppConfiguration config = configRepository.findByConfigKey(TOP_BAR_TEXT_KEY)
            .orElse(new AppConfiguration(TOP_BAR_TEXT_KEY, text, "Top bar дээр харагдах текст"));
        
        config.setConfigValue(text);
        configRepository.save(config);
        saveConfigurationToFile();
    }

    @Transactional(readOnly = true)
    public String getTopBarText() {
        return configRepository.findByConfigKey(TOP_BAR_TEXT_KEY)
            .map(AppConfiguration::getConfigValue)
            .orElse(DEFAULT_TOP_BAR_TEXT);
    }

    private void loadConfigurationFromFile() {
        if (!Files.exists(configFilePath)) {
            return;
        }

        try (InputStream input = Files.newInputStream(configFilePath)) {
            Properties props = new Properties();
            props.load(input);

            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                AppConfiguration config = configRepository.findByConfigKey(key)
                    .orElse(new AppConfiguration(key, value));
                
                config.setConfigValue(value);
                configRepository.save(config);
            }
        } catch (IOException e) {
            System.err.println("Failed to load configuration from file: " + e.getMessage());
        }
    }

    private void saveConfigurationToFile() {
        try {
            Properties props = new Properties();
            
            // Get all configurations from database
            configRepository.findAll().forEach(config -> {
                if (config.getConfigValue() != null) {
                    props.setProperty(config.getConfigKey(), config.getConfigValue());
                }
            });

            try (OutputStream output = Files.newOutputStream(configFilePath)) {
                props.store(output, "Shimegch Application Configuration");
            }
        } catch (IOException e) {
            System.err.println("Failed to save configuration to file: " + e.getMessage());
        }
    }
}
