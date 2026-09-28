package com.animalloo.data.mock;

import android.content.Context;
import android.content.SharedPreferences;

import com.animalloo.data.model.User;
import com.animalloo.data.repository.AuthRepository;
import com.animalloo.data.repository.FavoriteRepository;
import com.animalloo.util.RepositoryProvider;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MockFavoriteRepository implements FavoriteRepository {

    private static final String PREFS_NAME = "animalloo_favorites";
    private static final String KEY_FAVORITES_PREFIX = "favorite_keys_";

    private final Context appContext;

    public MockFavoriteRepository(Context context) {
        appContext = context.getApplicationContext();
    }

    @Override
    public boolean isFavorite(String key) {
        return getFavoriteKeys().contains(key);
    }

    @Override
    public void setFavorite(String key, boolean favorite) {
        Set<String> favorites = new HashSet<>(getFavoriteKeys());
        if (favorite) {
            favorites.add(key);
        } else {
            favorites.remove(key);
        }
        getPreferences().edit()
                .putString(getStorageKey(), joinKeys(favorites))
                .apply();
    }

    @Override
    public Set<String> getFavoriteKeys() {
        String stored = getPreferences().getString(getStorageKey(), "");
        if (stored == null || stored.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> keys = new HashSet<>();
        for (String key : stored.split(",")) {
            if (!key.isEmpty()) {
                keys.add(key);
            }
        }
        return keys;
    }

    private SharedPreferences getPreferences() {
        return appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private String getStorageKey() {
        AuthRepository authRepository = RepositoryProvider.getInstance().getAuthRepository();
        User user = authRepository.getCurrentUser();
        String userId = user != null ? user.getId() : "guest";
        return KEY_FAVORITES_PREFIX + userId;
    }

    private String joinKeys(Set<String> keys) {
        if (keys.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String key : keys) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(key);
        }
        return builder.toString();
    }
}
