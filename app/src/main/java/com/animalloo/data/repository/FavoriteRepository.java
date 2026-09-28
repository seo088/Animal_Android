package com.animalloo.data.repository;

import java.util.Set;

/**
 * Persists user favorite items locally (mock).
 */
public interface FavoriteRepository {

    String PREFIX_ALERT = "alert:";

    static String keyForAlert(String alertId) {
        return PREFIX_ALERT + alertId;
    }

    boolean isFavorite(String key);

    void setFavorite(String key, boolean favorite);

    Set<String> getFavoriteKeys();
}
