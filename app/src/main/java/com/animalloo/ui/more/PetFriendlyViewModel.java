package com.animalloo.ui.more;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.Facility;
import com.animalloo.data.model.PetFriendlyFilter;
import com.animalloo.data.model.UiState;
import com.animalloo.data.repository.FacilityRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

import java.util.List;

public class PetFriendlyViewModel extends ViewModel {

    private final FacilityRepository facilityRepository;

    private final MutableLiveData<UiState<List<Facility>>> facilitiesState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mapViewMode = new MutableLiveData<>(false);

    private String petType = "전체";
    private String sizeLimit = "전체";
    private Boolean indoorAllowed;
    private Boolean carrierRequired;
    private Boolean leashRequired;

    public PetFriendlyViewModel() {
        facilityRepository = RepositoryProvider.getInstance().getFacilityRepository();
    }

    public LiveData<UiState<List<Facility>>> getFacilitiesState() {
        return facilitiesState;
    }

    public LiveData<Boolean> getMapViewMode() {
        return mapViewMode;
    }

    public void setPetTypeFilter(String petType) {
        this.petType = petType;
        loadFacilities();
    }

    public void setSizeLimitFilter(String sizeLimit) {
        this.sizeLimit = sizeLimit;
        loadFacilities();
    }

    public void setIndoorAllowedFilter(Boolean indoorAllowed) {
        this.indoorAllowed = indoorAllowed;
        loadFacilities();
    }

    public void setCarrierRequiredFilter(Boolean carrierRequired) {
        this.carrierRequired = carrierRequired;
        loadFacilities();
    }

    public void setLeashRequiredFilter(Boolean leashRequired) {
        this.leashRequired = leashRequired;
        loadFacilities();
    }

    public void toggleViewMode() {
        Boolean current = mapViewMode.getValue();
        mapViewMode.setValue(current == null || !current);
    }

    public void loadFacilities() {
        facilitiesState.setValue(UiState.loading());
        PetFriendlyFilter filter = new PetFriendlyFilter(
                petType, sizeLimit, indoorAllowed, carrierRequired, leashRequired);

        facilityRepository.getPetFriendlyFacilities(filter, new RepositoryCallback<List<Facility>>() {
            @Override
            public void onSuccess(List<Facility> data) {
                if (data == null || data.isEmpty()) {
                    facilitiesState.setValue(UiState.empty());
                } else {
                    facilitiesState.setValue(UiState.success(data));
                }
            }

            @Override
            public void onError(String message) {
                facilitiesState.setValue(UiState.error(message));
            }
        });
    }

    public void refresh() {
        loadFacilities();
    }
}
