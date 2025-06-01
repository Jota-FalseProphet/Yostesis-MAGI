// src/main/java/com/magi/asistencia/activities/AddFaltaActivity.java
package com.magi.asistencia.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

        tvFecha         = findViewById(R.id.tvFecha);
        spinnerDocente  = findViewById(R.id.spinnerDocente);
        switchFullDay   = findViewById(R.id.switchFullDay);
        tvSesionesLabel = findViewById(R.id.tvSesionesLabel);
        chipGroupSesiones = findViewById(R.id.chipGroupSesiones);
        spinnerMotivo   = findViewById(R.id.spinnerMotivo);
        btnGuardar      = findViewById(R.id.btnGuardarFalta);

        httpClient = new OkHttpClient();

        // 1. Fecha por defecto = hoy
        fechaSeleccionada = LocalDate.now();
        actualizarTextoFecha();

        // 2. Spinner motivos (array fijo)
        String[] motivosArray = new String[]{"BAJA_MEDICA", "PERMISO", "OTROS"};
        ArrayAdapter<String> adapterMotivo = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, motivosArray);
        adapterMotivo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMotivo.setAdapter(adapterMotivo);

        // 3. Switch “Todo el día”
        switchFullDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvSesionesLabel.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            chipGroupSesiones.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            if (!isChecked) recargarSesiones();
            validarCampos();
        });

        // 4. Botón Guardar
        btnGuardar.setOnClickListener(v -> guardarAusencia());

        // 5. Cargar lista de docentes
        cargarDocentes();

        // 6. DatePicker para fecha
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

                    // 5.1 Listener para refrescar sesiones al cambiar docente
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

                    // Al cargar docentes por primera vez, recargamos si no es fullDay
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
}
