package com.example.jamboree.data.repository;

import android.content.Context;

import com.example.jamboree.data.local.SessionManager;
import com.example.jamboree.data.remote.ApiClient;
import com.example.jamboree.data.remote.ApiResponse;
import com.example.jamboree.model.Event;
import com.example.jamboree.model.EventPage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserEventRepository {
    private static final int PAGE_SIZE = 30;

    private final ApiClient apiClient = new ApiClient();
    private final SessionManager sessionManager;

    public UserEventRepository(Context context) throws Exception {
        this.sessionManager = new SessionManager(context.getApplicationContext());
    }

    public EventPage getUserEvents(int page) throws Exception {
        String accessToken = sessionManager.getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            throw new Exception("User is not authenticated.");
        }

        String endpoint = "user_events?page=" + page + "&itemsPerPage=" + PAGE_SIZE;
        ApiResponse response = apiClient.get(endpoint, accessToken);

        if (response.isObject()) {
            return parseJsonLdResponse(response.getObject());
        }

        if (response.isArray()) {
            return parsePlainArrayResponse(response.getArray(), page);
        }

        throw new Exception("Unexpected user events response format.");
    }

    private EventPage parseJsonLdResponse(JSONObject root) throws Exception {
        int totalItems = root.optInt("totalItems", 0);

        Integer firstPage = null;
        Integer lastPage = null;
        Integer nextPage = null;

        JSONObject view = root.optJSONObject("view");
        if (view != null) {
            firstPage = extractPageNumber(view.optString("first", null));
            lastPage = extractPageNumber(view.optString("last", null));
            nextPage = extractPageNumber(view.optString("next", null));
        }

        JSONArray members = root.optJSONArray("member");
        List<Event> events = parseUserEventsArray(members);

        return new EventPage(events, totalItems, firstPage, lastPage, nextPage);
    }

    private EventPage parsePlainArrayResponse(JSONArray array, int currentPage) throws Exception {
        List<Event> events = parseUserEventsArray(array);

        Integer firstPage = 1;
        Integer lastPage = null;
        Integer nextPage = events.size() < PAGE_SIZE ? null : currentPage + 1;

        return new EventPage(events, events.size(), firstPage, lastPage, nextPage);
    }

    private List<Event> parseUserEventsArray(JSONArray array) throws Exception {
        List<Event> events = new ArrayList<>();

        if (array == null) {
            return events;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject userEventItem = array.getJSONObject(i);
            JSONObject eventObject = userEventItem.optJSONObject("event");

            if (eventObject == null) {
                continue;
            }

            String id = eventObject.optString("id", "");
            String name = eventObject.optString("name", "Unnamed event");
            String description = eventObject.optString("description", "");
            String price = eventObject.optString("price", "");
            String url = eventObject.optString("url", "");
            String imageUrl = eventObject.optString("imageUrl", "");
            String holdingDate = eventObject.optString("holdingDate", "");

            String providerName = "";
            JSONObject provider = eventObject.optJSONObject("provider");
            if (provider != null) {
                providerName = provider.optString("name", "");
            }

            String venue = "";
            String city = "";
            JSONObject location = eventObject.optJSONObject("location");
            if (location != null) {
                venue = cleanText(location.optString("venue", ""));
                city = cleanText(location.optString("city", ""));
            }

            events.add(new Event(
                id,
                name,
                description,
                price,
                url,
                imageUrl,
                holdingDate,
                providerName,
                venue,
                city
            ));
        }

        return events;
    }

    private Integer extractPageNumber(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile("[?&]page=(\\d+)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return Integer.parseInt(Objects.requireNonNull(matcher.group(1)));
        }

        return null;
    }

    private String cleanText(String value) {
        return value == null ? "" : value.trim();
    }
}
