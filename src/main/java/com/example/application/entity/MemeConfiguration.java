package com.example.application.entity;

import jakarta.persistence.*;

/**
 * Entity for storing meme configurations (images and texts) for PDF generation
 */
@Entity
@Table(name = "meme_configurations")
public class MemeConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meme_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemeType memeType;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder;

    public enum MemeType {
        IMAGE_URL,  // URL to meme image
        TEXT        // Funny text
    }

    // Constructors
    public MemeConfiguration() {}

    public MemeConfiguration(MemeType memeType, String content) {
        this.memeType = memeType;
        this.content = content;
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MemeType getMemeType() {
        return memeType;
    }

    public void setMemeType(MemeType memeType) {
        this.memeType = memeType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
