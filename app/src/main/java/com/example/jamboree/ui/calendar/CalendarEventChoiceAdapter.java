package com.example.jamboree.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jamboree.R;
import com.example.jamboree.model.CalendarEvent;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarEventChoiceAdapter extends RecyclerView.Adapter<CalendarEventChoiceAdapter.ViewHolder> {
    public interface OnCalendarEventClickListener {
        void onCalendarEventClick(CalendarEvent event);
    }

    private final List<CalendarEvent> events;
    private final OnCalendarEventClickListener listener;

    public CalendarEventChoiceAdapter(List<CalendarEvent> events, OnCalendarEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_event_choice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarEvent event = events.get(position);

        holder.titleTextView.setText(event.getTitle());
        holder.dateTextView.setText(formatDate(event.getStart()));

        holder.itemView.setOnClickListener(v -> listener.onCalendarEventClick(event));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "-";

        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(rawDate);
            return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return rawDate;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView dateTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
