package com.example.jamboree.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jamboree.R;
import com.example.jamboree.model.CalendarEvent;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventViewHolder> {

    private final List<CalendarEvent> events = new ArrayList<>();

    public void replaceEvents(List<CalendarEvent> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_event, parent, false);
        return new CalendarEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarEventViewHolder holder, int position) {
        CalendarEvent event = events.get(position);

        holder.titleTextView.setText(event.getTitle());
        holder.dateTextView.setText(formatDate(event.getStart()));
        holder.allDayTextView.setText(event.isAllDay() ? "All day" : "");
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
