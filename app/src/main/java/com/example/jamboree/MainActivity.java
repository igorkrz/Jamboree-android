package com.example.jamboree;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.jamboree.data.local.SessionManager;
import com.example.jamboree.data.repository.AuthRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private SessionManager sessionManager;
    private AuthRepository authRepository;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topAppBar = findViewById(R.id.topAppBar);

        try {
            sessionManager = new SessionManager(getApplicationContext());
            authRepository = new AuthRepository(getApplicationContext());
        } catch (Exception e) {
            Toast.makeText(this, "Session init failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            return;
        }

        navController = navHostFragment.getNavController();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            updateTopBarForDestination(destination);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        topAppBar.setOnMenuItemClickListener(this::onTopBarMenuItemClicked);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAuthMenu();
    }

    private void updateTopBarForDestination(@NonNull NavDestination destination) {
        if (topAppBar == null) {
            return;
        }

        boolean showBackButton = destination.getId() == R.id.eventDetailsFragment || destination.getId() == R.id.loginFragment;

        if (showBackButton) {
            topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            topAppBar.setNavigationOnClickListener(v -> navController.popBackStack());
        } else {
            topAppBar.setNavigationIcon(null);
            topAppBar.setNavigationOnClickListener(null);
        }
    }

    private boolean onTopBarMenuItemClicked(@NonNull MenuItem item) {
        if (item.getItemId() != R.id.action_auth) {
            return false;
        }

        if (sessionManager == null) {
            return false;
        }

        if (sessionManager.isLoggedIn()) {
            androidx.appcompat.widget.PopupMenu popupMenu =
                    new androidx.appcompat.widget.PopupMenu(this, findViewById(R.id.topAppBar));

            popupMenu.getMenuInflater().inflate(R.menu.auth_popup_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.action_logout) {
                    performLogout();
                    return true;
                }
                return false;
            });

            popupMenu.show();
        } else {
            navController.navigate(R.id.loginFragment);
        }

        return true;
    }

    private void performLogout() {
        if (authRepository == null) {
            return;
        }

        new Thread(() -> {
            try {
                sessionManager.clearSession();

                runOnUiThread(() -> {
                    updateAuthMenu();
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

                    int currentDestinationId = navController.getCurrentDestination() != null
                        ? navController.getCurrentDestination().getId()
                        : -1;

                    if (currentDestinationId == R.id.userEventsFragment ||
                        currentDestinationId == R.id.calendarFragment) {
                        navController.navigate(R.id.eventsFragment);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                    Toast.makeText(this, "Logout failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void updateAuthMenu() {
        if (topAppBar == null || sessionManager == null) {
            return;
        }

        Menu menu = topAppBar.getMenu();
        MenuItem authItem = menu.findItem(R.id.action_auth);

        if (authItem == null) {
            return;
        }

        if (sessionManager.isLoggedIn()) {
            authItem.setTitle("Logout");
            authItem.setIcon(android.R.drawable.ic_menu_myplaces);
        } else {
            authItem.setTitle("Login");
            authItem.setIcon(android.R.drawable.ic_input_add);
        }
    }
}
