package com.example.jamboree.model;

public class CalendarEvent {
    private final String title;
    private final String start;
    private final boolean allDay;
    private final String resourceId;
    private final String url;

    public CalendarEvent(String title, String start, boolean allDay, String resourceId, String url) {
        this.title = title;
        this.start = start;
        this.allDay = allDay;
        this.resourceId = resourceId;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getStart() {
        return start;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getUrl() {
        return url;
    }
}
