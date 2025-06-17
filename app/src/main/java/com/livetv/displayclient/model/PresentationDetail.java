package com.livetv.displayclient.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PresentationDetail extends Presentation {
    
    @SerializedName("slides")
    private List<Slide> slides;
    
    // Constructeurs
    public PresentationDetail() {
        super();
    }
    
    public PresentationDetail(int id, String title, String description, int slideCount, 
                            String createdAt, String previewUrl, List<Slide> slides) {
        super(id, title, description, slideCount, createdAt, previewUrl);
        this.slides = slides;
    }
    
    // Getters et Setters
    public List<Slide> getSlides() {
        return slides;
    }
    
    public void setSlides(List<Slide> slides) {
        this.slides = slides;
    }
}
