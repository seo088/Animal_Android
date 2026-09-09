package com.animalloo.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.model.UiState;
import com.animalloo.data.repository.AlertRepository;
import com.animalloo.data.repository.HomeRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final HomeRepository homeRepository;
    private final AlertRepository alertRepository;

    private final MutableLiveData<UiState<HomeStats>> statsState = new MutableLiveData<>();
    private final MutableLiveData<UiState<List<AlertNotification>>> alertsState = new MutableLiveData<>();

    public HomeViewModel() {
        RepositoryProvider provider = RepositoryProvider.getInstance();
        homeRepository = provider.getHomeRepository();
        alertRepository = provider.getAlertRepository();
    }

    public LiveData<UiState<HomeStats>> getStatsState() {
        return statsState;
    }

    public LiveData<UiState<List<AlertNotification>>> getAlertsState() {
        return alertsState;
    }

    public void loadHomeData() {
        loadStats();
        loadAlerts();
    }

    private void loadStats() {
        statsState.setValue(UiState.loading());
        homeRepository.getHomeStats(new RepositoryCallback<HomeStats>() {
            @Override
            public void onSuccess(HomeStats data) {
                statsState.setValue(UiState.success(data));
            }

            @Override
            public void onError(String message) {
                statsState.setValue(UiState.error(message));
            }
        });
    }

    private void loadAlerts() {
        alertsState.setValue(UiState.loading());
        alertRepository.getRecentAlerts(new RepositoryCallback<List<AlertNotification>>() {
            @Override
            public void onSuccess(List<AlertNotification> data) {
                if (data == null || data.isEmpty()) {
                    alertsState.setValue(UiState.empty());
                } else {
                    alertsState.setValue(UiState.success(data));
                }
            }

            @Override
            public void onError(String message) {
                alertsState.setValue(UiState.error(message));
            }
        });
    }

    public void refresh() {
        loadHomeData();
    }
}
