package com.animalloo.data.repository;

import java.util.Set;

/**
 * Persists user favorite items locally (mock).
 */
public interface FavoriteRepository {

    String PREFIX_ALERT = "alert:";
    String PREFIX_RESCUED_ANIMAL = "rescued:";

    static String keyForAlert(String alertId) {
        return PREFIX_ALERT + alertId;
    }

    static String keyForRescuedAnimal(String animalId) {
        return PREFIX_RESCUED_ANIMAL + animalId;
    }

    boolean isFavorite(String key);

    void setFavorite(String key, boolean favorite);

    Set<String> getFavoriteKeys();
}
