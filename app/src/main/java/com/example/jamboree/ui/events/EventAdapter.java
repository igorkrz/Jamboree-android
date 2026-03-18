package com.example.jamboree.ui.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.jamboree.R;
import com.example.jamboree.model.Event;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private final List<Event> events = new ArrayList<>();
    private final OnEventClickListener onEventClickListener;

    public EventAdapter(OnEventClickListener onEventClickListener) {
        this.onEventClickListener = onEventClickListener;
    }

    public void replaceEvents(List<Event> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    public void appendEvents(List<Event> newEvents) {
        int startPosition = events.size();
        events.addAll(newEvents);
        notifyItemRangeInserted(startPosition, newEvents.size());
    }

    public void clearEvents() {
        events.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.itemView.setOnClickListener(v -> {
            if (onEventClickListener != null) {
                onEventClickListener.onEventClick(event);
            }
        });

        holder.eventNameTextView.setText(event.getName());
        holder.eventDateTextView.setText(String.format("Date: %s", formatDate(event.getHoldingDate())));

        String venueText = event.getVenue();
        if (!event.getCity().isEmpty()) {
            if (!venueText.isEmpty()) {
                venueText += ", ";
            }
            venueText += event.getCity();
        }

        holder.eventVenueTextView.setText(
            venueText.isEmpty() ? "Venue: -" : "Venue: " + venueText
        );

        holder.eventPriceTextView.setText(
            event.getPrice().isEmpty() ? "Price: -" : "Price: " + event.getPrice() + " EUR"
        );

        holder.eventProviderTextView.setText(
            event.getProviderName().isEmpty()
                ? "Provider: -"
                : "Provider: " + event.getProviderName()
        );

        Glide.with(holder.itemView.getContext())
            .load(event.getImageUrl() == null || event.getImageUrl().isEmpty()
                ? R.drawable.placeholder_event
                : event.getImageUrl())
            .centerCrop()
            .placeholder(R.drawable.placeholder_event)
            .error(R.drawable.placeholder_event)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.eventImageView);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "-";

        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(rawDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            return dateTime.format(formatter);
        } catch (Exception e) {
            return rawDate;
        }
    }
}
