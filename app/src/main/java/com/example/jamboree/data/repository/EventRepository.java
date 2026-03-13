package com.example.jamboree.data.repository;

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

public class EventRepository {

    private final ApiClient apiClient = new ApiClient();

    public EventPage getUpcomingEvents(int page) throws Exception {
        String endpoint = "events?page=" + page + "&holdingDate%5Bafter%5D=today";
        ApiResponse response = apiClient.get(endpoint);

        if (response.isObject()) {
            return parseJsonLdResponse(response.getObject());
        }

        if (response.isArray()) {
            return parsePlainArrayResponse(response.getArray(), page);
        }

        throw new Exception("Unexpected API response format");
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
        List<Event> events = parseEventsArray(members);

        return new EventPage(events, totalItems, firstPage, lastPage, nextPage);
    }

    private EventPage parsePlainArrayResponse(JSONArray array, int currentPage) throws Exception {
        List<Event> events = parseEventsArray(array);
        Integer nextPage = events.isEmpty() ? null : currentPage + 1;

        return new EventPage(events, events.size(), 1, null, nextPage);
    }

    private List<Event> parseEventsArray(JSONArray array) throws Exception {
        List<Event> events = new ArrayList<>();
        if (array == null) {
            return events;
        }

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
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
