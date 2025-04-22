package com.magi.asistencia.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.magi.asistencia.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * LoginActivity sin Retrofit.
 * Hace un POST /api/login usando HttpURLConnection.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etDni, etPass;
    private Button btnLogin;

    // Cambia la IP o dominio por el de tu servidor Hetzner
    private static final String LOGIN_URL = "http://<IP_HETZNER>:8080/api/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etDni = findViewById(R.id.editDni);
        etPass = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.buttonLogin);

        btnLogin.setOnClickListener(v -> {
            String dni = etDni.getText().toString().trim();
            String pass = etPass.getText().toString();

            if (dni.isEmpty() || pass.isEmpty()) {
                toast("Introduce DNI y contraseña");
            } else {
                new LoginTask(dni, pass).execute();
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Tarea asíncrona que realiza la petición HTTP sin bloquear el hilo UI.
     */
    private class LoginTask extends AsyncTask<Void, Void, String> {
        private final String dni;
        private final String pass;
        private int responseCode = -1;

        LoginTask(String dni, String pass) {
            this.dni = dni;
            this.pass = pass;
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(LOGIN_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("dni", dni);
                body.put("password", pass);

                try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")) {
                    writer.write(body.toString());
                    writer.flush();
                }

                responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                // La API devuelve un String con comillas, ej. "ADMIN" -> ADMIN
                return sb.toString().replace("\"", "").trim();

            } catch (Exception e) {
                return null;
            } finally {
                if (conn != null) conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String role) {
            if (responseCode == 200 && role != null && !role.isEmpty()) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class)
                        .putExtra("ROL", role)
                        .putExtra("DNI", dni));
                finish();
            } else if (responseCode == 401) {
                toast("Credenciales incorrectas");
            } else {
                toast("Sin conexión");
            }
        }
    }
}