package com.animalloo.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.Facility;
import com.animalloo.data.model.FacilityCategory;
import com.animalloo.data.model.UiState;
import com.animalloo.data.repository.FacilityRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

import java.util.List;

public class MapViewModel extends ViewModel {

    private final FacilityRepository facilityRepository;

    private final MutableLiveData<UiState<List<Facility>>> facilitiesState = new MutableLiveData<>();
    private final MutableLiveData<FacilityCategory> selectedCategory = new MutableLiveData<>();
    private int requestGeneration;
    private double lastLatitude;
    private double lastLongitude;
    private boolean hasQueryCenter;

    public MapViewModel() {
        facilityRepository = RepositoryProvider.getInstance().getFacilityRepository();
    }

    public LiveData<UiState<List<Facility>>> getFacilitiesState() {
        return facilitiesState;
    }

    public LiveData<FacilityCategory> getSelectedCategory() {
        return selectedCategory;
    }

    public void loadFacilities(FacilityCategory category) {
        selectedCategory.setValue(category);
        facilitiesState.setValue(UiState.loading());
        int generation = ++requestGeneration;

        if (category == null) {
            facilityRepository.getAllFacilities(new RepositoryCallback<List<Facility>>() {
                @Override
                public void onSuccess(List<Facility> data) {
                    if (generation != requestGeneration) return;
                    if (data == null || data.isEmpty()) {
                        facilitiesState.setValue(UiState.empty());
                    } else {
                        facilitiesState.setValue(UiState.success(data));
                    }
                }

                @Override
                public void onError(String message) {
                    if (generation != requestGeneration) return;
                    facilitiesState.setValue(UiState.error(message));
                }
            });
        } else {
            facilityRepository.getFacilitiesByCategory(category, new RepositoryCallback<List<Facility>>() {
                @Override
                public void onSuccess(List<Facility> data) {
                    if (generation != requestGeneration) return;
                    if (data == null || data.isEmpty()) {
                        facilitiesState.setValue(UiState.empty());
                    } else {
                        facilitiesState.setValue(UiState.success(data));
                    }
                }

                @Override
                public void onError(String message) {
                    if (generation != requestGeneration) return;
                    facilitiesState.setValue(UiState.error(message));
                }
            });
        }
    }

    public void loadFacilitiesAt(FacilityCategory category, double latitude, double longitude,
                                 boolean force) {
        if (!force && hasQueryCenter && distanceKm(lastLatitude, lastLongitude, latitude, longitude) < 1.0) {
            return;
        }
        selectedCategory.setValue(category);
        lastLatitude = latitude;
        lastLongitude = longitude;
        hasQueryCenter = true;
        facilitiesState.setValue(UiState.loading());
        int generation = ++requestGeneration;
        facilityRepository.getFacilitiesNear(category, latitude, longitude,
                new RepositoryCallback<List<Facility>>() {
                    @Override
                    public void onSuccess(List<Facility> data) {
                        if (generation != requestGeneration) return;
                        facilitiesState.setValue(data == null || data.isEmpty()
                                ? UiState.empty() : UiState.success(data));
                    }

                    @Override
                    public void onError(String message) {
                        if (generation != requestGeneration) return;
                        facilitiesState.setValue(UiState.error(message));
                    }
                });
    }

    public void refresh() {
        if (hasQueryCenter) {
            loadFacilitiesAt(selectedCategory.getValue(), lastLatitude, lastLongitude, true);
        } else {
            loadFacilities(selectedCategory.getValue());
        }
    }

    private static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double latitudeRadians = Math.toRadians(lat2 - lat1);
        double longitudeRadians = Math.toRadians(lng2 - lng1);
        double value = Math.sin(latitudeRadians / 2) * Math.sin(latitudeRadians / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(longitudeRadians / 2) * Math.sin(longitudeRadians / 2);
        return 6371.0 * 2.0 * Math.atan2(Math.sqrt(value), Math.sqrt(1.0 - value));
    }
}
