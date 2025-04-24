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

public class LoginActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://159.69.215.108:8080/api/login";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText editDni       = findViewById(R.id.editDni);
        EditText editPassword  = findViewById(R.id.editPassword);
        Button   buttonLogin   = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(v -> {
            String dni  = editDni.getText().toString().trim();
            String pass = editPassword.getText().toString().trim();

            if (dni.isEmpty() || pass.isEmpty()) {
                toast("Introduce DNI y contraseña");
                return;
            }
            new LoginTask(dni, pass).execute();
        });
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }

    /*-----------------------------------------------------*/
    /* AsyncTask para hacer POST /api/login                */
    /*-----------------------------------------------------*/
    private class LoginTask extends AsyncTask<Void, Void, String> {
        private final String dni, pass;
        private int responseCode = -1;

        LoginTask(String dni, String pass){ this.dni = dni; this.pass = pass; }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(BASE_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("dni", dni);
                body.put("password", pass);

                try(OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream())){
                    out.write(body.toString());
                    out.flush();
                }

                responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try(BufferedReader br =
                                new BufferedReader(new InputStreamReader(con.getInputStream()))){
                        return br.readLine();        // rol en texto llano
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(String rol) {
            if (responseCode == HttpURLConnection.HTTP_OK && rol != null) {
                Intent i = new Intent(LoginActivity.this, MainActivity.class)
                        .putExtra("ROL", rol)
                        .putExtra("DNI", dni);
                startActivity(i);
                finish();

            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                toast("Credenciales incorrectas");
            } else {
                toast("Sin conexión");
            }
        }
    }
}
