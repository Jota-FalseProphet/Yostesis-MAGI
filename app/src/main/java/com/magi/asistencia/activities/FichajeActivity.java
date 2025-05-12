package com.magi.asistencia.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.magi.asistencia.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class FichajeActivity extends AppCompatActivity {

    private static final String TAG = "FichajeTask";
    private static final String BASE_URL = "https://magi.it.com/api/fichaje";

    private com.google.android.material.appbar.MaterialToolbar topAppBar;

    private String dni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fichaje);

        // Obtener DNI desde Login
        dni = getIntent().getStringExtra("DNI");
        // Siempre en modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Edge-to-edge + status bar blanca + iconos oscuros
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(true);
        // Ajuste de padding top al DrawerLayout
        View root = findViewById(R.id.activity_fichaje_coordinator_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBars.top, 0, 0);
            return insets;
        });
        // Configuramos toolbar
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayShowTitleEnabled(false);
        // ——— LOGO: REDIRIGE AL DASHBOARD ———
        ImageView logo = findViewById(R.id.logoText_toolbar);
        logo.setOnClickListener(v -> {
            Intent i = new Intent(this, DashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });
        // ——— MENÚ ICON: ABRE DESPLEGABLE ———
        ImageView menuIcon = findViewById(R.id.ic_menu_toolbar);
        menuIcon.setOnClickListener(this::showModulesMenu);

        Button btnIn  = findViewById(R.id.btnIn);
        Button btnOut = findViewById(R.id.btnOut);

        btnIn.setOnClickListener(v -> new FichajeTask("start").execute());
        btnOut.setOnClickListener(v -> new FichajeTask("end").execute());
    }
    // MENU DESPLEGABLE
    private void showModulesMenu(View anchor) {
        Context wrapper = new ContextThemeWrapper(this, R.style.ThemeOverlay_PopupMAGI);
        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(wrapper, anchor);
        popup.inflate(R.menu.menu_dashboard);
        for (int i = 0; i < popup.getMenu().size(); i++) {
            MenuItem item = popup.getMenu().getItem(i);
            if (item.getIcon() != null) {
                item.getIcon().setTint(ContextCompat.getColor(this, R.color.amarillo_magi));
            }
        }
        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popup.show();
    }

    private class FichajeTask extends AsyncTask<Void, Void, Boolean> {

        private final String tipo;
        private int code = -1;
        private String errorMsg = null;  // mensaje de error desde el backend (me ahorro codigo)

        FichajeTask(String tipo) {
            this.tipo = tipo.toLowerCase(Locale.ROOT);
        }

        @Override
        protected Boolean doInBackground(Void... p) {
            HttpURLConnection c = null;
            try {
                String urlStr = BASE_URL + "/" + tipo + "?dni=" + dni;
                Log.d(TAG, "Llamando a URL: " + urlStr);
                URL url = new URL(urlStr);
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);

                code = c.getResponseCode();
                Log.d(TAG, "Código HTTP: " + code + " – " + c.getResponseMessage());

                if (code == HttpURLConnection.HTTP_OK) {
                    return true;
                } else {
                    // Leer el cuerpo de error
                    InputStream err = c.getErrorStream();
                    if (err != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(err));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();
                        try {
                            JSONObject obj = new JSONObject(sb.toString());
                            errorMsg = obj.optString("error", sb.toString());
                        } catch (JSONException je) {
                            errorMsg = sb.toString();
                        }
                    } else {
                        errorMsg = c.getResponseMessage();
                    }
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al ejecutar petición de fichaje", e);
                errorMsg = e.getMessage();
                return false;
            } finally {
                if (c != null) c.disconnect();
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (ok) {
                Toast.makeText(FichajeActivity.this,
                        tipo.equals("start") ? "Entrada registrada" : "Salida registrada",
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, tipo + " registrado con éxito");
            } else {
                // Mostrar mensaje de error capturado desde el backend
                if (errorMsg != null && !errorMsg.isEmpty()) {
                    Toast.makeText(FichajeActivity.this,
                            errorMsg,
                            Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Error API: " + errorMsg);
                } else if (code == HttpURLConnection.HTTP_CONFLICT) {
                    Toast.makeText(FichajeActivity.this,
                            "Fichaje no válido", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Fichaje no válido, código: " + code);
                } else {
                    Toast.makeText(FichajeActivity.this,
                            "Error de red (" + code + ")", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Error de red, código: " + code);
                }
            }
        }
    }
}
