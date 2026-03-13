package com.example.jamboree;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jamboree.data.repository.EventRepository;
import com.example.jamboree.model.Event;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView eventsTextView;
    private final EventRepository eventRepository = new EventRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        eventsTextView = findViewById(R.id.eventsTextView);

        loadEvents();
    }

    private void loadEvents() {
        new Thread(() -> {
            try {
                List<Event> events = eventRepository.getUpcomingEvents(1);

                StringBuilder builder = new StringBuilder();

                for (Event event : events) {
                    builder.append("Name: ").append(event.getName()).append("\n");
                    builder.append("Date: ").append(event.getHoldingDate()).append("\n");

                    if (!event.getVenue().isEmpty()) {
                        builder.append("Venue: ").append(event.getVenue()).append("\n");
                    }

                    if (!event.getCity().isEmpty()) {
                        builder.append("City: ").append(event.getCity()).append("\n");
                    }

                    if (!event.getPrice().isEmpty()) {
                        builder.append("Price: ").append(event.getPrice()).append(" EUR\n");
                    }

                    if (!event.getProviderName().isEmpty()) {
                        builder.append("Provider: ").append(event.getProviderName()).append("\n");
                    }

                    builder.append("\n----------------------\n\n");
                }

                runOnUiThread(() -> eventsTextView.setText(builder.toString()));

            } catch (Exception e) {
                runOnUiThread(() -> eventsTextView.setText("Error: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }
}
