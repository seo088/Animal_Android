package com.animalloo.ui.detail;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.DetailUiModel;
import com.animalloo.data.model.Facility;
import com.animalloo.data.model.Hospital;
import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.data.model.RescuedAnimal;
import com.animalloo.data.model.UiState;
import com.animalloo.data.repository.AlertRepository;
import com.animalloo.data.repository.AnimalRepository;
import com.animalloo.data.repository.FacilityRepository;
import com.animalloo.data.repository.HospitalRepository;
import com.animalloo.data.repository.LostAnimalRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

public class DetailViewModel extends ViewModel {

    private final Context appContext;
    private final FacilityRepository facilityRepository;
    private final HospitalRepository hospitalRepository;
    private final AnimalRepository animalRepository;
    private final AlertRepository alertRepository;
    private final LostAnimalRepository lostAnimalRepository;

    private final MutableLiveData<UiState<DetailUiModel>> detailState = new MutableLiveData<>();

    public DetailViewModel() {
        RepositoryProvider provider = RepositoryProvider.getInstance();
        appContext = provider.getApplicationContext();
        facilityRepository = provider.getFacilityRepository();
        hospitalRepository = provider.getHospitalRepository();
        animalRepository = provider.getAnimalRepository();
        alertRepository = provider.getAlertRepository();
        lostAnimalRepository = provider.getLostAnimalRepository();
    }

    public LiveData<UiState<DetailUiModel>> getDetailState() {
        return detailState;
    }

    public void loadDetail(DetailType type, String itemId) {
        detailState.setValue(UiState.loading());

        if (type == DetailType.FACILITY) {
            loadFacility(itemId);
        } else if (type == DetailType.HOSPITAL) {
            loadHospital(itemId);
        } else if (type == DetailType.RESCUED_ANIMAL) {
            loadRescuedAnimal(itemId);
        } else if (type == DetailType.ALERT) {
            loadAlert(itemId);
        } else if (type == DetailType.LOST_ANIMAL) {
            loadLostReport(itemId);
        } else {
            detailState.setValue(UiState.error("지원하지 않는 상세 유형입니다."));
        }
    }

    private void loadFacility(String itemId) {
        facilityRepository.getFacilityById(itemId, new RepositoryCallback<Facility>() {
            @Override
            public void onSuccess(Facility data) {
                detailState.setValue(UiState.success(DetailUiMapper.fromFacility(appContext, data)));
            }

            @Override
            public void onError(String message) {
                detailState.setValue(UiState.error(message));
            }
        });
    }

    private void loadHospital(String itemId) {
        hospitalRepository.getHospitalById(itemId, new RepositoryCallback<Hospital>() {
            @Override
            public void onSuccess(Hospital data) {
                detailState.setValue(UiState.success(DetailUiMapper.fromHospital(appContext, data)));
            }

            @Override
            public void onError(String message) {
                detailState.setValue(UiState.error(message));
            }
        });
    }

    private void loadRescuedAnimal(String itemId) {
        animalRepository.getRescuedAnimalById(itemId, new RepositoryCallback<RescuedAnimal>() {
            @Override
            public void onSuccess(RescuedAnimal data) {
                detailState.setValue(UiState.success(DetailUiMapper.fromRescuedAnimal(appContext, data)));
            }

            @Override
            public void onError(String message) {
                detailState.setValue(UiState.error(message));
            }
        });
    }

    private void loadAlert(String itemId) {
        alertRepository.getAlertById(itemId, new RepositoryCallback<AlertNotification>() {
            @Override
            public void onSuccess(AlertNotification data) {
                detailState.setValue(UiState.success(DetailUiMapper.fromAlert(appContext, data)));
            }

            @Override
            public void onError(String message) {
                detailState.setValue(UiState.error(message));
            }
        });
    }

    private void loadLostReport(String itemId) {
        lostAnimalRepository.getReportById(itemId, new RepositoryCallback<LostAnimalReport>() {
            @Override
            public void onSuccess(LostAnimalReport data) {
                detailState.setValue(UiState.success(DetailUiMapper.fromLostReport(appContext, data)));
            }

            @Override
            public void onError(String message) {
                detailState.setValue(UiState.error(message));
            }
        });
    }
}
