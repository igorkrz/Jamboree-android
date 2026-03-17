package com.example.jamboree.data.repository;

import android.content.Context;

import com.example.jamboree.data.local.SessionManager;
import com.example.jamboree.data.remote.ApiClient;
import com.example.jamboree.data.remote.ApiResponse;
import com.example.jamboree.data.remote.UnauthorizedException;
import com.example.jamboree.model.CalendarEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CalendarRepository {
    private static final String CALENDAR_ENDPOINT = "calendar";
    private final ApiClient apiClient = new ApiClient();
    private final SessionManager sessionManager;
    private final AuthRepository authRepository;

    public CalendarRepository(Context context) throws Exception {
        Context appContext = context.getApplicationContext();
        this.sessionManager = new SessionManager(appContext);
        this.authRepository = new AuthRepository(appContext);
    }

    public List<CalendarEvent> getCalendarEvents(String startDate) throws Exception {
        if (!sessionManager.isLoggedIn()) {
            throw new Exception("User is not authenticated.");
        }

        String endpoint = CALENDAR_ENDPOINT + "?start=" + startDate;

        return authRepository.executeWithAutoReLogin(accessToken -> {
            ApiResponse response = apiClient.get(endpoint, accessToken);

            if (response.getStatusCode() == 401) {
                throw new UnauthorizedException("Access token expired.");
            }

            if (!response.isObject()) {
                throw new Exception("Unexpected calendar response format.");
            }

            return parseCalendarEvents(response.getObject());
        });
    }

    private List<CalendarEvent> parseCalendarEvents(JSONObject root) throws Exception {
        List<CalendarEvent> events = new ArrayList<>();
        JSONArray members = root.optJSONArray("member");

        if (members == null) {
            return events;
        }

        for (int i = 0; i < members.length(); i++) {
            JSONObject item = members.getJSONObject(i);

            String title = item.optString("title", "Untitled");
            String start = item.optString("start", "");
            boolean allDay = item.optBoolean("allDay", false);
            String resourceId = item.optString("resourceId", "");
            String url = item.optString("url", "");

            events.add(new CalendarEvent(title, start, allDay, resourceId, url));
        }

        return events;
    }
}
