package com.example.jamboree.data.remote;

import org.json.JSONObject;

public class ApiResponse {

    private final JSONObject object;
    private final String contentType;
    private final int statusCode;

    public ApiResponse(JSONObject object, String contentType, int statusCode) {
        this.object = object;
        this.contentType = contentType;
        this.statusCode = statusCode;
    }

    public boolean isObject() {
        return object != null;
    }

    public JSONObject getObject() {
        return object;
    }

    public String getContentType() {
        return contentType;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
