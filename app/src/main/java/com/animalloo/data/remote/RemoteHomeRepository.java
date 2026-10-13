package com.animalloo.data.remote;

import com.animalloo.R;
import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.remote.dto.HomeProfileResponse;
import com.animalloo.data.repository.HomeRepository;
import com.animalloo.data.repository.RepositoryCallback;

/**
 * GET /home/stats, /home/profile. The profile needs a session; without one (or on
 * failure) a guest profile is returned so the header still renders.
 */
public class RemoteHomeRepository implements HomeRepository {

    private static final String EMPTY_VALUE = "-";

    private final AnimalLooApi api;
    private final SessionStore sessionStore;

    public RemoteHomeRepository(AnimalLooApi api, SessionStore sessionStore) {
        this.api = api;
        this.sessionStore = sessionStore;
    }

    @Override
    public void getHomeStats(RepositoryCallback<HomeStats> callback) {
        api.getHomeStats().enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void getHomeProfile(String userDisplayName, RepositoryCallback<HomeProfile> callback) {
        String fallbackName = userDisplayName == null || userDisplayName.isEmpty() ? "게스트" : userDisplayName;
        if (sessionStore.getToken() == null) {
            callback.onSuccess(toProfile(null, fallbackName));
            return;
        }
        api.getHomeProfile().enqueue(ApiCallbacks.map(new RepositoryCallback<HomeProfile>() {
            @Override
            public void onSuccess(HomeProfile data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(toProfile(null, fallbackName));
            }
        }, response -> toProfile(response, fallbackName)));
    }

    /** Pet fields stay null when no pet is registered; see HomeProfile.hasPet(). */
    private static HomeProfile toProfile(HomeProfileResponse response, String fallbackName) {
        if (response == null) {
            return new HomeProfile(fallbackName, null, R.drawable.ic_profile_placeholder, null, null, null);
        }
        return new HomeProfile(
                orDefault(response.getUserDisplayName(), fallbackName),
                response.getUserPhotoUrl(),
                R.drawable.ic_profile_placeholder,
                response.getPetName(),
                orDefault(response.getPetBreed(), EMPTY_VALUE),
                orDefault(response.getPetAge(), EMPTY_VALUE));
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }
}
