package com.example.jamboree.model;

public class AuthResult {

    private final String user;
    private final String token;

    public AuthResult(String user, String token) {
        this.user = user;
        this.token = token;
    }

    public String getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
}
