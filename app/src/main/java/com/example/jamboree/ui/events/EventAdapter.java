package com.example.jamboree.ui.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.jamboree.R;
import com.example.jamboree.model.Event;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {

    private final List<Event> events = new ArrayList<>();

    public void setEvents(List<Event> newEvents) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new EventDiffCallback(events, newEvents));
        events.clear();
        events.addAll(newEvents);
        diffResult.dispatchUpdatesTo(this);
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

    private static class EventDiffCallback extends DiffUtil.Callback {
        private final List<Event> oldList;
        private final List<Event> newList;

        EventDiffCallback(List<Event> oldList, List<Event> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return Objects.equals(
                oldList.get(oldItemPosition).getId(),
                newList.get(newItemPosition).getId()
            );
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Event oldItem = oldList.get(oldItemPosition);
            Event newItem = newList.get(newItemPosition);

            return Objects.equals(oldItem.getId(), newItem.getId()) &&
                Objects.equals(oldItem.getName(), newItem.getName()) &&
                Objects.equals(oldItem.getHoldingDate(), newItem.getHoldingDate()) &&
                Objects.equals(oldItem.getVenue(), newItem.getVenue()) &&
                Objects.equals(oldItem.getCity(), newItem.getCity()) &&
                Objects.equals(oldItem.getPrice(), newItem.getPrice()) &&
                Objects.equals(oldItem.getProviderName(), newItem.getProviderName()) &&
                Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl());
        }
    }
}
