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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.magi.asistencia.R;
import com.magi.asistencia.adapters.GuardiaAdapter;
import com.magi.asistencia.model.SessionHorario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity del módulo Guardias – sin Retrofit, 100 % Java (HttpURLConnection),
 * replicando la barra y status bar de FichajeActivity.
 */
public class GuardiasActivity extends AppCompatActivity {

    private static final String TAG = "GuardiasTask";

    private static final String BASE_URL = "https://magi.it.com/api/guardias";

    private MaterialToolbar topAppBar;
    private String dni;

    private GuardiaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardias);

        // —— DNI desde Login ——
        dni = getIntent().getStringExtra("DNI");

        // —— Modo claro ——
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // —— Edge-to-edge + status bar blanca ——
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(w, w.getDecorView())
                .setAppearanceLightStatusBars(true);

        // Padding top al Coordinator
        View root = findViewById(R.id.activity_guardias_coordinator_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, sb.top, 0, 0);
            return insets;
        });

        // —— Toolbar (logo + menú) ——
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayShowTitleEnabled(false);

        ImageView logo = findViewById(R.id.logoText_toolbar);
        logo.setOnClickListener(v -> {
            Intent i = new Intent(this, DashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });

        ImageView menuIcon = findViewById(R.id.ic_menu_toolbar);
        menuIcon.setOnClickListener(this::showModulesMenu);

        // —— RecyclerView + FAB ——
        RecyclerView rv = findViewById(R.id.recyclerGuardias);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuardiaAdapter(this::onGuardiaClick);
        rv.setAdapter(adapter);

        List<SessionHorario> mock = Arrays.asList(
                new SessionHorario(1,"1º ESO B","B-12","09:55","10:50",false,null),
                new SessionHorario(2,"2º ESO C","A-14","10:50","11:45",true,"12345678A")
        );
        adapter.setData(mock);


        ExtendedFloatingActionButton fab = findViewById(R.id.fabHistorico);
        fab.setOnClickListener(v -> startActivity(new Intent(this, HistoricoGuardiasActivity.class)));

        // Primera descarga
        new CargarAusenciasTask().execute();
    }

    /**
     * Menú emergente (idéntico al de FichajeActivity).
     */
    private void showModulesMenu(View anchor) {
        Context wrapper = new ContextThemeWrapper(this, R.style.ThemeOverlay_PopupMAGI);
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(wrapper, anchor);
        popup.inflate(R.menu.menu_dashboard);
        for (int i = 0; i < popup.getMenu().size(); i++) {
            MenuItem mi = popup.getMenu().getItem(i);
            if (mi.getIcon() != null)
                mi.getIcon().setTint(ContextCompat.getColor(this, R.color.amarillo_magi));
        }
        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popup.show();
    }

    // ————————————————————— NETWORK TASKS —————————————————————

    private class CargarAusenciasTask extends AsyncTask<Void, Void, List<SessionHorario>> {
        private String errorMsg;
        @Override
        protected List<SessionHorario> doInBackground(Void... p) {
            HttpURLConnection c = null;
            try {
                String urlStr = BASE_URL + "/ausencias/vigentes";
                Log.d(TAG, "GET " + urlStr);
                URL url = new URL(urlStr);
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);

                int code = c.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    return parseLista(sb.toString());
                } else {
                    errorMsg = c.getResponseMessage();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error GET ausencias", e);
                errorMsg = e.getMessage();
            } finally {
                if (c != null) c.disconnect();
            }
            return null;
        }
        @Override protected void onPostExecute(List<SessionHorario> lista) {
            if (lista != null) {
                adapter.setData(lista);
            } else {
                mostrarError(errorMsg != null ? errorMsg : "Error de red");
            }
        }
    }

    private class AsignarGuardiaTask extends AsyncTask<Void, Void, Boolean> {
        private final long idSes;
        private String errorMsg;
        AsignarGuardiaTask(long id){ this.idSes = id; }
        @Override protected Boolean doInBackground(Void... p){
            HttpURLConnection c = null;
            try {
                String urlStr = BASE_URL + "?dniAsignat="+dni+"&idSessio="+idSes;
                Log.d(TAG, "POST " + urlStr);
                URL url = new URL(urlStr);
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);
                int code = c.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) return true;
                errorMsg = c.getResponseMessage();
            } catch (Exception e){
                Log.e(TAG,"Error POST asignar",e);
                errorMsg = e.getMessage();
            } finally { if(c!=null) c.disconnect(); }
            return false;
        }
        @Override protected void onPostExecute(Boolean ok){
            if(ok){ new CargarAusenciasTask().execute(); }
            else mostrarError(errorMsg!=null? errorMsg : "Error al asignar");
        }
    }

    // ————————————————————— UI EVENTS —————————————————————

    private void onGuardiaClick(SessionHorario s){
        if(Boolean.TRUE.equals(s.getCubierta())){
            Toast.makeText(this, "Ya está cubierta", Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("¿Te asignas esta guardia?")
                .setMessage(s.getGrupo()+" · Aula "+s.getAula()+" ("+s.getHoraInicio()+")")
                .setPositiveButton("Sí", (d,w)-> new AsignarGuardiaTask(s.getIdSessio()).execute())
                .setNegativeButton("No", null)
                .show();
    }

    private void mostrarError(String m){
        Snackbar.make(findViewById(android.R.id.content), m, Snackbar.LENGTH_LONG).show();
    }

    // ————————————————————— JSON —————————————————————
    // ————————————————————— JSON —————————————————————
    private List<SessionHorario> parseLista(String json) throws JSONException {
        JSONArray arr = new JSONArray(json);
        List<SessionHorario> lista = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);

            SessionHorario s = new SessionHorario(
                    o.getLong("idSessio"),
                    o.optString("plantilla", "—"),   // grupo → plantilla
                    null,                            // aula aún no llega
                    o.optString("horaDesde"),        // horaInicio
                    o.optString("horaFins"),         // horaFin
                    o.optBoolean("cubierta", false),
                    null                             // profesorGuardia
            );
            lista.add(s);
        }
        return lista;
    }

}
