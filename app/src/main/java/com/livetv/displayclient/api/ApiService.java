package com.livetv.displayclient.api;

import com.livetv.displayclient.model.PresentationDetailResponse;
import com.livetv.displayclient.model.PresentationResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    
    @GET("presentations")
    Call<PresentationResponse> getPresentations();
    
    @GET("presentation/{id}")
    Call<PresentationDetailResponse> getPresentationDetail(@Path("id") int presentationId);
}
