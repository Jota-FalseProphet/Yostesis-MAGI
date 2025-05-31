package com.magi.asistencia.activities;

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
import android.widget.ImageView;
import android.widget.Toast;

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

import com.magi.asistencia.R;
import com.magi.asistencia.adapters.InformeAdapter;
import com.magi.asistencia.model.Informe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * InformesActivity: muestra el listado de informes de “faltas” (JSON) y permite
 * descargar un PDF si el usuario pulsa el FAB.
 *
 * Comparte el mismo header (logo + menú) y configuración de status bar que DashboardActivity.
 */
public class InformesActivity extends AppCompatActivity {

    private static final String TAG = "InformesTask";
    private static final String BASE_URL = "https://magi.it.com/api/informes/faltas";
    private static final String PREFS = "MAGI_PREFS";
    private static final String PREF_DNI = "PREF_DNI";

    private RecyclerView recyclerView;
    private InformeAdapter adapter;
    private SharedPreferences prefs;
    private String dni;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informes);

        // ────────────────────────────────────────────────────────────────────────────────
        // 1) Siempre en modo claro para esta pantalla
        // ────────────────────────────────────────────────────────────────────────────────
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // ────────────────────────────────────────────────────────────────────────────────
        // 2) Edge-to-edge + status bar blanca + iconos oscuros
        // ────────────────────────────────────────────────────────────────────────────────
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));
        new WindowInsetsControllerCompat(window, window.getDecorView())
                .setAppearanceLightStatusBars(true);

        // ────────────────────────────────────────────────────────────────────────────────
        // 3) Ajustar padding top al CoordinatorLayout raíz para no solapar el Toolbar
        // ────────────────────────────────────────────────────────────────────────────────
        View root = findViewById(R.id.activity_informes_coordinator_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBars.top, 0, 0);
            return insets;
        });

        // ────────────────────────────────────────────────────────────────────────────────
        // 4) Guardar/recuperar DNI e IS_ADMIN (igual que GuardiasActivity)
        // ────────────────────────────────────────────────────────────────────────────────
        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        dni = getIntent().getStringExtra("DNI");
        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);
        Log.d(TAG, "DNI=" + dni + "  isAdmin=" + isAdmin);
        if (dni == null) {
            // Si no viene DNI en el Intent, lo leemos de SharedPreferences
            dni = prefs.getString(PREF_DNI, null);
        } else {
            // Si viene DNI desde Login, lo guardamos para próximas sesiones
            prefs.edit().putString(PREF_DNI, dni).apply();
        }

        // ────────────────────────────────────────────────────────────────────────────────
        // 5) Header: logo (clic para volver a Dashboard) + menú desplegable
        // ────────────────────────────────────────────────────────────────────────────────
        ImageView logo = findViewById(R.id.logoText_toolbar);
        logo.setOnClickListener(v -> {
            Intent i = new Intent(this, DashboardActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        });

        ImageView menuIcon = findViewById(R.id.ic_menu_toolbar);
        menuIcon.setOnClickListener(this::showModulesMenu);

        // ────────────────────────────────────────────────────────────────────────────────
        // 6) RecyclerView: configurar LayoutManager y Adapter
        // ────────────────────────────────────────────────────────────────────────────────
        recyclerView = findViewById(R.id.recyclerInformes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InformeAdapter(this::onInformeClick);
        recyclerView.setAdapter(adapter);

        // ────────────────────────────────────────────────────────────────────────────────
        // 7) Lanzar tarea asíncrona para obtener JSON de /api/informes/faltas
        //    y poblar el RecyclerView
        // ────────────────────────────────────────────────────────────────────────────────
        new FetchInformesTask().execute();

        // ────────────────────────────────────────────────────────────────────────────────
        // 8) Configurar FAB para descargar PDF de todo el listado (como en Guardias)
        // ────────────────────────────────────────────────────────────────────────────────
        findViewById(R.id.fabInformes).setOnClickListener(v -> {
            String pdfUrl = BASE_URL + "?periodo=ULTIMOS30D&formato=PDF";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(android.net.Uri.parse(pdfUrl));
            startActivity(i);
        });
    }

    /**
     * Despliega el menú lateral (módulos) tal como en GuardiasActivity.
     */
    private void showModulesMenu(View anchor) {
        Context wrapper = new ContextThemeWrapper(this, R.style.ThemeOverlay_PopupMAGI);
        androidx.appcompat.widget.PopupMenu popup =
                new androidx.appcompat.widget.PopupMenu(wrapper, anchor);
        popup.inflate(R.menu.menu_dashboard);
        for (int i = 0; i < popup.getMenu().size(); i++) {
            MenuItem mi = popup.getMenu().getItem(i);
            if (mi.getIcon() != null) {
                mi.getIcon().setTint(ContextCompat.getColor(this, R.color.amarillo_magi));
            }
        }
        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popup.show();
    }

    /**
     * Cuando el usuario pulsa una fila de informe, por ejemplo, para ver detalle.
     */
    private void onInformeClick(Informe informe) {
        // TODO: manejar pulsación sobre un informe (abrir detalle, compartir, etc.)
    }

    /**
     * AsyncTask que consulta el endpoint /api/informes/faltas para obtener JSON con la lista.
     * Luego parsea el JSON y actualiza el Adapter.
     */
    private class FetchInformesTask extends AsyncTask<Void, Void, List<Informe>> {

        private String errorMsg = null;

        @Override
        protected List<Informe> doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            try {
                // Construir URL básica (sin filtros): devuelve un List<InformeDetalle> en JSON
                String urlStr = BASE_URL;
                URL url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    errorMsg = "Código HTTP: " + connection.getResponseCode();
                    return null;
                }

                String json = readStream(connection.getInputStream());
                return parseInformes(json);

            } catch (Exception e) {
                Log.e(TAG, "Error al obtener informes", e);
                errorMsg = e.getMessage();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(List<Informe> informes) {
            // Detener cualquier animación de refresco si la hubiera (opcional)
            // Ejemplo: swipeRefreshLayout.setRefreshing(false);

            if (informes != null) {
                adapter.submitList(informes);
            } else {
                // Mostrar Toast del error o placeholder vacío
                Toast.makeText(InformesActivity.this,
                        "Error al cargar informes: " + (errorMsg != null ? errorMsg : "desconocido"),
                        Toast.LENGTH_LONG).show();
            }
        }

        /**
         * Parsea el JSON y construye la lista de objetos Informe.
         * Ajusta los campos según tu modelo real.
         */
        private List<Informe> parseInformes(String jsonStr) throws Exception {
            List<Informe> lista = new ArrayList<>();
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Informe inf = new Informe();
                inf.setId(obj.getInt("id"));
                inf.setDocente(obj.getString("docente"));
                inf.setGrupo(obj.getString("grupo"));
                inf.setFecha(obj.getString("fecha"));       // asume formato “yyyy-MM-dd”
                inf.setTotalFaltas(obj.getInt("totalFaltas"));
                lista.add(inf);
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

    /**
     * Opcional: manejar elementos del menú desplegable (menu_dashboard.xml).
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Copia aquí la misma lógica que tenías en GuardiasActivity:
        // switch (item.getItemId()) { ... }
        // Por ejemplo:
        // case R.id.nav_guardias: ...
        // case R.id.nav_informes: // ya estamos aquí, no hacer nada
        // ...
        return super.onOptionsItemSelected(item);
    }
}
