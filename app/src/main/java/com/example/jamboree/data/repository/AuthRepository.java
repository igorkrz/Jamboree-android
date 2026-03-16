package com.example.jamboree.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.jamboree.data.local.SessionManager;
import com.example.jamboree.data.remote.ApiClient;
import com.example.jamboree.data.remote.ApiResponse;
import com.example.jamboree.model.AuthResult;

import org.json.JSONObject;

public class AuthRepository {

    private final ApiClient apiClient = new ApiClient();
    private final SessionManager sessionManager;

    public AuthRepository(Context context) throws Exception {
        this.sessionManager = new SessionManager(context.getApplicationContext());
    }

    public AuthResult login(String email, String password) throws Exception {
        JSONObject body = new JSONObject();
        body.put("username", email);
        body.put("password", password);

        ApiResponse response = apiClient.postJson("security/login_check", body);

        Log.d("test", body.toString());
        Log.d("test", response.toString());

        if (!response.isObject()) {
            throw new Exception("Unexpected login response format");
        }

        JSONObject json = response.getObject();

        String user = json.optString("user", "");
        String accessToken = json.optString("access_token", "");
        String refreshToken = json.optString("refresh_token", "");

        if (accessToken.isEmpty()) {
            throw new Exception("Login failed: missing access token");
        }

        sessionManager.saveSession(user, accessToken, refreshToken);

        return new AuthResult(user, accessToken, refreshToken);
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public String getAccessToken() {
        return sessionManager.getAccessToken();
    }

    public void logout() {
        sessionManager.clearSession();
    }
}