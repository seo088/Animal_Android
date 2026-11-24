package com.animalloo.data.remote;

import com.animalloo.R;
import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.remote.dto.HomeProfileResponse;
import com.animalloo.data.remote.dto.PetRequest;
import com.animalloo.data.model.PetProfileInput;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
        HomeProfile cached = sessionStore.getCachedPetProfile(fallbackName);
        if (sessionStore.getToken() == null) {
            callback.onSuccess(cached != null ? cached : toProfile(null, fallbackName));
            return;
        }
        api.getHomeProfile().enqueue(ApiCallbacks.map(new RepositoryCallback<HomeProfile>() {
            @Override
            public void onSuccess(HomeProfile data) {
                callback.onSuccess(mergeWithCachedPet(data, cached));
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(cached != null ? cached : toProfile(null, fallbackName));
            }
        }, response -> toProfile(response, fallbackName)));
    }

    @Override
    public void upsertPet(String name, String breed, String age,
                          RepositoryCallback<HomeProfile> callback) {
        if (sessionStore.getToken() == null) {
            callback.onError("로그인 후 반려동물을 등록할 수 있습니다.");
            return;
        }
        api.upsertPet(new PetRequest(name, breed, age)).enqueue(ApiCallbacks.map(
                callback, response -> toProfile(response, "게스트")));
    }

    @Override
    public void upsertPet(PetProfileInput pet, File photo, RepositoryCallback<HomeProfile> callback) {
        sessionStore.savePet(pet, photo == null ? null : photo.getAbsolutePath());
        if (sessionStore.getToken() == null) {
            callback.onSuccess(sessionStore.getCachedPetProfile("게스트"));
            return;
        }
        api.upsertPet(new PetRequest(pet.name, pet.breed, pet.age, pet.species, pet.gender, pet.neutered, pet.weightKg, pet.birthDate, pet.adoptionDate)).enqueue(ApiCallbacks.map(new RepositoryCallback<HomeProfile>() {
            @Override public void onSuccess(HomeProfile profile) {
                if (photo == null || !photo.exists()) {
                    callback.onSuccess(sessionStore.getCachedPetProfile("게스트"));
                    return;
                }
                RequestBody body = RequestBody.create(photo, MediaType.get("image/jpeg"));
                api.uploadPetPhoto(MultipartBody.Part.createFormData("photo", photo.getName(), body)).enqueue(ApiCallbacks.map(new RepositoryCallback<HomeProfile>() {
                    @Override public void onSuccess(HomeProfile ignored) {
                        callback.onSuccess(sessionStore.getCachedPetProfile("게스트"));
                    }

                    @Override public void onError(String message) {
                        callback.onSuccess(sessionStore.getCachedPetProfile("게스트"));
                    }
                }, response -> toProfile(response, profile.getUserDisplayName())));
            }
            @Override public void onError(String message) {
                callback.onSuccess(sessionStore.getCachedPetProfile("게스트"));
            }
        }, response -> toProfile(response, "게스트")));
    }

    /**
     * A few deployed backend versions return only part of the pet payload. Keep a locally saved
     * field when that field is absent from the response, but always accept a real server value.
     */
    private static HomeProfile mergeWithCachedPet(HomeProfile remote, HomeProfile cached) {
        if (cached == null || !cached.hasPet()) return remote;
        return new HomeProfile(
                valueOr(remote.getUserDisplayName(), cached.getUserDisplayName()),
                valueOr(remote.getUserPhotoUrl(), cached.getUserPhotoUrl()),
                remote.getUserPhotoFallbackResId(),
                valueOr(remote.getPetName(), cached.getPetName()),
                valueOr(remote.getPetBreed(), cached.getPetBreed()),
                valueOr(remote.getPetAge(), cached.getPetAge()),
                valueOr(remote.getPetSpecies(), cached.getPetSpecies()),
                valueOr(remote.getPetGender(), cached.getPetGender()),
                valueOr(remote.getPetNeutered(), cached.getPetNeutered()),
                remote.getPetWeightKg() != null ? remote.getPetWeightKg() : cached.getPetWeightKg(),
                valueOr(remote.getPetBirthDate(), cached.getPetBirthDate()),
                valueOr(remote.getPetAdoptionDate(), cached.getPetAdoptionDate()),
                valueOr(remote.getPetPhotoUrl(), cached.getPetPhotoUrl()));
    }

    private static String valueOr(String value, String fallback) {
        return value == null || value.isEmpty() || EMPTY_VALUE.equals(value) ? fallback : value;
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
                orDefault(response.getPetAge(), EMPTY_VALUE), response.getPetSpecies(),
                response.getPetGender(), response.getPetNeutered(), response.getPetWeightKg(),
                response.getPetBirthDate(), response.getPetAdoptionDate(), response.getPetPhotoUrl());
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }
}
