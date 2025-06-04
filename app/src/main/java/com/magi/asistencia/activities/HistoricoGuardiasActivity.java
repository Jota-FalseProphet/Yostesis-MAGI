package com.magi.asistencia.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

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
    private boolean isAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_guardias);

        Bundle extras = getIntent().getExtras();
        String dni;
        boolean isAdmin;
        if (extras != null) {
            dni     = extras.getString("DNI", "");
            isAdmin = extras.getBoolean("IS_ADMIN", false);
        } else {
            dni     = "";
            isAdmin = false;
        }
        //ui
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
            intent.putExtras(extras);
            startActivity(intent);
            finish();
        });
        toolbar.findViewById(R.id.ic_menu_toolbar).setOnClickListener(this::showModulesMenu);

        //alert asi guapete hardcodeado porque tiempo no hay
        RecyclerView rv = findViewById(R.id.recyclerHistorico);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoricoAdapter(g -> {
            int grisOscuro = Color.parseColor("#333333");
            String titulo = "Guardia de " + g.getFechaGuardia();
            SpannableString tituloColoreado = new SpannableString(titulo);
            tituloColoreado.setSpan(new ForegroundColorSpan(grisOscuro), 0, titulo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            String mensaje =
                    "Asignada: " + g.getDniAsignat() + "\n" +
                            "Ausente:  " + g.getDniAbsent()  + "\n" +
                            "Grupo:    " + g.getGrupo()       + "\n" +
                            "Aula:     " + g.getAula();
            SpannableString mensajeColoreado = new SpannableString(mensaje);
            mensajeColoreado.setSpan(new ForegroundColorSpan(grisOscuro), 0, mensaje.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setTitle(tituloColoreado)
                    .setMessage(mensajeColoreado)
                    .setPositiveButton("OK", null)
                    .show();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            }
            Button btnPositivo = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            if (btnPositivo != null) {
                btnPositivo.setTextColor(Color.parseColor("#ffc913"));
            }
        });
        rv.setAdapter(adapter);


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
                Intent intent = new Intent(HistoricoGuardiasActivity.this, LoginActivity.class);
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
}
