package com.livetv.displayclient.model;

import com.google.gson.annotations.SerializedName;

public class Slide {
    
    @SerializedName("id")
    private int id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("image_path")
    private String imagePath;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("duration")
    private int duration;
    
    @SerializedName("transition_type")
    private String transitionType;
    
    // Constructeurs
    public Slide() {}
    
    public Slide(int id, String title, String imagePath, String imageUrl, int duration, String transitionType) {
        this.id = id;
        this.title = title;
        this.imagePath = imagePath;
        this.imageUrl = imageUrl;
        this.duration = duration;
        this.transitionType = transitionType;
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
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public String getTransitionType() {
        return transitionType;
    }
    
    public void setTransitionType(String transitionType) {
        this.transitionType = transitionType;
    }
}
