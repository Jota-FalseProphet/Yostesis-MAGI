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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.magi.asistencia.R;
import com.magi.asistencia.adapters.GuardiaHistoricoAdapter;
import com.magi.asistencia.model.SessionHorario;
import com.magi.asistencia.network.HttpHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GuardiasActivity extends AppCompatActivity {

    private static final String TAG = "GuardiasTask";
    private static final String BASE_URL = "https://magi.it.com/api/guardias";
    private static final String PREFS = "MAGI_PREFS";
    private static final String PREF_DNI = "PREF_DNI";

    private MaterialToolbar topAppBar;
    private String dni;
    private boolean isAdmin;
    private GuardiaHistoricoAdapter adapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardias);

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        dni = getIntent().getStringExtra("DNI");
        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);
        Log.d("HISTORICO", "DNI="+dni+"  isAdmin="+isAdmin);
        if (dni == null) {
            dni = prefs.getString(PREF_DNI, null);
        } else {
            prefs.edit().putString(PREF_DNI, dni).apply();
        }
        if (dni == null) {
            Toast.makeText(this, "Error: no se ha obtenido el DNI del usuario", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(w, w.getDecorView())
                .setAppearanceLightStatusBars(true);
        View root = findViewById(R.id.activity_guardias_coordinator_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, sb.top, 0, 0);
            return insets;
        });

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

        RecyclerView rv = findViewById(R.id.recyclerGuardias);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GuardiaHistoricoAdapter(this::onGuardiaClick);
        rv.setAdapter(adapter);


        FloatingActionButton fab = findViewById(R.id.fabHistorico);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(GuardiasActivity.this,
                    HistoricoGuardiasActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
        });


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
                Intent intent = new Intent(GuardiasActivity.this, LoginActivity.class);
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
        private int code = -1;

        AsignarGuardiaTask(long id) {
            this.idSes = id;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (dni == null) {
                errorMsg = "DNI no disponible";
                return false;
            }
            HttpURLConnection c = null;
            try {
                String urlStr = BASE_URL
                        + "/asignar"
                        + "?dniAsignat=" + URLEncoder.encode(dni, "UTF-8")
                        + "&idSessio="  + idSes;
                URL url = new URL(urlStr);
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);

                code = c.getResponseCode();
                if (code == HttpURLConnection.HTTP_NO_CONTENT || code == HttpURLConnection.HTTP_OK) {
                    return true;
                } else {
                    InputStream errStream = c.getErrorStream();
                    if (errStream != null) {
                        String body = readStream(errStream);
                        try {
                            JSONObject errJson = new JSONObject(body);
                            errorMsg = errJson.optString("message", body);
                        } catch (JSONException ex) {
                            errorMsg = body;
                        }
                    } else {
                        errorMsg = c.getResponseMessage();
                    }
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        errorMsg = "No se puede asignar guardia: sesión finalizada";
                    }
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, "Error POST asignar", e);
                errorMsg = e.getMessage();
                return false;
            } finally {
                if (c != null) c.disconnect();
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (ok) {
                new CargarAusenciasTask().execute();
            } else {
                if (code == HttpURLConnection.HTTP_BAD_REQUEST) {
                    String toastMsg = (errorMsg != null && !errorMsg.isEmpty())
                            ? errorMsg
                            : "No se puede asignar guardia: sesión finalizada";
                    Toast.makeText(GuardiasActivity.this, toastMsg, Toast.LENGTH_LONG).show();
                } else {
                    mostrarError(errorMsg != null ? errorMsg : "Error al asignar (" + code + ")");
                }
            }
        }
    }

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

    private String readStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

}
