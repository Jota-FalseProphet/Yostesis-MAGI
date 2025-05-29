package com.magi.asistencia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

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
import com.google.android.material.snackbar.Snackbar;
import com.magi.asistencia.R;
import com.magi.asistencia.adapters.HistoricoAdapter;
import com.magi.asistencia.model.GuardiaHistorico;
import com.magi.asistencia.network.HttpHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HistoricoGuardiasActivity extends AppCompatActivity {
    private HistoricoAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_guardias);

        // ——— Leer extras de sesión ———
        Bundle extras = getIntent().getExtras();
        String dni;
        boolean isAdmin;
        if (extras != null) {
            // Si en tus Intents estás usando la clave "DNI"
            dni     = extras.getString("DNI", "");
            isAdmin = extras.getBoolean("IS_ADMIN", false);
        } else {
            // Fallback por si acaso
            dni     = "";
            isAdmin = false;
        }

        // ——— UI setup ———
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);
        View root = findViewById(R.id.activity_historico_guardias_coordinator_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, sb.top, 0, 0);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.findViewById(R.id.logoText_toolbar).setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // opcional: reenviar extras si el Dashboard los necesita
            intent.putExtras(extras);
            startActivity(intent);
            finish();
        });
        toolbar.findViewById(R.id.ic_menu_toolbar).setOnClickListener(this::showModulesMenu);

        // ——— RecyclerView + Adapter ———
        RecyclerView rv = findViewById(R.id.recyclerHistorico);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoricoAdapter(g -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Guardia de " + g.getFechaGuardia())
                    .setMessage(
                            "Asignada: " + g.getDniAsignat() + "\n" +
                                    "Ausente:  " + g.getDniAbsent()  + "\n" +
                                    "Grupo:    " + g.getGrupo()       + "\n" +
                                    "Aula:     " + g.getAula()
                    )
                    .setPositiveButton("OK", null)
                    .show();
        });
        rv.setAdapter(adapter);

        // ——— Carga de datos ———
        String endpoint;
        if (isAdmin) {
            endpoint = "https://magi.it.com/api/guardias/historico?admin=true";
        } else {
            String encDni = URLEncoder.encode(dni, StandardCharsets.UTF_8);
            endpoint = "https://magi.it.com/api/guardias/historico?dni=" + encDni;
        }
        new CargarHistoricoTask(endpoint).execute();

        Log.d("HISTORICO", "DNI="+dni+"  isAdmin="+isAdmin);

    }


    private class CargarHistoricoTask extends android.os.AsyncTask<Void, Void, List<GuardiaHistorico>> {
        private final String endpoint;
        private String errorMsg;

        CargarHistoricoTask(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        protected List<GuardiaHistorico> doInBackground(Void... voids) {
            try {
                String json = HttpHelper.get(endpoint);
                json = json.trim();
                List<GuardiaHistorico> lista = new ArrayList<>();

                if (json.startsWith("{")) {
                    // Admin: {"dni1":[...], "dni2":[...], ...}
                    JSONObject root = new JSONObject(json);
                    Iterator<String> keys = root.keys();
                    while (keys.hasNext()) {
                        String absentDni = keys.next();
                        JSONArray arr = root.getJSONArray(absentDni);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            lista.add(GuardiaHistorico.fromJson(o, absentDni));

                        }
                    }
                } else {
                    // Docente: [...]
                    JSONArray arr = new JSONArray(json);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        lista.add(GuardiaHistorico.fromJson(o, null));
                    }
                }
                return lista;

            } catch (Exception e) {
                errorMsg = e.getMessage();
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<GuardiaHistorico> data) {
            if (data != null) {
                adapter.setData(data);
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        errorMsg != null ? errorMsg : "Error cargando histórico",
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void showModulesMenu(View v) {
        new MaterialAlertDialogBuilder(this)
                .setMessage("Aquí el menú de módulos…")
                .setPositiveButton("OK", null)
                .show();
    }
}
