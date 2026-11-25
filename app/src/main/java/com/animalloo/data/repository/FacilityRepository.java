package com.animalloo.data.repository;

import com.animalloo.data.model.Facility;
import com.animalloo.data.model.FacilityCategory;
import com.animalloo.data.model.PetFriendlyFilter;

import java.util.List;

public interface FacilityRepository {

    void getAllFacilities(RepositoryCallback<List<Facility>> callback);

    void getFacilitiesByCategory(FacilityCategory category, RepositoryCallback<List<Facility>> callback);

    /** 지도 카메라 중심을 기준으로 반경 내 시설을 조회한다. */
    void getFacilitiesNear(FacilityCategory category, double latitude, double longitude,
                           RepositoryCallback<List<Facility>> callback);

    void getPetFriendlyFacilities(PetFriendlyFilter filter, RepositoryCallback<List<Facility>> callback);

    void getFacilityById(String id, RepositoryCallback<Facility> callback);
}
