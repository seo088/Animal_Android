package com.animalloo.data.mock;

import com.animalloo.data.model.HomeStats;
import com.animalloo.data.repository.HomeRepository;
import com.animalloo.data.repository.RepositoryCallback;

public class MockHomeRepository implements HomeRepository {

    private final MockDataProvider dataProvider;
    private final MockAsyncHelper asyncHelper;

    public MockHomeRepository() {
        dataProvider = MockDataProvider.getInstance();
        asyncHelper = MockAsyncHelper.getInstance();
    }

    @Override
    public void getHomeStats(RepositoryCallback<HomeStats> callback) {
        asyncHelper.execute(dataProvider::getHomeStats, callback);
    }
}
