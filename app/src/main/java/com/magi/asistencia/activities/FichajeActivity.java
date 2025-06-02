package com.magi.asistencia.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.annotation.NonNull;
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

    private MaterialToolbar topAppBar;
    private String dni;

    // Nombre del SharedPreferences y clave interna
    private static final String PREFS_NAME = "magi_prefs";
    private static final String KEY_JORNADA_INICIADA = "jornada_iniciada";

    private Button btnIn;
    private Button btnOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fichaje);

        // Recuperamos el DNI desde el Intent
        dni = getIntent().getStringExtra("DNI");

        // Forzamos modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Ajustes para status bar con iconos oscuros
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(true);
        View root = findViewById(R.id.activity_fichaje_coordinator_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBars.top, 0, 0);
            return insets;
        });

        // Toolbar
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayShowTitleEnabled(false);

        // Logo que vuelve al Dashboard
        ImageView logo = findViewById(R.id.logoText_toolbar);
        logo.setOnClickListener(v -> {
            Intent i = new Intent(this, DashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });

        // Menu icon para desplegable
        ImageView menuIcon = findViewById(R.id.ic_menu_toolbar);
        menuIcon.setOnClickListener(this::showModulesMenu);

        // Botones In y Out
        btnIn  = findViewById(R.id.btnIn);
        btnOut = findViewById(R.id.btnOut);

        // 1) Leemos de SharedPreferences si la jornada está iniciada
        boolean jornadaIniciada = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_JORNADA_INICIADA, false);

        // 2) Si la jornada ya estaba iniciada, deshabilitamos btnIn y bajamos la opacidad
        if (jornadaIniciada) {
            btnIn.setEnabled(false);
            btnIn.setAlpha(0.5f);
            // Opción: dejamos btnOut habilitado para poder cerrar la jornada
            btnOut.setEnabled(true);
            btnOut.setAlpha(1f);
        } else {
            // Si NO está iniciada, habilitamos btnIn y deshabilitamos btnOut
            btnIn.setEnabled(true);
            btnIn.setAlpha(1f);
            btnOut.setEnabled(false);
            btnOut.setAlpha(0.5f);
        }

        // 3) Asignamos listeners que disparan el AsyncTask
        btnIn.setOnClickListener(v -> new FichajeTask("start").execute());
        btnOut.setOnClickListener(v -> new FichajeTask("end").execute());
    }

    // Menú desplegable (igual que antes)
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_fichajes) {
            Intent intent = new Intent(this, FichajeActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_guardias) {
            Intent intent = new Intent(this, GuardiasActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_informes) {
            Intent intent = new Intent(this, InformesActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cerrar sesión");
            builder.setMessage("¿Estás seguro de que quieres cerrar sesión?");
            builder.setPositiveButton("Sí, cerrar", (dialog, which) -> {
                Intent intent = new Intent(FichajeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            Button btnSi = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSi.setTextColor(ContextCompat.getColor(this, R.color.amarillo_magi));

            Button btnNo = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            btnNo.setTextColor(ContextCompat.getColor(this, R.color.gris_claro));

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class FichajeTask extends AsyncTask<Void, Void, Boolean> {

        private final String tipo;
        private int code = -1;
        private String errorMsg = null;

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
                // Si el POST ha sido OK, mostramos el Toast y actualizamos SharedPreferences
                if (tipo.equals("start")) {
                    Toast.makeText(FichajeActivity.this,
                            "Entrada registrada", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "start registrado con éxito");

                    // 1) Guardamos en SharedPreferences que la jornada está iniciada
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_JORNADA_INICIADA, true)
                            .apply();

                    // 2) Deshabilitamos btnIn y bajamos opacidad; habilitamos btnOut
                    btnIn.setEnabled(false);
                    btnIn.setAlpha(0.5f);
                    btnOut.setEnabled(true);
                    btnOut.setAlpha(1f);

                } else { // tipo == "end"
                    Toast.makeText(FichajeActivity.this,
                            "Salida registrada", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "end registrado con éxito");

                    // 1) Guardamos en SharedPreferences que la jornada ya NO está iniciada
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_JORNADA_INICIADA, false)
                            .apply();

                    // 2) Deshabilitamos btnOut y bajamos opacidad; habilitamos btnIn
                    btnOut.setEnabled(false);
                    btnOut.setAlpha(0.5f);
                    btnIn.setEnabled(true);
                    btnIn.setAlpha(1f);
                }
            } else {
                // Si falla, mostramos el mensaje de error que venga del backend o de red
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
