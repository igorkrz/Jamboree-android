package com.example.jamboree.model;

public class Event {
    private final String id;
    private final String name;
    private final String description;
    private final String price;
    private final String url;
    private final String imageUrl;
    private final String holdingDate;
    private final String providerName;
    private final String venue;
    private final String city;

    public Event(
        String id,
        String name,
        String description,
        String price,
        String url,
        String imageUrl,
        String holdingDate,
        String providerName,
        String venue,
        String city
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.url = url;
        this.imageUrl = imageUrl;
        this.holdingDate = holdingDate;
        this.providerName = providerName;
        this.venue = venue;
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getHoldingDate() {
        return holdingDate;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getVenue() {
        return venue;
    }

    public String getCity() {
        return city;
    }
}
