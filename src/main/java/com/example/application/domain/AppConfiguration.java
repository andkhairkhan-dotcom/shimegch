package com.example.application.domain;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

/**
 * Application configuration settings.
 */
@Entity
@Table(name = "app_configuration")
public class AppConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "config_id")
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", length = 1000)
    private String configValue;

    @Column(name = "description", length = 500)
    private String description;

    protected AppConfiguration() {
        // For JPA
    }

    public AppConfiguration(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public AppConfiguration(String configKey, String configValue, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public @Nullable String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(@Nullable String configValue) {
        this.configValue = configValue;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        AppConfiguration other = (AppConfiguration) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return configKey + ": " + configValue;
    }
}
