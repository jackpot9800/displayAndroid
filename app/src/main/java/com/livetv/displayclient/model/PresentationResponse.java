package com.livetv.displayclient.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PresentationResponse {
    
    @SerializedName("presentations")
    private List<Presentation> presentations;
    
    // Constructeurs
    public PresentationResponse() {}
    
    public PresentationResponse(List<Presentation> presentations) {
        this.presentations = presentations;
    }
    
    // Getters et Setters
    public List<Presentation> getPresentations() {
        return presentations;
    }
    
    public void setPresentations(List<Presentation> presentations) {
        this.presentations = presentations;
    }
}
