package com.example.jamboree.data.repository;

import com.example.jamboree.data.parser.EventParser;
import com.example.jamboree.data.remote.ApiClient;
import com.example.jamboree.data.remote.ApiResponse;
import com.example.jamboree.model.EventPage;

public class EventRepository {
    private static final String EVENT_ENDPOINT = "events";
    private final ApiClient apiClient = new ApiClient();
    private final EventParser eventParser = new EventParser();

    public EventPage getUpcomingEvents(int page) throws Exception {
        String endpoint = EVENT_ENDPOINT + "?page=" + page + "&holdingDate%5Bafter%5D=today";
        ApiResponse response = apiClient.get(endpoint);

        if (!response.isObject()) {
            throw new Exception("Unexpected API response format");
        }

        return eventParser.parseEventPageFromCollection(response.getObject());
    }
}
