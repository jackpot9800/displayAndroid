package com.livetv.displayclient;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.livetv.displayclient.api.ApiClient;
import com.livetv.displayclient.api.ApiService;
import com.livetv.displayclient.model.PresentationDetail;
import com.livetv.displayclient.model.PresentationDetailResponse;
import com.livetv.displayclient.model.Slide;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PresentationActivity extends Activity {
    
    private ImageView slideImageView;
    private TextView slideInfoText;
    private ProgressBar progressBar;
    private TextView loadingText;
    
    private ApiService apiService;
    private List<Slide> slides;
    private int currentSlideIndex = 0;
    private Handler slideHandler;
    private Runnable slideRunnable;
    private boolean isPlaying = false;
    
    private int presentationId;
    private String presentationTitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mode plein écran
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_presentation);
        
        // Récupérer les paramètres
        presentationId = getIntent().getIntExtra("presentation_id", 0);
        presentationTitle = getIntent().getStringExtra("presentation_title");
        
        initViews();
        initApi();
        loadPresentation();
    }
    
    private void initViews() {
        slideImageView = findViewById(R.id.slide_image);
        slideInfoText = findViewById(R.id.slide_info);
        progressBar = findViewById(R.id.progress_bar);
        loadingText = findViewById(R.id.loading_text);
        
        slideHandler = new Handler();
    }
    
    private void initApi() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }
    
    private void loadPresentation() {
        showLoading(true);
        loadingText.setText("Chargement de la présentation...");
        
        Call<PresentationDetailResponse> call = apiService.getPresentationDetail(presentationId);
        call.enqueue(new Callback<PresentationDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<PresentationDetailResponse> call, @NonNull Response<PresentationDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PresentationDetail presentation = response.body().getPresentation();
                    slides = presentation.getSlides();
                    
                    if (slides != null && !slides.isEmpty()) {
                        showLoading(false);
                        currentSlideIndex = 0;
                        showSlide(currentSlideIndex);
                        startSlideshow();
                    } else {
                        showError("Aucune slide trouvée dans cette présentation");
                    }
                } else {
                    showError("Erreur lors du chargement de la présentation");
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<PresentationDetailResponse> call, @NonNull Throwable t) {
                showError("Impossible de se connecter au serveur");
            }
        });
    }
    
    private void showSlide(int index) {
        if (slides == null || index < 0 || index >= slides.size()) {
            return;
        }
        
        Slide slide = slides.get(index);
        
        // Charger l'image avec Glide
        Glide.with(this)
                .load(slide.getImageUrl())
                .centerCrop()
                .into(slideImageView);
        
        // Mettre à jour les informations
        String info = String.format("%s - Slide %d/%d", 
                presentationTitle != null ? presentationTitle : "Présentation",
                index + 1, 
                slides.size());
        slideInfoText.setText(info);
    }
    
    private void startSlideshow() {
        if (slides == null || slides.isEmpty()) {
            return;
        }
        
        isPlaying = true;
        scheduleNextSlide();
    }
    
    private void scheduleNextSlide() {
        if (!isPlaying || slides == null || slides.isEmpty()) {
            return;
        }
        
        Slide currentSlide = slides.get(currentSlideIndex);
        int duration = currentSlide.getDuration() > 0 ? currentSlide.getDuration() * 1000 : 5000; // 5 secondes par défaut
        
        slideRunnable = new Runnable() {
            @Override
            public void run() {
                nextSlide();
            }
        };
        
        slideHandler.postDelayed(slideRunnable, duration);
    }
    
    private void nextSlide() {
        if (slides == null || slides.isEmpty()) {
            return;
        }
        
        currentSlideIndex = (currentSlideIndex + 1) % slides.size();
        showSlide(currentSlideIndex);
        scheduleNextSlide();
    }
    
    private void previousSlide() {
        if (slides == null || slides.isEmpty()) {
            return;
        }
        
        currentSlideIndex = (currentSlideIndex - 1 + slides.size()) % slides.size();
        showSlide(currentSlideIndex);
        
        // Redémarrer le timer
        stopSlideshow();
        startSlideshow();
    }
    
    private void togglePlayPause() {
        if (isPlaying) {
            stopSlideshow();
            Toast.makeText(this, "Présentation en pause", Toast.LENGTH_SHORT).show();
        } else {
            startSlideshow();
            Toast.makeText(this, "Présentation reprise", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopSlideshow() {
        isPlaying = false;
        if (slideRunnable != null) {
            slideHandler.removeCallbacks(slideRunnable);
        }
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loadingText.setVisibility(show ? View.VISIBLE : View.GONE);
        slideImageView.setVisibility(show ? View.GONE : View.VISIBLE);
        slideInfoText.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showError(String message) {
        showLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                nextSlide();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                previousSlide();
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                togglePlayPause();
                return true;
            case KeyEvent.KEYCODE_BACK:
                stopSlideshow();
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        stopSlideshow();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (slides != null && !slides.isEmpty()) {
            startSlideshow();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSlideshow();
    }
}
