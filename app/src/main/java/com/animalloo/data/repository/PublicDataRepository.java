package com.animalloo.data.repository;

import com.animalloo.data.model.PublicDataInfo;

public interface PublicDataRepository {

    void fetchDemoPublicData(RepositoryCallback<PublicDataInfo> callback);
}
