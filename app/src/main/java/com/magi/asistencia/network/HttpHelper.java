package com.magi.asistencia.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpHelper {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .build();

    public static String get(String url) throws IOException {
        Request req = new Request.Builder()
                .url(url)
                .build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful())
                throw new IOException("Código " + resp.code());
            return resp.body().string();
        }
    }

    public static boolean post(String url) throws IOException {
        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(new byte[0]))
                .build();
        try (Response resp = client.newCall(req).execute()) {
            return resp.isSuccessful();
        }
    }

    public static boolean postJson(String url, String json) throws IOException {
        URL u = new URL(url);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setRequestMethod("POST");
        c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        c.setDoOutput(true);

        try (OutputStream os = c.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = c.getResponseCode();
        c.disconnect();
        return code >= 200 && code < 300;
    }

}
