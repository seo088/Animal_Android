package com.animalloo.data.repository;

import com.animalloo.data.model.Facility;
import com.animalloo.data.model.FacilityCategory;
import com.animalloo.data.model.PetFriendlyFilter;

import java.util.List;

public interface FacilityRepository {

    void getAllFacilities(RepositoryCallback<List<Facility>> callback);

    void getFacilitiesByCategory(FacilityCategory category, RepositoryCallback<List<Facility>> callback);

    void getPetFriendlyFacilities(PetFriendlyFilter filter, RepositoryCallback<List<Facility>> callback);

    void getFacilityById(String id, RepositoryCallback<Facility> callback);
}
