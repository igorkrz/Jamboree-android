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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.jamboree.R;
import com.example.jamboree.data.controller.FavoriteEventsController;
import com.example.jamboree.data.repository.EventRepository;
import com.example.jamboree.model.Event;
import com.example.jamboree.model.EventPage;

public class EventsFragment extends Fragment {
    private RecyclerView eventsRecyclerView;
    private ProgressBar progressBar;
    private ProgressBar loadMoreProgressBar;
    private TextView errorTextView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private EventAdapter eventAdapter;
    private LinearLayoutManager layoutManager;
    private final EventRepository eventRepository = new EventRepository();
    private FavoriteEventsController favoriteEventsController;

    private boolean isLoading = false;
    private Integer currentPage = 1;
    private Integer nextPage = null;
    private Integer lastPage = null;

    public EventsFragment() {
        super(R.layout.fragment_events);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            favoriteEventsController = new FavoriteEventsController(requireContext());
        } catch (Exception e) {
            showError("Failed to initialize favorites: " + e.getMessage());
            return;
        }

        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        loadMoreProgressBar = view.findViewById(R.id.loadMoreProgressBar);
        errorTextView = view.findViewById(R.id.errorTextView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        eventAdapter = new EventAdapter(
            event -> {
                androidx.navigation.NavController navController =
                        androidx.navigation.fragment.NavHostFragment.findNavController(this);

                navController.navigate(
                        R.id.eventDetailsFragment,
                        EventDetailsFragment.createArgs(event.getId())
                );
            },
            this::toggleFavorite
        );

        layoutManager = new LinearLayoutManager(requireContext());

        eventsRecyclerView.setLayoutManager(layoutManager);
        eventsRecyclerView.setAdapter(eventAdapter);

        swipeRefreshLayout.setOnRefreshListener(this::refreshEvents);

        eventsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy <= 0) {
                    return;
                }

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                boolean reachedThreshold = (visibleItemCount + firstVisibleItemPosition) >= (totalItemCount - 4);

                if (!isLoading && nextPage != null && reachedThreshold) {
                    loadNextPage();
                }
            }
        });

        loadFirstPage();
    }

    private void loadFirstPage() {
        resetPagingState();
        showInitialLoading();

        loadEventsPage(1, true);
    }

    private void refreshEvents() {
        if (isLoading) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        resetPagingState();
        errorTextView.setVisibility(View.GONE);
        loadEventsPage(1, true);
    }

    private void loadNextPage() {
        if (isLoading || nextPage == null) {
            return;
        }

        loadMoreProgressBar.setVisibility(View.VISIBLE);
        loadEventsPage(nextPage, false);
    }

    private void resetPagingState() {
        currentPage = 1;
        nextPage = null;
        lastPage = null;
    }

    private void loadEventsPage(int page, boolean isRefresh) {
        isLoading = true;

        new Thread(() -> {
            try {
                EventPage eventPage = eventRepository.getUpcomingEvents(page);

                postToUi(() -> {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    loadMoreProgressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    errorTextView.setVisibility(View.GONE);
                    eventsRecyclerView.setVisibility(View.VISIBLE);

                    currentPage = page;
                    nextPage = eventPage.getNextPage();
                    lastPage = eventPage.getLastPage();

                    if (isRefresh) {
                        eventAdapter.replaceEvents(eventPage.getEvents());
                        eventsRecyclerView.scrollToPosition(0);
                    } else {
                        eventAdapter.appendEvents(eventPage.getEvents());
                    }

                    loadFavoriteStateIfLoggedIn();
                });
            } catch (Exception e) {
                postToUi(() -> {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    loadMoreProgressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (eventAdapter.getItemCount() == 0) {
                        showError("Error: " + e.getMessage());
                    }

                });
                e.printStackTrace();
            }
        }).start();
    }

    private void loadFavoriteStateIfLoggedIn() {
        favoriteEventsController.loadFavoritesAsync(new FavoriteEventsController.FavoriteStateListener() {
            @Override
            public void onFavoritesLoaded(java.util.Map<String, String> favoriteEventMap) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> eventAdapter.setFavoriteEventMap(favoriteEventMap));
            }

            @Override
            public void onFavoriteAdded(String eventId, String userEventId) {
            }

            @Override
            public void onFavoriteRemoved(String eventId) {
            }

            @Override
            public void onError(String message) {
            }

            @Override
            public void onAuthenticationRequired() {
            }
        });
    }

    private void toggleFavorite(Event event) {
        favoriteEventsController.toggleFavoriteAsync(
            event,
            true,
            new FavoriteEventsController.FavoriteStateListener() {
                @Override
                public void onFavoritesLoaded(java.util.Map<String, String> favoriteEventMap) {
                }

                @Override
                public void onFavoriteAdded(String eventId, String userEventId) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() ->
                        eventAdapter.setFavoriteEventMap(favoriteEventsController.getFavoriteEventMap())
                    );
                }

                @Override
                public void onFavoriteRemoved(String eventId) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() ->
                        eventAdapter.setFavoriteEventMap(favoriteEventsController.getFavoriteEventMap())
                    );
                }

                @Override
                public void onError(String message) {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> showError(message));
                }

                @Override
                public void onAuthenticationRequired() {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() -> {
                        androidx.navigation.NavController navController =
                                androidx.navigation.fragment.NavHostFragment.findNavController(EventsFragment.this);
                        navController.navigate(R.id.loginFragment);
                    });
                }
            }
        );
    }

    private void showInitialLoading() {
        progressBar.setVisibility(View.VISIBLE);
        loadMoreProgressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);
        eventsRecyclerView.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        loadMoreProgressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        eventsRecyclerView.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
    }

    private void postToUi(Runnable action) {
        if (!isAdded()) {
            return;
        }

        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) {
                return;
            }
            action.run();
        });
    }
}
