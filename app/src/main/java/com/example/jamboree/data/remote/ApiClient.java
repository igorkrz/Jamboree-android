package com.example.jamboree.data.remote;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://10.0.2.2/api/";

    public String get(String endpoint) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        String contentType = connection.getContentType();

        BufferedReader reader;
        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        Log.d(TAG, "URL: " + url);
        Log.d(TAG, "HTTP code: " + responseCode);
        Log.d(TAG, "Content-Type: " + contentType);
        Log.d(TAG, "Response body:\n" + response);

        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("HTTP " + responseCode + ": " + response);
        }

        return response.toString();
    }
}
