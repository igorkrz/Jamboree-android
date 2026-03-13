package com.example.jamboree.model;

import java.util.List;

public class EventPage {
    private final List<Event> events;
    private final int totalItems;
    private final Integer firstPage;
    private final Integer lastPage;
    private final Integer nextPage;

    public EventPage(List<Event> events, int totalItems, Integer firstPage, Integer lastPage, Integer nextPage) {
        this.events = events;
        this.totalItems = totalItems;
        this.firstPage = firstPage;
        this.lastPage = lastPage;
        this.nextPage = nextPage;
    }

    public List<Event> getEvents() {
        return events;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public Integer getFirstPage() {
        return firstPage;
    }

    public Integer getLastPage() {
        return lastPage;
    }

    public Integer getNextPage() {
        return nextPage;
    }

    public boolean hasNextPage() {
        return nextPage != null;
    }
}
