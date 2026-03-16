package com.example.jamboree.model;

public class AuthResult {
    private final String user;
    private final String accessToken;
    private final String refreshToken;

    public AuthResult(String user, String accessToken, String refreshToken) {
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getUser() {
        return user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
