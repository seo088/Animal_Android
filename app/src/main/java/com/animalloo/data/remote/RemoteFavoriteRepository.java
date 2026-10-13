package com.animalloo.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.animalloo.data.model.User;
import com.animalloo.data.repository.FavoriteRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * FavoriteRepository is synchronous, so favorites are served from a local per-user
 * cache. Signed-in changes are written through to PUT/DELETE /favorites/{key}, and
 * refresh() replaces the cache with the server copy (on start and after login).
 * Guests keep favorites locally only.
 */
public class RemoteFavoriteRepository implements FavoriteRepository {

    private static final String PREFS_NAME = "animalloo_favorites_remote";

    private final AnimalLooApi api;
    private final SessionStore sessionStore;
    private final SharedPreferences prefs;

    public RemoteFavoriteRepository(AnimalLooApi api, SessionStore sessionStore, Context context) {
        this.api = api;
        this.sessionStore = sessionStore;
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        refresh();
    }

    @Override
    public boolean isFavorite(String key) {
        return getFavoriteKeys().contains(key);
    }

    @Override
    public void setFavorite(String key, boolean favorite) {
        Set<String> keys = new HashSet<>(getFavoriteKeys());
        if (favorite) {
            keys.add(key);
        } else {
            keys.remove(key);
        }
        prefs.edit().putStringSet(storageKey(), keys).apply();

        if (sessionStore.getToken() != null) {
            Call<Void> call = favorite ? api.addFavorite(key) : api.removeFavorite(key);
            call.enqueue(IGNORE_RESULT);
        }
    }

    @Override
    public Set<String> getFavoriteKeys() {
        Set<String> stored = prefs.getStringSet(storageKey(), null);
        return stored == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(stored));
    }

    /** Pulls the signed-in user's favorites from the server into the local cache. */
    public void refresh() {
        if (sessionStore.getToken() == null) {
            return;
        }
        String key = storageKey();
        api.getFavorites().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    prefs.edit().putStringSet(key, new HashSet<>(response.body())).apply();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable throwable) {
                // keep the cached copy while offline
            }
        });
    }

    private String storageKey() {
        User user = sessionStore.getUser();
        return "favorites_" + (user != null ? user.getId() : "guest");
    }

    // Best effort: the local cache is the source of truth for the UI until the next refresh().
    private static final Callback<Void> IGNORE_RESULT = new Callback<Void>() {
        @Override
        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
        }

        @Override
        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
        }
    };
}
