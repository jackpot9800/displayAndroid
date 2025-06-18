package com.displayclient;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private EditText serverUrlEditText;
    private EditText displayKeyEditText;
    private Button saveButton;

    private static final String PREFS_NAME = "DisplayClientPrefs";
    private static final String PREF_SERVER_URL = "server_url";
    private static final String PREF_DISPLAY_KEY = "display_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        TextView serverLabel = new TextView(this);
        serverLabel.setText("URL du serveur:");
        layout.addView(serverLabel);

        serverUrlEditText = new EditText(this);
        layout.addView(serverUrlEditText);

        TextView keyLabel = new TextView(this);
        keyLabel.setText("Clé d'affichage:");
        layout.addView(keyLabel);

        displayKeyEditText = new EditText(this);
        layout.addView(displayKeyEditText);

        saveButton = new Button(this);
        saveButton.setText("Enregistrer");
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
        layout.addView(saveButton);

        setContentView(layout);

        loadSettings();
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        serverUrlEditText.setText(prefs.getString(PREF_SERVER_URL, ""));
        displayKeyEditText.setText(prefs.getString(PREF_DISPLAY_KEY, ""));
    }

    private void saveSettings() {
        String serverUrl = serverUrlEditText.getText().toString().trim();
        String displayKey = displayKeyEditText.getText().toString().trim();

        if (serverUrl.isEmpty() || displayKey.isEmpty()) {
            Toast.makeText(this, "L'URL du serveur et la clé d'affichage sont requises", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_SERVER_URL, serverUrl);
        editor.putString(PREF_DISPLAY_KEY, displayKey);
        editor.apply();

        Toast.makeText(this, "Paramètres enregistrés", Toast.LENGTH_SHORT).show();
        finish();
    }
}
