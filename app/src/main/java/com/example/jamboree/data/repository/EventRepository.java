package com.example.jamboree.data.repository;


import com.example.jamboree.data.remote.ApiClient;
import com.example.jamboree.model.Event;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EventRepository {

    private final ApiClient apiClient = new ApiClient();

    public List<Event> getUpcomingEvents(int page) throws Exception {
        String endpoint = "events?page=" + page + "&holdingDate%5Bafter%5D=today";
        String response = apiClient.get(endpoint);
        String trimmed = response.trim();

        if (!trimmed.startsWith("[")) {
            throw new Exception("Expected JSON array but got: " +
                trimmed.substring(0, Math.min(trimmed.length(), 200)
            ));
        }

        JSONArray array = new JSONArray(trimmed);

        List<Event> events = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);

            String id = item.optString("id", "");
            String name = item.optString("name", "Unnamed event");
            String description = item.optString("description", "");
            String price = item.optString("price", "");
            String url = item.optString("url", "");
            String imageUrl = item.optString("imageUrl", "");
            String holdingDate = item.optString("holdingDate", "");

            String providerName = "";
            JSONObject provider = item.optJSONObject("provider");
            if (provider != null) {
                providerName = provider.optString("name", "");
            }

            String venue = "";
            String city = "";
            JSONObject location = item.optJSONObject("location");
            if (location != null) {
                venue = location.optString("venue", "");
                city = location.optString("city", "");
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
}
