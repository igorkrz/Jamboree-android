package com.example.jamboree.ui.calendar;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jamboree.R;

public class CalendarEventViewHolder extends RecyclerView.ViewHolder {
    TextView titleTextView;
    TextView dateTextView;
    TextView allDayTextView;

    public CalendarEventViewHolder(@NonNull View itemView) {
        super(itemView);
        titleTextView = itemView.findViewById(R.id.titleTextView);
        dateTextView = itemView.findViewById(R.id.dateTextView);
        allDayTextView = itemView.findViewById(R.id.allDayTextView);
    }
}
