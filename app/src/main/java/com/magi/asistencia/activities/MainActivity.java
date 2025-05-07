package com.magi.asistencia.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.magi.asistencia.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {



    private static final String BASE_URL = "http://159.69.215.108:80/";

    private String dni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dni = getIntent().getStringExtra("DNI");

        Button btnIn  = findViewById(R.id.btnIn);
        Button btnOut = findViewById(R.id.btnOut);

        btnIn.setOnClickListener(v -> new FichajeTask("in").execute());
        btnOut.setOnClickListener(v -> new FichajeTask("out").execute());
    }


    private class FichajeTask extends AsyncTask<Void,Void,Boolean> {

        private final String tipo;
        private int responseCode = -1;

        FichajeTask(String tipo){ this.tipo = tipo.toLowerCase(Locale.ROOT); }

        @Override protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(BASE_URL + tipo + "?dni=" + dni);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);


                con.setDoOutput(true);
                try(OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream())){
                    out.write("{}");
                    out.flush();
                }

                responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    try(BufferedReader br =
                                new BufferedReader(new InputStreamReader(con.getInputStream()))){
                        br.readLine();
                    }
                    return true;
                }
            } catch (Exception e){ e.printStackTrace(); }
            return false;
        }

        @Override protected void onPostExecute(Boolean ok) {
            if (ok) {
                Toast.makeText(MainActivity.this,
                        tipo.equals("in") ? "Entrada registrada" : "Salida registrada",
                        Toast.LENGTH_SHORT).show();
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                Toast.makeText(MainActivity.this,
                        "Fichaje no válido (¿dos IN seguidos?)", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this,
                        "Error de red ("+responseCode+")", Toast.LENGTH_LONG).show();
            }
        }
    }
}
