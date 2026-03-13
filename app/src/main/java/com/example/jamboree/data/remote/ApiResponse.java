package com.example.jamboree.data.remote;

import org.json.JSONArray;
import org.json.JSONObject;

public class ApiResponse {

    private final JSONObject object;
    private final JSONArray array;
    private final String contentType;
    private final int statusCode;

    public ApiResponse(JSONObject object, String contentType, int statusCode) {
        this.object = object;
        this.array = null;
        this.contentType = contentType;
        this.statusCode = statusCode;
    }

    public ApiResponse(JSONArray array, String contentType, int statusCode) {
        this.array = array;
        this.object = null;
        this.contentType = contentType;
        this.statusCode = statusCode;
    }

    public boolean isObject() {
        return object != null;
    }

    public boolean isArray() {
        return array != null;
    }

    public JSONObject getObject() {
        return object;
    }

    public JSONArray getArray() {
        return array;
    }

    public String getContentType() {
        return contentType;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
