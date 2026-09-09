package com.animalloo.data.repository;

import com.animalloo.data.model.Hospital;

import java.util.List;

public interface HospitalRepository {

    void getAllHospitals(RepositoryCallback<List<Hospital>> callback);

    void getNearbyHospitals(RepositoryCallback<List<Hospital>> callback);

    void getHospitalById(String id, RepositoryCallback<Hospital> callback);
}
