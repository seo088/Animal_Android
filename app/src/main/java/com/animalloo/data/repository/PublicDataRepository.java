package com.animalloo.data.repository;

import com.animalloo.data.model.PublicDataInfo;

public interface PublicDataRepository {

    void fetchPublicDataStatus(RepositoryCallback<PublicDataInfo> callback);
}
