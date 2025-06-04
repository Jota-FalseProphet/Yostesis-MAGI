package com.magi.asistencia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.magi.asistencia.R;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateUserActivity extends AppCompatActivity {

    private static final String TAG      = "CreateUserActivity";
    private static final String BASE_URL = "https://magi.it.com/"; // Ajusta al endpoint real

    private TextInputEditText editDni;
    private TextInputEditText editNombreCompleto;
    private TextInputEditText editPassword;
    private TextInputEditText editRepeatPassword;
    private View                buttonRegister;
    private View                tvLoginLink;

    private OkHttpClient httpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blanco));

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(true);

        View root = findViewById(R.id.activity_create_user_constraint_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, sys.top, 0, 0);
            return insets;
        });

        editDni            = findViewById(R.id.editDniCreate);
        editNombreCompleto = findViewById(R.id.editNombreCreate);
        editPassword       = findViewById(R.id.editPasswordCreate);
        editRepeatPassword = findViewById(R.id.editRepeatPasswordCreate);
        buttonRegister     = findViewById(R.id.buttonRegisterCreate);
        tvLoginLink        = findViewById(R.id.tvLoginLink);

        httpClient = new OkHttpClient();

        buttonRegister.setOnClickListener(v -> {
            String dni       = editDni.getText().toString().trim();
            String nombre    = editNombreCompleto.getText().toString().trim();
            String pass      = editPassword.getText().toString().trim();
            String passRepet = editRepeatPassword.getText().toString().trim();

            // Validaciones locales
            if (dni.isEmpty() || nombre.isEmpty() || pass.isEmpty() || passRepet.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!dni.matches("^[0-9]{8}[A-Za-z]$")) {
                Toast.makeText(this, "DNI inválido. Debe ser 8 dígitos y una letra", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pass.length() < 8) {
                Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass.equals(passRepet)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            crearUsuarioEnBackend(dni, nombre, pass);
        });

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(CreateUserActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void crearUsuarioEnBackend(String dni, String nombre, String pass) {
        String url = BASE_URL + "api/docentes";

        try {
            JSONObject json = new JSONObject();
            json.put("dni", dni);
            json.put("nombreCompleto", nombre);
            json.put("contrasena", pass);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error al conectar con el servidor", e);
                    runOnUiThread(() ->
                            Toast.makeText(CreateUserActivity.this,
                                    "Fallo al conectar con el servidor", Toast.LENGTH_LONG).show());
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    int code = response.code();
                    String respBody = response.body() != null ? response.body().string() : "";

                    runOnUiThread(() -> {
                        if (code == 201) {
                            Toast.makeText(CreateUserActivity.this,
                                    "Registro exitoso. Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(CreateUserActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                        } else if (code == 409) {
                            Toast.makeText(CreateUserActivity.this,
                                    "Error: ya existe un usuario con ese DNI.", Toast.LENGTH_LONG).show();
                        } else {
                            String msg = "Error de red (" + code + ")";
                            if (!respBody.isEmpty()) {
                                msg += ": " + respBody;
                            }
                            Toast.makeText(CreateUserActivity.this,
                                    msg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error formando JSON de registro", e);
            Toast.makeText(this, "Error interno al preparar datos", Toast.LENGTH_SHORT).show();
        }
    }
}
