package com.animalloo.data.repository;

import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.model.PetProfileInput;

import java.io.File;

public interface HomeRepository {

    void getHomeStats(RepositoryCallback<HomeStats> callback);

    void getHomeProfile(String userDisplayName, RepositoryCallback<HomeProfile> callback);

    void upsertPet(String name, String breed, String age, RepositoryCallback<HomeProfile> callback);

    void upsertPet(PetProfileInput pet, File photo, RepositoryCallback<HomeProfile> callback);
}
