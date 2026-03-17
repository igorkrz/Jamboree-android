package com.example.jamboree.data.remote;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://10.0.2.2/api/";

    public ApiResponse get(String endpoint) throws Exception {
        return request("GET", endpoint, null, null);
    }

    public ApiResponse get(String endpoint, String accessToken) throws Exception {
        return request("GET", endpoint, null, accessToken);
    }

    public ApiResponse postJson(String endpoint, JSONObject body) throws Exception {
        return request("POST", endpoint, body, null);
    }

    public ApiResponse postJson(String endpoint, JSONObject body, String accessToken) throws Exception {
        return request("POST", endpoint, body, accessToken);
    }

    public ApiResponse request(String method, String endpoint, JSONObject body, String accessToken) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept", "application/json+ld");

        if (accessToken != null && !accessToken.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        }

        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            byte[] output = body.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(output);
                os.flush();
            }
        }

        int responseCode = connection.getResponseCode();
        String contentType = connection.getContentType();

        InputStream stream = responseCode >= 200 && responseCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream();

        String bodyText = "";

        if (stream != null) {
            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }
            bodyText = responseBuilder.toString().trim();
        }

        connection.disconnect();

        Log.d(TAG, "URL: " + url);
        Log.d(TAG, "HTTP code: " + responseCode);
        Log.d(TAG, "Content-Type: " + contentType);
        Log.d(TAG, "Response body:\n" + bodyText);

        if (!contentType.contains("application/json")) {
            throw new Exception("Expected JSON-LD but got: " + contentType);
        }

        return new ApiResponse(new JSONObject(bodyText), contentType, responseCode);
    }
}
