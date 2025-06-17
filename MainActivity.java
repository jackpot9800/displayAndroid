package com.displayclient;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends Activity {

    private EditText serverUrlEditText;
    private EditText displayKeyEditText;
    private Button connectButton;
    private TextView statusTextView;
    private WebView displayWebView;
    private LinearLayout configLayout;

    private OkHttpClient httpClient;
    private Handler heartbeatHandler;
    private Runnable heartbeatRunnable;
    private String serverUrl;
    private String displayKey;
    private boolean isConnected = false;

    private static final String PREFS_NAME = "DisplayClientPrefs";
    private static final String PREF_SERVER_URL = "server_url";
    private static final String PREF_DISPLAY_KEY = "display_key";
    private static final int HEARTBEAT_INTERVAL = 30000; // 30 secondes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeViews();
        setupHttpClient();
        loadSavedSettings();
        setupHeartbeat();
    }

    private void initializeViews() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Configuration panel
        configLayout = new LinearLayout(this);
        configLayout.setOrientation(LinearLayout.VERTICAL);
        configLayout.setPadding(20, 20, 20, 20);

        TextView serverLabel = new TextView(this);
        serverLabel.setText("URL du serveur:");
        configLayout.addView(serverLabel);

        serverUrlEditText = new EditText(this);
        serverUrlEditText.setText("http://192.168.0.20/mods/livetv/display_actions.php");
        configLayout.addView(serverUrlEditText);

        TextView keyLabel = new TextView(this);
        keyLabel.setText("Clé d'affichage:");
        configLayout.addView(keyLabel);

        displayKeyEditText = new EditText(this);
        configLayout.addView(displayKeyEditText);

        connectButton = new Button(this);
        connectButton.setText("Connecter");
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToServer();
            }
        });
        configLayout.addView(connectButton);

        // Status bar
        statusTextView = new TextView(this);
        statusTextView.setText("Non connecté");
        statusTextView.setPadding(20, 10, 20, 10);

        // WebView for display
        displayWebView = new WebView(this);
        displayWebView.getSettings().setJavaScriptEnabled(true);
        displayWebView.getSettings().setDomStorageEnabled(true);
        displayWebView.setWebViewClient(new WebViewClient());

        mainLayout.addView(configLayout);
        mainLayout.addView(statusTextView);
        mainLayout.addView(displayWebView);

        setContentView(mainLayout);
    }

    private void setupHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private void loadSavedSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        serverUrlEditText
                .setText(prefs.getString(PREF_SERVER_URL, "http://192.168.0.20/mods/livetv/display_actions.php"));
        displayKeyEditText.setText(prefs.getString(PREF_DISPLAY_KEY, ""));
    }

    private void saveSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_SERVER_URL, serverUrl);
        editor.putString(PREF_DISPLAY_KEY, displayKey);
        editor.apply();
    }

    private void setupHeartbeat() {
        heartbeatHandler = new Handler();
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    sendHeartbeat();
                    heartbeatHandler.postDelayed(this, HEARTBEAT_INTERVAL);
                }
            }
        };
    }

    private void connectToServer() {
        serverUrl = serverUrlEditText.getText().toString().trim();
        displayKey = displayKeyEditText.getText().toString().trim();

        if (serverUrl.isEmpty() || displayKey.isEmpty()) {
            Toast.makeText(this, "L'URL du serveur et la clé d'affichage sont requises", Toast.LENGTH_SHORT).show();
            return;
        }

        authenticateWithServer();
    }

    private void authenticateWithServer() {
        FormBody formBody = new FormBody.Builder()
                .add("action", "client_auth")
                .add("display_key", displayKey)
                .build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(formBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    statusTextView.setText("Erreur de connexion: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        if (jsonResponse.getBoolean("success")) {
                            onConnectionSuccess(jsonResponse);
                        } else {
                            String errorMessage = jsonResponse.getString("message");
                            statusTextView.setText("Erreur: " + errorMessage);
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        statusTextView.setText("Erreur de parsing: " + e.getMessage());
                    }
                });
            }
        });
    }

    private void onConnectionSuccess(JSONObject response) {
        try {
            isConnected = true;
            connectButton.setEnabled(false);
            serverUrlEditText.setEnabled(false);
            displayKeyEditText.setEnabled(false);
            configLayout.setVisibility(View.GONE);

            statusTextView.setText("Connecté");
            saveSettings();

            JSONObject display = response.getJSONObject("display");
            String displayName = display.getString("name");
            String location = display.getString("location");

            statusTextView.setText("Connecté - " + displayName + " (" + location + ")");

            // Charger la présentation si disponible
            if (!display.isNull("presentation_id")) {
                int presentationId = display.getInt("presentation_id");
                String presentationUrl = serverUrl.replace("display_actions.php", "") + "presentation.php?id="
                        + presentationId;
                displayWebView.loadUrl(presentationUrl);
            }

            // Appliquer les paramètres
            JSONObject settings = response.getJSONObject("settings");
            applySettings(settings);

            // Démarrer le heartbeat
            heartbeatHandler.post(heartbeatRunnable);

        } catch (Exception e) {
            statusTextView.setText("Erreur lors de la connexion: " + e.getMessage());
        }
    }

    private void sendHeartbeat() {
        FormBody formBody = new FormBody.Builder()
                .add("action", "client_heartbeat")
                .add("display_key", displayKey)
                .add("status", "online")
                .build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(formBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    statusTextView.setText("Erreur heartbeat: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        if (jsonResponse.getBoolean("success")) {
                            JSONObject pendingActions = jsonResponse.getJSONObject("pending_actions");

                            if (pendingActions.getBoolean("refresh_requested")) {
                                displayWebView.reload();
                            }

                            if (pendingActions.getBoolean("restart_requested")) {
                                restartApp();
                            }
                        }
                    } catch (Exception e) {
                        statusTextView.setText("Erreur heartbeat parsing: " + e.getMessage());
                    }
                });
            }
        });
    }

    private void applySettings(JSONObject settings) {
        try {
            // Appliquer les paramètres d'affichage
            String orientation = settings.getString("orientation");
            if ("portrait".equals(orientation)) {
                // Forcer l'orientation portrait
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                // Forcer l'orientation paysage
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            // Autres paramètres peuvent être appliqués ici
            // brightness, volume, etc.

        } catch (Exception e) {
            statusTextView.setText("Erreur application paramètres: " + e.getMessage());
        }
    }

    private void restartApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isConnected = false;
        if (heartbeatHandler != null) {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Envoyer un statut "en pause" au serveur
        if (isConnected) {
            sendStatusUpdate("paused");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Envoyer un statut "actif" au serveur
        if (isConnected) {
            sendStatusUpdate("online");
        }
    }

    private void sendStatusUpdate(String status) {
        FormBody formBody = new FormBody.Builder()
                .add("action", "client_heartbeat")
                .add("display_key", displayKey)
                .add("status", status)
                .build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(formBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Ignorer les erreurs de mise à jour de statut
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Réponse reçue avec succès
            }
        });
    }
}
