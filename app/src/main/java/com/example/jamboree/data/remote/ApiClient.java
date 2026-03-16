package com.example.jamboree.data.remote;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            byte[] output = body.toString().getBytes(StandardCharsets.UTF_8);
            OutputStream os = connection.getOutputStream();
            os.write(output);
            os.flush();
            os.close();
        }

        if (accessToken != null && !accessToken.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        }

        int responseCode = connection.getResponseCode();

        InputStream stream = responseCode >= 200 && responseCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();

        String bodyText = response.toString().trim();
        String contentType = connection.getHeaderField("Content-Type");

        Log.d(TAG, "URL: " + url);
        Log.d(TAG, "HTTP code: " + responseCode);
        Log.d(TAG, "Content-Type: " + contentType);
        Log.d(TAG, "Response body:\n" + response);

        if (contentType != null && contentType.contains("application/json+ld")) {
            return new ApiResponse(new JSONObject(bodyText), contentType, responseCode);
        }

        throw new Exception("Expected JSON-LD but got: " + contentType);
    }
}
