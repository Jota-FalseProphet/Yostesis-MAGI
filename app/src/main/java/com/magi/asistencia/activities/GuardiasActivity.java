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
import com.magi.asistencia.network.HttpHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        // —— Forzar modo claro ——
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // —— Edge-to-edge + status bar blanca ——
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(w, w.getDecorView())
                .setAppearanceLightStatusBars(true);

        // Ajustar padding top del root para status bar
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

        // —— RecyclerView + Adapter ——
        RecyclerView rv = findViewById(R.id.recyclerGuardias);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuardiaAdapter(this::onGuardiaClick);
        rv.setAdapter(adapter);

        // —— FAB Histórico ——
        ExtendedFloatingActionButton fab = findViewById(R.id.fabHistorico);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, HistoricoGuardiasActivity.class))
        );

        // —— Primera carga de datos ——
        new CargarAusenciasTask().execute();
    }

    private void showModulesMenu(View anchor) {
        Context wrapper = new ContextThemeWrapper(this, R.style.ThemeOverlay_PopupMAGI);
        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(wrapper, anchor);
        popup.inflate(R.menu.menu_dashboard);
        for (int i = 0; i < popup.getMenu().size(); i++) {
            MenuItem mi = popup.getMenu().getItem(i);
            if (mi.getIcon() != null)
                mi.getIcon().setTint(ContextCompat.getColor(this, R.color.amarillo_magi));
        }
        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popup.show();
    }

    // ————————————————— NETWORK TASKS —————————————————

    private class CargarAusenciasTask extends AsyncTask<Void, Void, List<SessionHorario>> {
        private String errorMsg;

        @Override
        protected List<SessionHorario> doInBackground(Void... voids) {
            try {
                String json = HttpHelper.get(BASE_URL + "/ausencias/vigentes");
                return parseLista(json);
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error GET ausencias", e);
                errorMsg = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<SessionHorario> lista) {
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

        AsignarGuardiaTask(long id) {
            this.idSes = id;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                return HttpHelper.post(BASE_URL + "?dniAsignat=" + dni + "&idSessio=" + idSes);
            } catch (IOException e) {
                Log.e(TAG, "Error POST asignar", e);
                errorMsg = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (ok) {
                new CargarAusenciasTask().execute();
            } else {
                mostrarError(errorMsg != null ? errorMsg : "Error al asignar");
            }
        }
    }

    // ————————————————— UI EVENTS —————————————————

    private void onGuardiaClick(SessionHorario s) {
        if (Boolean.TRUE.equals(s.getCubierta())) {
            Toast.makeText(this, "Guardia ya cubierta", Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("¿Te asignas esta guardia?")
                .setMessage(
                        s.getGrupo() + " · Aula " + s.getAula() +
                                " (" + s.getHoraInicio() + "–" + s.getHoraFin() + ")"
                )
                .setPositiveButton("Sí", (d, w) ->
                        new AsignarGuardiaTask(s.getIdSessio()).execute()
                )
                .setNegativeButton("No", null)
                .show();
    }

    private void mostrarError(String msg) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
    }

    // ————————————————— JSON —————————————————

    private List<SessionHorario> parseLista(String json) throws JSONException {
        JSONArray arr = new JSONArray(json);
        List<SessionHorario> lista = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            SessionHorario s = new SessionHorario(
                    o.getLong("idSessio"),
                    o.optString("grupo", "—"),
                    o.optString("aula", "—"),
                    o.optString("horaDesde"),
                    o.optString("horaHasta"),
                    o.optBoolean("cubierta", false),
                    o.optString("profesorGuardia", null)
            );
            lista.add(s);
        }
        return lista;
    }
}
