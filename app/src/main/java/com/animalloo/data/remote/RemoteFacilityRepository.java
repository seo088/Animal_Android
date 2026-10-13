package com.animalloo.data.remote;

import android.content.Context;

import com.animalloo.data.model.Facility;
import com.animalloo.data.model.FacilityCategory;
import com.animalloo.data.model.PetFriendlyFilter;
import com.animalloo.data.repository.FacilityRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.DeviceLocationProvider;

import java.util.List;

/**
 * GET /facilities, /facilities/pet-friendly, /facilities/{id}. Lists are sent the
 * device location so the server returns the nearest results with distanceKm.
 */
public class RemoteFacilityRepository implements FacilityRepository {

    private static final double MAP_RADIUS_KM = 10.0;
    private static final int MAP_LIMIT = 300;
    // 동반시설 are spread nationwide (관광지, 휴게소), so search wide and sort by distance.
    private static final double PET_FRIENDLY_RADIUS_KM = 300.0;
    private static final int PET_FRIENDLY_LIMIT = 200;

    private final AnimalLooApi api;
    private final Context appContext;

    public RemoteFacilityRepository(AnimalLooApi api, Context context) {
        this.api = api;
        this.appContext = context.getApplicationContext();
    }

    @Override
    public void getAllFacilities(RepositoryCallback<List<Facility>> callback) {
        getFacilities(null, callback);
    }

    @Override
    public void getFacilitiesByCategory(FacilityCategory category, RepositoryCallback<List<Facility>> callback) {
        getFacilities(category.name(), callback);
    }

    private void getFacilities(String category, RepositoryCallback<List<Facility>> callback) {
        DeviceLocationProvider.getLocation(appContext, (lat, lng) ->
                api.getFacilities(category, lat, lng, MAP_RADIUS_KM, MAP_LIMIT)
                        .enqueue(ApiCallbacks.of(callback)));
    }

    @Override
    public void getPetFriendlyFacilities(PetFriendlyFilter filter, RepositoryCallback<List<Facility>> callback) {
        DeviceLocationProvider.getLocation(appContext, (lat, lng) ->
                api.getPetFriendlyFacilities(
                                filter.getPetType(),
                                filter.getSizeLimit(),
                                filter.getIndoorAllowed(),
                                filter.getCarrierRequired(),
                                filter.getLeashRequired(),
                                lat, lng, PET_FRIENDLY_RADIUS_KM, PET_FRIENDLY_LIMIT)
                        .enqueue(ApiCallbacks.of(callback)));
    }

    @Override
    public void getFacilityById(String id, RepositoryCallback<Facility> callback) {
        api.getFacility(id).enqueue(ApiCallbacks.of(callback));
    }
}
