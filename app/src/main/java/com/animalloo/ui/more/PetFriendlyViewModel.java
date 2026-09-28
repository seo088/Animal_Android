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
    private int requestGeneration;

    public PetFriendlyViewModel() {
        facilityRepository = RepositoryProvider.getInstance().getFacilityRepository();
    }

    public LiveData<UiState<List<Facility>>> getFacilitiesState() {
        return facilitiesState;
    }

    public LiveData<Boolean> getMapViewMode() {
        return mapViewMode;
    }

    public PetFriendlyFilter getFilter() {
        return new PetFriendlyFilter(petType, sizeLimit, indoorAllowed, carrierRequired, leashRequired);
    }

    public void restoreFilters(PetFriendlyFilter filter, boolean mapMode) {
        petType = filter.getPetType();
        sizeLimit = filter.getSizeLimit();
        indoorAllowed = filter.getIndoorAllowed();
        carrierRequired = filter.getCarrierRequired();
        leashRequired = filter.getLeashRequired();
        mapViewMode.setValue(mapMode);
    }

    public void resetFilters() {
        restoreFilters(PetFriendlyFilter.empty(), Boolean.TRUE.equals(mapViewMode.getValue()));
        loadFacilities();
    }

    public void setPetTypeFilter(String petType) {
        if (this.petType.equals(petType)) return;
        this.petType = petType;
        loadFacilities();
    }

    public void setSizeLimitFilter(String sizeLimit) {
        if (this.sizeLimit.equals(sizeLimit)) return;
        this.sizeLimit = sizeLimit;
        loadFacilities();
    }

    public void setIndoorAllowedFilter(Boolean indoorAllowed) {
        if (java.util.Objects.equals(this.indoorAllowed, indoorAllowed)) return;
        this.indoorAllowed = indoorAllowed;
        loadFacilities();
    }

    public void setCarrierRequiredFilter(Boolean carrierRequired) {
        if (java.util.Objects.equals(this.carrierRequired, carrierRequired)) return;
        this.carrierRequired = carrierRequired;
        loadFacilities();
    }

    public void setLeashRequiredFilter(Boolean leashRequired) {
        if (java.util.Objects.equals(this.leashRequired, leashRequired)) return;
        this.leashRequired = leashRequired;
        loadFacilities();
    }

    public void toggleViewMode() {
        Boolean current = mapViewMode.getValue();
        mapViewMode.setValue(current == null || !current);
    }

    public void setMapViewMode(boolean mapMode) {
        mapViewMode.setValue(mapMode);
    }

    public void loadFacilities() {
        facilitiesState.setValue(UiState.loading());
        PetFriendlyFilter filter = getFilter();
        int generation = ++requestGeneration;

        facilityRepository.getPetFriendlyFacilities(filter, new RepositoryCallback<List<Facility>>() {
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

    public void refresh() {
        loadFacilities();
    }
}
