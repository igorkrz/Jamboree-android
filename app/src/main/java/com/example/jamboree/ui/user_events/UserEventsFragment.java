package com.example.jamboree.ui.user_events;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jamboree.R;
import com.example.jamboree.data.local.SessionManager;
import com.example.jamboree.data.repository.UserEventRepository;
import com.example.jamboree.model.EventPage;
import com.example.jamboree.ui.events.EventAdapter;

public class UserEventsFragment extends Fragment {

    private LinearLayout contentLayout;
    private LinearLayout loginRequiredLayout;
    private Button loginButton;

    private RecyclerView eventsRecyclerView;
    private ProgressBar progressBar;
    private ProgressBar loadMoreProgressBar;
    private TextView errorTextView;

    private EventAdapter eventAdapter;
    private LinearLayoutManager layoutManager;

    private SessionManager sessionManager;
    private UserEventRepository userEventsRepository;

    private boolean isLoading = false;
    private Integer currentPage = 1;
    private Integer nextPage = null;
    private Integer lastPage = null;

    public UserEventsFragment() {
        super(R.layout.fragment_user_events);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contentLayout = view.findViewById(R.id.contentLayout);
        loginRequiredLayout = view.findViewById(R.id.loginRequiredLayout);
        loginButton = view.findViewById(R.id.loginButton);

        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        loadMoreProgressBar = view.findViewById(R.id.loadMoreProgressBar);
        errorTextView = view.findViewById(R.id.errorTextView);

        try {
            sessionManager = new SessionManager(requireContext());
            userEventsRepository = new UserEventRepository(requireContext());
        } catch (Exception e) {
            showErrorState("Failed to initialize secure session: " + e.getMessage());
            return;
        }

        eventAdapter = new EventAdapter();
        layoutManager = new LinearLayoutManager(requireContext());

        eventsRecyclerView.setLayoutManager(layoutManager);
        eventsRecyclerView.setAdapter(eventAdapter);

        loginButton.setOnClickListener(v -> openLogin());

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

                boolean shouldLoadMore =
                    !isLoading &&
                        nextPage != null &&
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2 &&
                        firstVisibleItemPosition >= 0 &&
                        totalItemCount >= 1;

                if (shouldLoadMore) {
                    loadNextPage();
                }
            }
        });

        if (!sessionManager.isLoggedIn()) {
            showLoginRequiredState();
            return;
        }

        showContentState();
        loadFirstPage();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sessionManager == null) {
            return;
        }

        if (sessionManager.isLoggedIn()) {
            showContentState();

            if (eventAdapter != null && eventAdapter.getItemCount() == 0 && !isLoading) {
                loadFirstPage();
            }
        } else {
            showLoginRequiredState();
        }
    }

    private void loadFirstPage() {
        resetPagingState();
        showLoadingState();
        loadUserEventsPage(1, true);
    }

    private void loadNextPage() {
        if (isLoading || nextPage == null) {
            return;
        }

        loadMoreProgressBar.setVisibility(View.VISIBLE);
        loadUserEventsPage(nextPage, false);
    }

    private void loadUserEventsPage(int page, boolean isRefresh) {
        isLoading = true;

        new Thread(() -> {
            try {
                EventPage eventPage = userEventsRepository.getUserEvents(page);

                postToUi(() -> {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    loadMoreProgressBar.setVisibility(View.GONE);
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

                    showEventsState();
                });

            } catch (Exception e) {
                postToUi(() -> {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    loadMoreProgressBar.setVisibility(View.GONE);

                    if (eventAdapter.getItemCount() == 0) {
                        showErrorState("Failed to load user events: " + e.getMessage());
                    }
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void resetPagingState() {
        currentPage = 1;
        nextPage = null;
        lastPage = null;
    }

    private void showLoginRequiredState() {
        loginRequiredLayout.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
    }

    private void showContentState() {
        loginRequiredLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
    }

    private void showLoadingState() {
        loginRequiredLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        loadMoreProgressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);
        eventsRecyclerView.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        loginRequiredLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        loadMoreProgressBar.setVisibility(View.GONE);
        eventsRecyclerView.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
    }

    private void showEventsState() {
        loginRequiredLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        loadMoreProgressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.GONE);
        eventsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void openLogin() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.loginFragment);
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
