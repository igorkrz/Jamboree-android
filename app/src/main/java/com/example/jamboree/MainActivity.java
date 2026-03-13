package com.example.jamboree;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jamboree.data.repository.EventRepository;
import com.example.jamboree.model.Event;
import com.example.jamboree.ui.events.EventAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView errorTextView;

    private EventAdapter eventAdapter;
    private final EventRepository eventRepository = new EventRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);

        eventAdapter = new EventAdapter();
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(eventAdapter);

        loadEvents();
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                List<Event> events = eventRepository.getUpcomingEvents(1);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    eventAdapter.setEvents(events);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.VISIBLE);
                    errorTextView.setText(String.format("Error: %s", e.getMessage()));
                });
                e.printStackTrace();
            }
        }).start();
    }
}
