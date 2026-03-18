package com.example.jamboree.data.controller;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.jamboree.data.local.SessionManager;
import com.example.jamboree.data.repository.UserEventRepository;
import com.example.jamboree.model.Event;

import java.util.HashMap;
import java.util.Map;

public class FavoriteEventsController {
    public interface FavoriteStateListener {
        void onFavoritesLoaded(Map<String, String> favoriteEventMap);
        void onFavoriteAdded(String eventId, String userEventId);
        void onFavoriteRemoved(String eventId);
        void onError(String message);
        void onAuthenticationRequired();
    }

    private final SessionManager sessionManager;
    private final UserEventRepository userEventsRepository;
    private final Map<String, String> favoriteEventMap = new HashMap<>();

    public FavoriteEventsController(Context context) throws Exception {
        Context appContext = context.getApplicationContext();
        this.sessionManager = new SessionManager(appContext);
        this.userEventsRepository = new UserEventRepository(appContext);
    }

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public Map<String, String> getFavoriteEventMap() {
        return new HashMap<>(favoriteEventMap);
    }

    public void loadFavoritesAsync(@NonNull FavoriteStateListener listener) {
        if (!isLoggedIn()) {
            favoriteEventMap.clear();
            listener.onFavoritesLoaded(new HashMap<>(favoriteEventMap));
            return;
        }

        new Thread(() -> {
            try {
                Map<String, String> refs = userEventsRepository.getUserEventRefs();
                synchronized (favoriteEventMap) {
                    favoriteEventMap.clear();
                    favoriteEventMap.putAll(refs);
                }
                listener.onFavoritesLoaded(new HashMap<>(favoriteEventMap));
            } catch (Exception e) {
                listener.onError("Failed to load favorites: " + e.getMessage());
            }
        }).start();
    }

    public void toggleFavoriteAsync(@NonNull Event event, boolean requireLoginRedirect, @NonNull FavoriteStateListener listener) {
        if (!isLoggedIn()) {
            if (requireLoginRedirect) {
                listener.onAuthenticationRequired();
            } else {
                listener.onError("User is not authenticated.");
            }
            return;
        }

        new Thread(() -> {
            try {
                String existingUserEventId;
                synchronized (favoriteEventMap) {
                    existingUserEventId = favoriteEventMap.get(event.getId());
                }

                if (existingUserEventId == null) {
                    String newUserEventId = userEventsRepository.addUserEvent(event.getId());

                    synchronized (favoriteEventMap) {
                        favoriteEventMap.put(event.getId(), newUserEventId);
                    }

                    listener.onFavoriteAdded(event.getId(), newUserEventId);
                } else {
                    userEventsRepository.removeUserEvent(existingUserEventId);

                    synchronized (favoriteEventMap) {
                        favoriteEventMap.remove(event.getId());
                    }

                    listener.onFavoriteRemoved(event.getId());
                }
            } catch (Exception e) {
                listener.onError("Favorite update failed: " + e.getMessage());
            }
        }).start();
    }
}
