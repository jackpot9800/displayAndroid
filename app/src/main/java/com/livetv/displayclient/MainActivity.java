package com.livetv.displayclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.livetv.displayclient.api.ApiClient;
import com.livetv.displayclient.api.ApiService;
import com.livetv.displayclient.model.Presentation;
import com.livetv.displayclient.model.PresentationResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity {
    
    private ListView presentationsList;
    private TextView statusText;
    private List<Presentation> presentations;
    private ArrayAdapter<String> adapter;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initApi();
        loadPresentations();
    }
    
    private void initViews() {
        presentationsList = findViewById(R.id.presentations_list);
        statusText = findViewById(R.id.status_text);
        
        presentations = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        presentationsList.setAdapter(adapter);
        
        presentationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < presentations.size()) {
                    openPresentation(presentations.get(position));
                }
            }
        });
    }
    
    private void initApi() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }
    
    private void loadPresentations() {
        statusText.setText("Chargement des présentations...");
        
        Call<PresentationResponse> call = apiService.getPresentations();
        call.enqueue(new Callback<PresentationResponse>() {
            @Override
            public void onResponse(@NonNull Call<PresentationResponse> call, @NonNull Response<PresentationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    presentations.clear();
                    presentations.addAll(response.body().getPresentations());
                    
                    List<String> titles = new ArrayList<>();
                    for (Presentation p : presentations) {
                        String title = p.getTitle();
                        if (title == null || title.isEmpty()) {
                            title = "Présentation sans titre";
                        }
                        titles.add(title + " (" + p.getSlideCount() + " slides)");
                    }
                    
                    adapter.clear();
                    adapter.addAll(titles);
                    adapter.notifyDataSetChanged();
                    
                    statusText.setText(presentations.size() + " présentation(s) disponible(s)");
                } else {
                    statusText.setText("Erreur lors du chargement");
                    Toast.makeText(MainActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<PresentationResponse> call, @NonNull Throwable t) {
                statusText.setText("Erreur de connexion");
                Toast.makeText(MainActivity.this, "Impossible de se connecter au serveur", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void openPresentation(Presentation presentation) {
        Intent intent = new Intent(this, PresentationActivity.class);
        intent.putExtra("presentation_id", presentation.getId());
        intent.putExtra("presentation_title", presentation.getTitle());
        startActivity(intent);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                // Gérer la sélection avec la télécommande
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    focusedView.performClick();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                // Recharger les présentations
                loadPresentations();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les présentations quand on revient à l'activité principale
        loadPresentations();
    }
}
