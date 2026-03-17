package com.example.jamboree.data.parser;

import com.example.jamboree.model.Event;
import com.example.jamboree.model.EventPage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventParser {
    public EventPage parseEventPageFromCollection(JSONObject root) throws Exception {
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

    public List<Event> parseEventsArray(JSONArray array) throws Exception {
        List<Event> events = new ArrayList<>();

        if (array == null) {
            return events;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            events.add(parseEvent(item));
        }

        return events;
    }

    public Event parseEvent(JSONObject item) {
        JSONObject eventObject = unwrapEventObject(item);

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

        return new Event(
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
        );
    }

    private JSONObject unwrapEventObject(JSONObject item) {
        if (item == null) {
            return new JSONObject();
        }

        JSONObject nestedEvent = item.optJSONObject("event");
        if (nestedEvent != null) {
            return nestedEvent;
        }

        return item;
    }

    public Integer extractPageNumber(String url) {
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

    public String cleanText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
