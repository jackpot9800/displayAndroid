package com.livetv.displayclient;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.livetv.displayclient.api.ApiClient;

public class SettingsActivity extends Activity {
    
    private static final String PREFS_NAME = "LiveTVSettings";
    private static final String PREF_SERVER_URL = "server_url";
    private static final String DEFAULT_SERVER_URL = "http://192.168.18.28/mods/livetv/api/";
    
    private EditText serverUrlEditText;
    private Button saveButton;
    private TextView versionInfo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        initViews();
        loadSettings();
        setupListeners();
        displayVersion();
    }
    
    private void initViews() {
        serverUrlEditText = findViewById(R.id.server_url);
        saveButton = findViewById(R.id.save_button);
        versionInfo = findViewById(R.id.version_info);
    }
    
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String serverUrl = prefs.getString(PREF_SERVER_URL, DEFAULT_SERVER_URL);
        serverUrlEditText.setText(serverUrl);
    }
    
    private void setupListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }
    
    private void displayVersion() {
        // Version codée en dur pour éviter les problèmes de compilation
        String versionName = "1.2";
        versionInfo.setText("Version: " + versionName);
    }
    
    private void saveSettings() {
        String serverUrl = serverUrlEditText.getText().toString().trim();
        
        // Validation de l'URL
        if (serverUrl.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer une adresse de serveur", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Ajouter http:// si pas présent
        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            serverUrl = "http://" + serverUrl;
        }
        
        // Ajouter / à la fin si pas présent
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/";
        }
        
        // Sauvegarder dans SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_SERVER_URL, serverUrl);
        editor.apply();
        
        // Réinitialiser le client API
        ApiClient.resetClient();
        
        Toast.makeText(this, "Paramètres sauvegardés", Toast.LENGTH_SHORT).show();
        finish(); // Retourner à l'activité précédente
    }
    
    public static String getServerUrl(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(PREF_SERVER_URL, DEFAULT_SERVER_URL);
    }
}
