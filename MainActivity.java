package com.displayclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView statusTextView;
    private WebView displayWebView;

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

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        statusTextView = findViewById(R.id.statusTextView);
        displayWebView = findViewById(R.id.displayWebView);

        displayWebView.getSettings().setJavaScriptEnabled(true);
        displayWebView.getSettings().setDomStorageEnabled(true);
        displayWebView.setWebViewClient(new WebViewClient());

        setupHttpClient();
        loadSavedSettings();
        setupHeartbeat();
    }

    private void initializeViews() {
        // No longer needed as views are initialized in onCreate
    }

    private void setupHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private void loadSavedSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        serverUrl = prefs.getString(PREF_SERVER_URL, "");
        displayKey = prefs.getString(PREF_DISPLAY_KEY, "");
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
        if (serverUrl == null || serverUrl.isEmpty() || displayKey == null || displayKey.isEmpty()) {
            Toast.makeText(this, "L'URL du serveur et la clé d'affichage sont requises", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show the server URL being used for connection
        Toast.makeText(this, "Connexion au serveur: " + serverUrl, Toast.LENGTH_LONG).show();

        authenticateWithServer();
    }

    // Simple network discovery using UDP broadcast (example)
    private void discoverServer() {
        new Thread(() -> {
            try {
                java.net.DatagramSocket socket = new java.net.DatagramSocket();
                socket.setBroadcast(true);

                String discoveryMessage = "DISCOVER_DISPLAY_SERVER";
                byte[] sendData = discoveryMessage.getBytes();

                java.net.DatagramPacket sendPacket = new java.net.DatagramPacket(sendData, sendData.length,
                        java.net.InetAddress.getByName("255.255.255.255"), 8888);
                socket.send(sendPacket);

                byte[] recvBuf = new byte[15000];
                java.net.DatagramPacket receivePacket = new java.net.DatagramPacket(recvBuf, recvBuf.length);

                socket.setSoTimeout(5000); // 5 seconds timeout
                socket.receive(receivePacket);

                String message = new String(receivePacket.getData()).trim();
                String serverIp = receivePacket.getAddress().getHostAddress();

                if (message.equals("DISPLAY_SERVER_RESPONSE")) {
                    runOnUiThread(() -> {
                        serverUrl = "http://" + serverIp + "/mods/livetv/display_actions.php";
                        Toast.makeText(MainActivity.this, "Serveur découvert: " + serverUrl, Toast.LENGTH_LONG).show();
                        saveSettings();
                        connectToServer();
                    });
                }

                socket.close();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Découverte du serveur échouée: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        // Reload settings in case they changed
        loadSavedSettings();
        if (!isConnected) {
            connectToServer();
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
