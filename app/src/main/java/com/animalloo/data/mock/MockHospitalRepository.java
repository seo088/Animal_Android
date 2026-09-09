package com.animalloo.data.mock;

import com.animalloo.data.model.Hospital;
import com.animalloo.data.repository.HospitalRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MockHospitalRepository implements HospitalRepository {

    private final MockDataProvider dataProvider;
    private final MockAsyncHelper asyncHelper;

    public MockHospitalRepository() {
        dataProvider = MockDataProvider.getInstance();
        asyncHelper = MockAsyncHelper.getInstance();
    }

    @Override
    public void getAllHospitals(RepositoryCallback<List<Hospital>> callback) {
        asyncHelper.execute(() -> new ArrayList<>(dataProvider.getHospitals()), callback);
    }

    @Override
    public void getNearbyHospitals(RepositoryCallback<List<Hospital>> callback) {
        asyncHelper.execute(() -> {
            List<Hospital> sorted = new ArrayList<>(dataProvider.getHospitals());
            Collections.sort(sorted, Comparator.comparingDouble(Hospital::getDistanceKm));
            return sorted;
        }, callback);
    }

    @Override
    public void getHospitalById(String id, RepositoryCallback<Hospital> callback) {
        asyncHelper.execute(() -> {
            Hospital hospital = dataProvider.findHospitalById(id);
            if (hospital == null) {
                throw new IllegalStateException("병원 정보를 찾을 수 없습니다.");
            }
            return hospital;
        }, callback);
    }
}
