package com.example.jamboree.data.repository;

import android.content.Context;

import com.example.jamboree.data.local.SessionManager;
import com.example.jamboree.data.parser.EventParser;
import com.example.jamboree.data.remote.ApiClient;
import com.example.jamboree.data.remote.ApiResponse;
import com.example.jamboree.data.remote.UnauthorizedException;
import com.example.jamboree.model.EventPage;

public class UserEventRepository {
    private static final String USER_EVENT_ENDPOINT = "user_events";
    private final ApiClient apiClient = new ApiClient();
    private final SessionManager sessionManager;
    private final AuthRepository authRepository;
    private final EventParser eventParser = new EventParser();

    public UserEventRepository(Context context) throws Exception {
        Context appContext = context.getApplicationContext();
        this.sessionManager = new SessionManager(appContext);
        this.authRepository = new AuthRepository(appContext);
    }

    public EventPage getUserEvents(int page) throws Exception {
        if (!sessionManager.isLoggedIn()) {
            throw new Exception("User is not authenticated.");
        }

        String endpoint = USER_EVENT_ENDPOINT + "?page=" + page;

        return authRepository.executeWithAutoReLogin(accessToken -> {
            ApiResponse response = apiClient.get(endpoint, accessToken);

            if (response.getStatusCode() == 401) {
                throw new UnauthorizedException("Access token expired.");
            }

            if (!response.isObject()) {
                throw new Exception("Unexpected user events response format.");
            }

            return eventParser.parseEventPageFromCollection(response.getObject());
        });
    }
}
