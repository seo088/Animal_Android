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

    public void refresh() {
        loadFacilities(selectedCategory.getValue());
    }
}
