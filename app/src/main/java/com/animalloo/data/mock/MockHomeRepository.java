package com.animalloo.data.mock;

import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.repository.HomeRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.data.model.PetProfileInput;
import java.io.File;

public class MockHomeRepository implements HomeRepository {

    private final MockDataProvider dataProvider;
    private final MockAsyncHelper asyncHelper;
    private HomeProfile profile;

    public MockHomeRepository() {
        dataProvider = MockDataProvider.getInstance();
        asyncHelper = MockAsyncHelper.getInstance();
    }

    @Override
    public void getHomeStats(RepositoryCallback<HomeStats> callback) {
        asyncHelper.execute(dataProvider::getHomeStats, callback);
    }

    @Override
    public void getHomeProfile(String userDisplayName, RepositoryCallback<HomeProfile> callback) {
        asyncHelper.execute(() -> {
            if (profile == null) {
                profile = dataProvider.getHomeProfile(userDisplayName);
            }
            return profile;
        }, callback);
    }

    @Override
    public void upsertPet(String name, String breed, String age,
                          RepositoryCallback<HomeProfile> callback) {
        asyncHelper.execute(() -> {
            HomeProfile current = profile == null
                    ? dataProvider.getHomeProfile(null)
                    : profile;
            profile = new HomeProfile(
                    current.getUserDisplayName(),
                    current.getUserPhotoUrl(),
                    current.getUserPhotoFallbackResId(),
                    name,
                    breed,
                    age);
            return profile;
        }, callback);
    }

    @Override public void upsertPet(PetProfileInput pet, File photo, RepositoryCallback<HomeProfile> callback) {
        upsertPet(pet.name, pet.breed, pet.age, callback);
    }
}
