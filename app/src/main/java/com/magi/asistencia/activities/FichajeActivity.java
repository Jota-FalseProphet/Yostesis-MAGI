package com.magi.asistencia.activities;

import android.app.AlertDialog;
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

    private com.google.android.material.appbar.MaterialToolbar topAppBar;
    private boolean isAdmin;
    private String dni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fichaje);

        // dni desde login
        dni = getIntent().getStringExtra("DNI");
        // siempre modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //para que el status bar tenga iconos oscuros
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
        // toolbar
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayShowTitleEnabled(false);
        // logo siempre manda al dashboard
        ImageView logo = findViewById(R.id.logoText_toolbar);
        logo.setOnClickListener(v -> {
            Intent i = new Intent(this, DashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });
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
            if (isAdmin) {
                Intent intent = new Intent(this, InformesActivity.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Solo accesible por el administrador", Toast.LENGTH_SHORT).show();
            }
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
