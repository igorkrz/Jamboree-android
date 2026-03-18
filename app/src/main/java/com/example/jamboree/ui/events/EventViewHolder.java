package com.example.jamboree.ui.events;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jamboree.R;

public class EventViewHolder extends RecyclerView.ViewHolder {
    public final ImageView eventImageView;
    public final TextView eventNameTextView;
    public final TextView eventDateTextView;
    public final TextView eventVenueTextView;
    public final TextView eventPriceTextView;
    public final TextView eventProviderTextView;
    public final ImageButton favoriteButton;

    public EventViewHolder(@NonNull View itemView) {
        super(itemView);

        eventImageView = itemView.findViewById(R.id.eventImageView);
        eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
        eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
        eventVenueTextView = itemView.findViewById(R.id.eventVenueTextView);
        eventPriceTextView = itemView.findViewById(R.id.eventPriceTextView);
        eventProviderTextView = itemView.findViewById(R.id.eventProviderTextView);
        favoriteButton = itemView.findViewById(R.id.favoriteButton);
    }
}
