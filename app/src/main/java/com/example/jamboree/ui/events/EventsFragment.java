package com.example.jamboree.ui.events;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jamboree.R;
import com.example.jamboree.data.repository.EventRepository;
import com.example.jamboree.model.Event;

import java.util.List;

public class EventsFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView errorTextView;

    private EventAdapter eventAdapter;
    private final EventRepository eventRepository = new EventRepository();

    public EventsFragment() {
        super(R.layout.fragment_events);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        errorTextView = view.findViewById(R.id.errorTextView);

        eventAdapter = new EventAdapter();
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventsRecyclerView.setAdapter(eventAdapter);

        loadEvents();
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                List<Event> events = eventRepository.getUpcomingEvents(1);

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    eventAdapter.setEvents(events);
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.VISIBLE);
                    errorTextView.setText(String.format("Error: %s", e.getMessage()));
                });
                e.printStackTrace();
            }
        }).start();
    }
}
