package com.example.jamboree.data.repository;

import android.content.Context;

import com.example.jamboree.data.local.SessionManager;
import com.example.jamboree.data.remote.ApiClient;
import com.example.jamboree.data.remote.ApiResponse;
import com.example.jamboree.data.remote.UnauthorizedException;
import com.example.jamboree.model.AuthResult;

import org.json.JSONObject;

public class AuthRepository {
    public interface AuthenticatedRequest<T> {
        T execute(String accessToken) throws Exception;
    }

    private static final String LOGIN_ENDPOINT = "login";
    private final ApiClient apiClient = new ApiClient();
    private final SessionManager sessionManager;

    public AuthRepository(Context context) throws Exception {
        this.sessionManager = new SessionManager(context.getApplicationContext());
    }

    public AuthResult login(String username, String password) throws Exception {
        AuthResult authResult = authenticate(username, password);
        sessionManager.saveSession(authResult.getUser(), username, password, authResult.getToken());

        return authResult;
    }

    public boolean reAuthenticate() throws Exception {
        String username = sessionManager.getUsername();
        String password = sessionManager.getPassword();

        if (isBlank(username) || isBlank(password)) {
            sessionManager.clearSession();
            return false;
        }

        try {
            AuthResult authResult = authenticate(username, password);
            sessionManager.updateAccessToken(authResult.getToken());
            return true;
        } catch (Exception e) {
            sessionManager.clearSession();
            return false;
        }
    }

    public <T> T executeWithAutoReLogin(AuthenticatedRequest<T> request) throws Exception {
        String accessToken = requireAccessToken();

        try {
            return request.execute(accessToken);
        } catch (UnauthorizedException e) {
            if (!reAuthenticate()) {
                throw new Exception("Session expired. Please log in again.");
            }

            return request.execute(requireAccessToken());
        }
    }

    private AuthResult authenticate(String username, String password) throws Exception {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", password);

        ApiResponse response = apiClient.postJson(LOGIN_ENDPOINT, body);
        JSONObject json = requireJsonObjectResponse(response, "Login failed");

        String token = json.optString("token", "");
        if (isBlank(token)) {
            throw new Exception("Login failed: missing token.");
        }

        String user = json.optString("user", username);
        return new AuthResult(user, token);
    }

    private JSONObject requireJsonObjectResponse(ApiResponse response, String errorPrefix) throws Exception {
        if (response.getStatusCode() >= 400) {
            throw new Exception(errorPrefix + ". HTTP " + response.getStatusCode());
        }

        if (!response.isObject()) {
            throw new Exception(errorPrefix + ": unexpected response format.");
        }

        return response.getObject();
    }

    private String requireAccessToken() throws Exception {
        String accessToken = sessionManager.getAccessToken();
        if (isBlank(accessToken)) {
            throw new Exception("User is not authenticated.");
        }
        return accessToken;
    }

    private boolean isBlank(String value) {
        return value == null || value.isEmpty();
    }
}
