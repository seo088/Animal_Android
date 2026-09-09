package com.animalloo.data.repository;

import com.animalloo.data.model.HomeStats;

public interface HomeRepository {

    void getHomeStats(RepositoryCallback<HomeStats> callback);
}
