package com.example.jamboree.ui.events;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.jamboree.R;
import com.example.jamboree.data.repository.EventRepository;
import com.example.jamboree.model.Event;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class EventDetailsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";

    private ImageView eventImageView;
    private TextView eventTitleTextView;
    private TextView eventDateTextView;
    private TextView eventVenueTextView;
    private TextView eventPriceTextView;
    private TextView eventProviderTextView;
    private TextView eventDescriptionTextView;
    private ProgressBar progressBar;
    private TextView errorTextView;

    private final EventRepository eventRepository = new EventRepository();

    public EventDetailsFragment() {
        super(R.layout.fragment_event_details);
    }

    public static Bundle createArgs(String eventId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_EVENT_ID, eventId);
        return bundle;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventImageView = view.findViewById(R.id.eventImageView);
        eventTitleTextView = view.findViewById(R.id.eventTitleTextView);
        eventDateTextView = view.findViewById(R.id.eventDateTextView);
        eventVenueTextView = view.findViewById(R.id.eventVenueTextView);
        eventPriceTextView = view.findViewById(R.id.eventPriceTextView);
        eventProviderTextView = view.findViewById(R.id.eventProviderTextView);
        eventDescriptionTextView = view.findViewById(R.id.eventDescriptionTextView);
        progressBar = view.findViewById(R.id.progressBar);
        errorTextView = view.findViewById(R.id.errorTextView);

        String eventId = null;
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString(ARG_EVENT_ID);
        }

        if (eventId == null || eventId.isEmpty()) {
            showError("Missing event ID.");
            return;
        }

        loadEvent(eventId);
    }

    private void loadEvent(String eventId) {
        showLoading();

        new Thread(() -> {
            try {
                Event event = eventRepository.getEventById(eventId);
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> showEvent(event));
            } catch (Exception e) {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> showError("Failed to load event: " + e.getMessage()));
            }
        }).start();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
    }

    private void showEvent(Event event) {
        progressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);

        eventTitleTextView.setText(event.getName());
        eventDateTextView.setText(formatDate(event.getHoldingDate()));

        String venueText = event.getVenue();
        if (!event.getCity().isEmpty()) {
            if (!venueText.isEmpty()) {
                venueText += ", ";
            }
            venueText += event.getCity();
        }

        eventVenueTextView.setText(venueText.isEmpty() ? "Venue: -" : "Venue: " + venueText);
        eventPriceTextView.setText(event.getPrice().isEmpty() ? "Price: -" : "Price: " + event.getPrice() + " EUR");
        eventProviderTextView.setText(event.getProviderName().isEmpty() ? "Provider: -" : "Provider: " + event.getProviderName());
        eventDescriptionTextView.setText(event.getDescription().isEmpty() ? "No description available." : event.getDescription());

        Glide.with(requireContext())
            .load(event.getImageUrl() == null || event.getImageUrl().isEmpty()
                ? R.drawable.placeholder_event
                : event.getImageUrl())
            .placeholder(R.drawable.placeholder_event)
            .error(R.drawable.placeholder_event)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(eventImageView);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "-";

        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(rawDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            return "Date: " + dateTime.format(formatter);
        } catch (Exception e) {
            return "Date: " + rawDate;
        }
    }
}
