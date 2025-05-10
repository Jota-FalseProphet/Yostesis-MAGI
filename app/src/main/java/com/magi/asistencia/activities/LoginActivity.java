package com.magi.asistencia.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.magi.asistencia.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LoginActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://magi.it.com/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText editDni      = findViewById(R.id.editDni);
        EditText editPassword = findViewById(R.id.editPassword);
        Button   buttonLogin  = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(v -> {
            String dni  = editDni.getText().toString().trim();
            String pass = editPassword.getText().toString().trim();
            if (dni.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Introduce DNI y contraseña", Toast.LENGTH_SHORT).show();
            } else {
                new LoginTask(dni, pass).execute();
            }
        });
    }
    private static void trustAllCertificates(HttpsURLConnection conn) {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String auth) {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String auth) {}
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAll, new java.security.SecureRandom());
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier((HostnameVerifier) (hostname, session) -> true);
        } catch (Exception e) {
            Log.w("LoginActivity", "Error confiando en certificados", e);
        }
    }

    private class LoginTask extends AsyncTask<Void, Void, String> {
        private final String dni, pass;
        private int code = -1;

        LoginTask(String dni, String pass) {
            this.dni = dni;
            this.pass = pass;
        }

        @Override
        protected String doInBackground(Void... v) {
            try {
                URL url = new URL(BASE_URL + "/login");
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

                // <<< Ignoramos SSL y hostname >>>
                trustAllCertificates(con);

                // Enviamos JSON, tal como espera Spring Boot (@RequestBody)
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setDoOutput(true);

                // Construimos el JSON con dni y password
                JSONObject body = new JSONObject();
                body.put("dni", dni);
                body.put("password", pass);

                // Escribimos el JSON en el cuerpo
                try (OutputStream os = con.getOutputStream()) {
                    byte[] out = body.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(out);
                    os.flush();
                }

                code = con.getResponseCode();

                // Leemos tanto input como error stream para ver la respuesta completa
                InputStreamReader isr = (code >= 400)
                        ? new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8)
                        : new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);

                try (BufferedReader br = new BufferedReader(isr)) {
                    StringBuilder resp = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        resp.append(line);
                    }
                    Log.d("LoginActivity", "Respuesta servidor (" + code + "): " + resp);
                    return resp.toString();
                }

            } catch (Exception e) {
                Log.e("LoginActivity", "Error en conexión", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String rol) {
            if (code == HttpURLConnection.HTTP_OK && rol != null) {
                Intent i = new Intent(LoginActivity.this, DashboardActivity.class)
                        .putExtra("ROL", rol)
                        .putExtra("DNI", dni);
                startActivity(i);
                finish();

            } else if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Toast.makeText(LoginActivity.this,
                        "Credenciales incorrectas", Toast.LENGTH_SHORT).show();

            } else {
                String msg = "Error de red (" + code + ")";
                if (rol != null && !rol.isEmpty()) {
                    msg += ": " + rol;
                }
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        }
    }
}
