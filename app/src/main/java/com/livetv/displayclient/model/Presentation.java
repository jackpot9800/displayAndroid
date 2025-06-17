package com.livetv.displayclient.model;

import com.google.gson.annotations.SerializedName;

public class Presentation {
    
    @SerializedName("id")
    private int id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("slide_count")
    private int slideCount;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("preview_url")
    private String previewUrl;
    
    // Constructeurs
    public Presentation() {}
    
    public Presentation(int id, String title, String description, int slideCount, String createdAt, String previewUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.slideCount = slideCount;
        this.createdAt = createdAt;
        this.previewUrl = previewUrl;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getSlideCount() {
        return slideCount;
    }
    
    public void setSlideCount(int slideCount) {
        this.slideCount = slideCount;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getPreviewUrl() {
        return previewUrl;
    }
    
    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }
}
