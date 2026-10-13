package com.animalloo.data.remote;

import android.content.Context;

import com.animalloo.data.model.Hospital;
import com.animalloo.data.repository.HospitalRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.DeviceLocationProvider;

import java.util.List;

/** GET /hospitals, /hospitals/nearby, /hospitals/{id} using the device location. */
public class RemoteHospitalRepository implements HospitalRepository {

    private static final double LIST_RADIUS_KM = 10.0;
    private static final int LIST_LIMIT = 100;
    // The diagnosis screen keeps only hospitals open now, so fetch a generous pool.
    private static final double NEARBY_RADIUS_KM = 10.0;
    private static final int NEARBY_LIMIT = 50;

    private final AnimalLooApi api;
    private final Context appContext;

    public RemoteHospitalRepository(AnimalLooApi api, Context context) {
        this.api = api;
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void getAllHospitals(RepositoryCallback<List<Hospital>> callback) {
        DeviceLocationProvider.getLocation(appContext, (lat, lng) ->
                api.getHospitals(lat, lng, LIST_RADIUS_KM, LIST_LIMIT).enqueue(ApiCallbacks.of(callback)));
    }

    @Override
    public void getNearbyHospitals(RepositoryCallback<List<Hospital>> callback) {
        DeviceLocationProvider.getLocation(appContext, (lat, lng) ->
                api.getNearbyHospitals(lat, lng, NEARBY_RADIUS_KM, NEARBY_LIMIT).enqueue(ApiCallbacks.of(callback)));
    }

    @Override
    public void getHospitalById(String id, RepositoryCallback<Hospital> callback) {
        api.getHospital(id).enqueue(ApiCallbacks.of(callback));
    }
}
