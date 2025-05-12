package com.magi.asistencia.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.magi.asistencia.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class FichajeActivity extends AppCompatActivity {

    private static final String TAG = "FichajeTask";
    private static final String BASE_URL = "https://magi.it.com/api/fichaje";

    private String dni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fichaje);

        dni = getIntent().getStringExtra("DNI");

        Button btnIn  = findViewById(R.id.btnIn);
        Button btnOut = findViewById(R.id.btnOut);

        btnIn.setOnClickListener(v -> new FichajeTask("start").execute());
        btnOut.setOnClickListener(v -> new FichajeTask("end").execute());
    }

    private class FichajeTask extends AsyncTask<Void, Void, Boolean> {

        private final String tipo;
        private int code = -1;
        private String errorMsg = null;  // mensaje de error desde el backend (me ahorro codigo)

        FichajeTask(String tipo) {
            this.tipo = tipo.toLowerCase(Locale.ROOT);
        }

        @Override
        protected Boolean doInBackground(Void... p) {
            HttpURLConnection c = null;
            try {
                String urlStr = BASE_URL + "/" + tipo + "?dni=" + dni;
                Log.d(TAG, "Llamando a URL: " + urlStr);
                URL url = new URL(urlStr);
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(5000);
                c.setReadTimeout(5000);

                code = c.getResponseCode();
                Log.d(TAG, "Código HTTP: " + code + " – " + c.getResponseMessage());

                if (code == HttpURLConnection.HTTP_OK) {
                    return true;
                } else {
                    // Leer el cuerpo de error
                    InputStream err = c.getErrorStream();
                    if (err != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(err));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();
                        try {
                            JSONObject obj = new JSONObject(sb.toString());
                            errorMsg = obj.optString("error", sb.toString());
                        } catch (JSONException je) {
                            errorMsg = sb.toString();
                        }
                    } else {
                        errorMsg = c.getResponseMessage();
                    }
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al ejecutar petición de fichaje", e);
                errorMsg = e.getMessage();
                return false;
            } finally {
                if (c != null) c.disconnect();
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (ok) {
                Toast.makeText(FichajeActivity.this,
                        tipo.equals("start") ? "Entrada registrada" : "Salida registrada",
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, tipo + " registrado con éxito");
            } else {
                // Mostrar mensaje de error capturado desde el backend
                if (errorMsg != null && !errorMsg.isEmpty()) {
                    Toast.makeText(FichajeActivity.this,
                            errorMsg,
                            Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Error API: " + errorMsg);
                } else if (code == HttpURLConnection.HTTP_CONFLICT) {
                    Toast.makeText(FichajeActivity.this,
                            "Fichaje no válido", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Fichaje no válido, código: " + code);
                } else {
                    Toast.makeText(FichajeActivity.this,
                            "Error de red (" + code + ")", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Error de red, código: " + code);
                }
            }
        }
    }
}
