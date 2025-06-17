package com.livetv.displayclient.model;

import com.google.gson.annotations.SerializedName;

public class PresentationDetailResponse {
    
    @SerializedName("presentation")
    private PresentationDetail presentation;
    
    // Constructeurs
    public PresentationDetailResponse() {}
    
    public PresentationDetailResponse(PresentationDetail presentation) {
        this.presentation = presentation;
    }
    
    // Getters et Setters
    public PresentationDetail getPresentation() {
        return presentation;
    }
    
    public void setPresentation(PresentationDetail presentation) {
        this.presentation = presentation;
    }
}
