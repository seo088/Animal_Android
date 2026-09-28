package com.animalloo.ui.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.Facility;
import com.animalloo.data.model.UiState;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

public class FacilityBottomSheetViewModel extends ViewModel {

    private final MutableLiveData<UiState<Facility>> facilityState = new MutableLiveData<>();

    public LiveData<UiState<Facility>> getFacilityState() {
        return facilityState;
    }

    public void loadFacility(String id) {
        facilityState.setValue(UiState.loading());
        RepositoryProvider.getInstance().getFacilityRepository().getFacilityById(id,
                new RepositoryCallback<Facility>() {
                    @Override
                    public void onSuccess(Facility facility) {
                        facilityState.setValue(facility == null
                                ? UiState.empty() : UiState.success(facility));
                    }

                    @Override
                    public void onError(String message) {
                        facilityState.setValue(UiState.error(message));
                    }
                });
    }
}
