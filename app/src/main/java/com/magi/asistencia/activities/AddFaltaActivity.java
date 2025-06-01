package com.magi.asistencia.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.magi.asistencia.R;
import com.magi.asistencia.model.Docente;
import com.magi.asistencia.model.SesionDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddFaltaActivity extends AppCompatActivity {

    private static final String TAG       = "AddFaltaActivity";
    private static final String BASE_URL  = "https://magi.it.com/";

    private TextView      tvFecha;
    private Spinner       spinnerDocente;
    private Switch        switchFullDay;
    private TextView      tvSesionesLabel;
    private ChipGroup     chipGroupSesiones;
    private Spinner       spinnerMotivo;
    private MaterialButton btnGuardar;

    private LocalDate       fechaSeleccionada;
    private List<Docente>   listaDocentes  = new ArrayList<>();
    private List<SesionDTO> listaSesiones  = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private OkHttpClient httpClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_falta);


        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        View root = findViewById(R.id.add_falta_coordinator_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, sys.top, 0, 0);
            return insets;
        });


        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        findViewById(R.id.logoText_toolbar).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)));

        findViewById(R.id.ic_menu_toolbar).setOnClickListener(this::mostrarMenu);

        tvFecha         = findViewById(R.id.tvFecha);
        spinnerDocente  = findViewById(R.id.spinnerDocente);
        switchFullDay   = findViewById(R.id.switchFullDay);
        tvSesionesLabel = findViewById(R.id.tvSesionesLabel);
        chipGroupSesiones = findViewById(R.id.chipGroupSesiones);
        spinnerMotivo   = findViewById(R.id.spinnerMotivo);
        btnGuardar      = findViewById(R.id.btnGuardarFalta);

        httpClient = new OkHttpClient();


        switchFullDay.setChecked(false);
        tvSesionesLabel.setVisibility(View.VISIBLE);
        chipGroupSesiones.setVisibility(View.VISIBLE);


        fechaSeleccionada = LocalDate.now();
        actualizarTextoFecha();


        String[] motivosArray = new String[]{"BAJA_MEDICA", "PERMISO", "OTROS"};
        ArrayAdapter<String> adapterMotivo = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, motivosArray);
        adapterMotivo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMotivo.setAdapter(adapterMotivo);


        switchFullDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvSesionesLabel.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            chipGroupSesiones.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            if (!isChecked) recargarSesiones();
            validarCampos();
        });


        btnGuardar.setOnClickListener(v -> guardarAusencia());


        cargarDocentes();


        tvFecha.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Selecciona una fecha")
                    .setTheme(R.style.ThemeOverlay_CustomDatePicker)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Instant instant = Instant.ofEpochMilli(selection);
                fechaSeleccionada = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                actualizarTextoFecha();
                if (!switchFullDay.isChecked()) recargarSesiones();
                validarCampos();
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    private void actualizarTextoFecha() {
        tvFecha.setText("Fecha: " + fechaSeleccionada.format(formatter));
    }

    private void cargarDocentes() {
        String url = BASE_URL + "api/docentes";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error al conectar para listar docentes", e);
                runOnUiThread(() ->
                        Toast.makeText(AddFaltaActivity.this,
                                "Fallo al conectar con el servidor", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Lista docentes: código " + response.code());
                    runOnUiThread(() ->
                            Toast.makeText(AddFaltaActivity.this,
                                    "Error cargando docentes", Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    String body = response.body().string();
                    JSONArray arr = new JSONArray(body);
                    listaDocentes.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        int id = obj.getInt("id");
                        String nombre = obj.getString("nombre");
                        listaDocentes.add(new Docente(id, nombre));
                    }
                } catch (JSONException ex) {
                    Log.e(TAG, "JSON inválido al listar docentes", ex);
                }

                runOnUiThread(() -> {
                    ArrayAdapter<Docente> adapter = new ArrayAdapter<>(
                            AddFaltaActivity.this,
                            android.R.layout.simple_spinner_item,
                            listaDocentes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDocente.setAdapter(adapter);


                    spinnerDocente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (!switchFullDay.isChecked()) {
                                recargarSesiones();
                            }
                            validarCampos();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            validarCampos();
                        }
                    });


                    if (!switchFullDay.isChecked()) recargarSesiones();
                    validarCampos();
                });
            }
        });
    }

    private void recargarSesiones() {
        chipGroupSesiones.removeAllViews();
        listaSesiones.clear();

        if (spinnerDocente.getSelectedItem() == null) return;

        Docente docente = (Docente) spinnerDocente.getSelectedItem();
        String fechaStr = fechaSeleccionada.format(formatter);
        String url = BASE_URL + "api/sesiones?docenteId=" + docente.getId() + "&fecha=" + fechaStr;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error al conectar para listar sesiones", e);
                runOnUiThread(() ->
                        Toast.makeText(AddFaltaActivity.this,
                                "Fallo al cargar sesiones", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Lista sesiones: código " + response.code());
                    runOnUiThread(() ->
                            Toast.makeText(AddFaltaActivity.this,
                                    "Error cargando sesiones", Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    String body = response.body().string();
                    JSONArray arr = new JSONArray(body);
                    listaSesiones.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        int id = obj.getInt("id");
                        String horaDesde = obj.getString("horaDesde");
                        String horaFins = obj.getString("horaFins");
                        String grupo = obj.optString("grupo", null);
                        listaSesiones.add(new SesionDTO(id, horaDesde, horaFins, grupo));
                    }
                } catch (JSONException ex) {
                    Log.e(TAG, "JSON inválido al listar sesiones", ex);
                }

                runOnUiThread(() -> {
                    if (listaSesiones.isEmpty()) {
                        Toast.makeText(AddFaltaActivity.this,
                                "No hay sesiones para esa fecha", Toast.LENGTH_SHORT).show();
                    }
                    for (SesionDTO sesion : listaSesiones) {
                        Chip chip = new Chip(AddFaltaActivity.this);
                        chip.setText(sesion.toString());
                        chip.setCheckable(true);
                        chip.setCheckedIconVisible(true);
                        chip.setOnCheckedChangeListener((buttonView, isChecked) -> validarCampos());
                        chipGroupSesiones.addView(chip);
                    }
                    validarCampos();
                });
            }
        });
    }

    private void validarCampos() {
        boolean ok = spinnerDocente.getSelectedItem() != null;

        if (!switchFullDay.isChecked()) {
            int countChecked = 0;
            for (int i = 0; i < chipGroupSesiones.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupSesiones.getChildAt(i);
                if (chip.isChecked()) countChecked++;
            }
            if (countChecked == 0) ok = false;
        }

        btnGuardar.setEnabled(ok);
    }

    private void guardarAusencia() {
        Docente docente = (Docente) spinnerDocente.getSelectedItem();
        if (docente == null) return;

        String fechaStr   = fechaSeleccionada.format(formatter);
        boolean isFullDay = switchFullDay.isChecked();
        String motivo     = (String) spinnerMotivo.getSelectedItem();

        List<Integer> sesionesSeleccionadas = new ArrayList<>();
        if (!isFullDay) {
            for (int i = 0; i < chipGroupSesiones.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupSesiones.getChildAt(i);
                if (chip.isChecked()) {
                    SesionDTO s = listaSesiones.get(i);
                    sesionesSeleccionadas.add(s.getId());
                }
            }
        }

        JSONObject json = new JSONObject();
        try {
            json.put("idDocente", docente.getId());
            json.put("fecha", fechaStr);
            json.put("fullDay", isFullDay);
            json.put("motivo", motivo);
            JSONArray arrSes = new JSONArray();
            for (Integer idSes : sesionesSeleccionadas) {
                arrSes.put(idSes);
            }
            json.put("sesiones", arrSes);
        } catch (JSONException e) {
            Log.e(TAG, "Error creando JSON de CrearAusenciaDTO", e);
            runOnUiThread(() ->
                    Toast.makeText(this, "Error interno", Toast.LENGTH_SHORT).show());
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url(BASE_URL + "api/ausencias")
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error al conectar para crear ausencia", e);
                runOnUiThread(() ->
                        Toast.makeText(AddFaltaActivity.this,
                                "Fallo al conectar con el servidor", Toast.LENGTH_LONG).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 201) {
                    try {
                        String respBody = response.body().string();
                        JSONObject obj = new JSONObject(respBody);
                        final int idAus = obj.getInt("idAusencia");
                        runOnUiThread(() -> {
                            Toast.makeText(AddFaltaActivity.this,
                                    "Ausencia creada (ID=" + idAus + ")",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        });
                    } catch (JSONException ex) {
                        Log.e(TAG, "JSON inválido en respuesta CrearAusencia", ex);
                        runOnUiThread(() -> {
                            Toast.makeText(AddFaltaActivity.this,
                                    "Ausencia creada, pero no se leyó ID",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }
                } else if (response.code() == 409) {
                    runOnUiThread(() ->
                            Toast.makeText(AddFaltaActivity.this,
                                    "Ya existe una ausencia para esta fecha",
                                    Toast.LENGTH_LONG).show());
                } else {
                    String mensaje = "Error: " + response.code();
                    Log.e(TAG, "Crear ausencia devolvió " + response.code() + ": "
                            + response.message());
                    runOnUiThread(() ->
                            Toast.makeText(AddFaltaActivity.this,
                                    mensaje,
                                    Toast.LENGTH_LONG).show());
                }
            }
        });
    }


    private void mostrarMenu(View anchor) {
        ContextThemeWrapper wrap = new ContextThemeWrapper(this, R.style.ThemeOverlay_PopupMAGI);
        androidx.appcompat.widget.PopupMenu pop = new androidx.appcompat.widget.PopupMenu(wrap, anchor);
        pop.inflate(R.menu.menu_dashboard);
        for (int i = 0; i < pop.getMenu().size(); i++) {
            MenuItem m = pop.getMenu().getItem(i);
            if (m.getIcon() != null) m.getIcon().setTint(
                    ContextCompat.getColor(this, R.color.amarillo_magi));
        }
        pop.setOnMenuItemClickListener(item -> onOptionsItemSelected(item));
        pop.show();
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
                Intent intent = new Intent(AddFaltaActivity.this, LoginActivity.class);
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
