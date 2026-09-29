package com.animalloo.data.repository;

import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.HomeStats;

public interface HomeRepository {

    void getHomeStats(RepositoryCallback<HomeStats> callback);

    void getHomeProfile(String userDisplayName, RepositoryCallback<HomeProfile> callback);
}
