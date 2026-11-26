package com.animalloo.data.remote;

import android.content.Context;

import com.animalloo.data.model.Hospital;
import com.animalloo.data.repository.HospitalRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.util.List;

/** GET /hospitals, /hospitals/nearby, /hospitals/{id} using the device location. */
public class RemoteHospitalRepository implements HospitalRepository {

    private static final double LIST_RADIUS_KM = 10.0;
    private static final int LIST_LIMIT = 100;
    // The diagnosis screen keeps only hospitals open now, so fetch a generous pool.
    private static final double NEARBY_RADIUS_KM = 10.0;
    private static final int NEARBY_LIMIT = 50;
    // GPS 검증 전까지 증상 안내의 병원 추천은 국립군산대학교 디지털정보관을
    // 기준으로 고정한다. 지도·시설 화면의 현재 위치 동작에는 영향을 주지 않는다.
    private static final double DIAGNOSIS_HOSPITAL_LATITUDE = 35.9470943;
    private static final double DIAGNOSIS_HOSPITAL_LONGITUDE = 126.6814341;

    private final AnimalLooApi api;
    private final Context appContext;

    public RemoteHospitalRepository(AnimalLooApi api, Context context) {
        this.api = api;
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void getAllHospitals(RepositoryCallback<List<Hospital>> callback) {
        api.getHospitals(DIAGNOSIS_HOSPITAL_LATITUDE, DIAGNOSIS_HOSPITAL_LONGITUDE,
                LIST_RADIUS_KM, LIST_LIMIT).enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void getNearbyHospitals(RepositoryCallback<List<Hospital>> callback) {
        api.getNearbyHospitals(DIAGNOSIS_HOSPITAL_LATITUDE, DIAGNOSIS_HOSPITAL_LONGITUDE,
                NEARBY_RADIUS_KM, NEARBY_LIMIT).enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void getHospitalById(String id, RepositoryCallback<Hospital> callback) {
        api.getHospital(id).enqueue(ApiCallbacks.of(callback));
    }
}
