package com.magi.asistencia.network;

import java.io.IOException;
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
}
