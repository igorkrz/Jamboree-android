package com.example.jamboree.data.repository;

import android.content.Context;

import com.example.jamboree.data.local.SessionManager;
import com.example.jamboree.data.parser.EventParser;
import com.example.jamboree.data.remote.ApiClient;
import com.example.jamboree.data.remote.ApiResponse;
import com.example.jamboree.model.EventPage;

public class UserEventRepository {
    private final ApiClient apiClient = new ApiClient();
    private final SessionManager sessionManager;
    private final EventParser eventParser = new EventParser();

    public UserEventRepository(Context context) throws Exception {
        this.sessionManager = new SessionManager(context.getApplicationContext());
    }

    public EventPage getUserEvents(int page) throws Exception {
        String accessToken = sessionManager.getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            throw new Exception("User is not authenticated.");
        }

        String endpoint = "user_events?page=" + page;
        ApiResponse response = apiClient.get(endpoint, accessToken);

        if (response.isObject()) {
            throw new Exception("Unexpected user events response format.");
        }

        return eventParser.parseEventPageFromCollection(response.getObject());
    }
}
