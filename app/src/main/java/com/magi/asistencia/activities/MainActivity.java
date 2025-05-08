package com.magi.asistencia.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.magi.asistencia.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    /* Usa el mismo host y puerto que LoginActivity */
    private static final String BASE_URL = "https://159.69.215.108/api/fichaje";

    private String dni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dni = getIntent().getStringExtra("DNI");

        Button btnIn  = findViewById(R.id.btnIn);
        Button btnOut = findViewById(R.id.btnOut);

        btnIn.setOnClickListener(v -> new FichajeTask("start").execute());
        btnOut.setOnClickListener(v -> new FichajeTask("end").execute());
    }

    private class FichajeTask extends AsyncTask<Void, Void, Boolean> {

        private final String tipo;
        private int code = -1;

        FichajeTask(String tipo) { this.tipo = tipo.toLowerCase(Locale.ROOT); }

        @Override protected Boolean doInBackground(Void... p) {
            try {
                URL url = new URL(BASE_URL + "/" + tipo + "?dni=" + dni);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);

                code = c.getResponseCode();
                return code == HttpURLConnection.HTTP_OK;
            } catch (Exception e) { e.printStackTrace(); }
            return false;
        }

        @Override protected void onPostExecute(Boolean ok) {
            if (ok) {
                Toast.makeText(MainActivity.this,
                        tipo.equals("start") ? "Entrada registrada" : "Salida registrada",
                        Toast.LENGTH_SHORT).show();
            } else if (code == HttpURLConnection.HTTP_CONFLICT) {
                Toast.makeText(MainActivity.this,
                        "Fichaje no válido", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this,
                        "Error de red (" + code + ")", Toast.LENGTH_LONG).show();
            }
        }
    }
}
