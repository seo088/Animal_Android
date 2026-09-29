package com.animalloo.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.model.UiState;
import com.animalloo.data.model.User;
import com.animalloo.data.repository.AlertRepository;
import com.animalloo.data.repository.AuthRepository;
import com.animalloo.data.repository.FavoriteRepository;
import com.animalloo.data.repository.HomeRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeViewModel extends ViewModel {

    private final HomeRepository homeRepository;
    private final AlertRepository alertRepository;
    private final FavoriteRepository favoriteRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<UiState<HomeProfile>> profileState = new MutableLiveData<>();
    private final MutableLiveData<UiState<HomeStats>> statsState = new MutableLiveData<>();
    private final MutableLiveData<UiState<List<AlertNotification>>> alertsState = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> favoriteAlertIds = new MutableLiveData<>(new HashSet<>());

    public HomeViewModel() {
        RepositoryProvider provider = RepositoryProvider.getInstance();
        homeRepository = provider.getHomeRepository();
        alertRepository = provider.getAlertRepository();
        favoriteRepository = provider.getFavoriteRepository();
        authRepository = provider.getAuthRepository();
    }

    public LiveData<UiState<HomeProfile>> getProfileState() {
        return profileState;
    }

    public LiveData<UiState<HomeStats>> getStatsState() {
        return statsState;
    }

    public LiveData<UiState<List<AlertNotification>>> getAlertsState() {
        return alertsState;
    }

    public LiveData<Set<String>> getFavoriteAlertIds() {
        return favoriteAlertIds;
    }

    public void loadHomeData() {
        loadProfile();
        loadStats();
        loadAlerts();
    }

    private void loadProfile() {
        profileState.setValue(UiState.loading());
        User user = authRepository.getCurrentUser();
        String displayName = user != null ? user.getDisplayName() : null;
        homeRepository.getHomeProfile(displayName, new RepositoryCallback<HomeProfile>() {
            @Override
            public void onSuccess(HomeProfile data) {
                profileState.setValue(UiState.success(data));
            }

            @Override
            public void onError(String message) {
                profileState.setValue(UiState.error(message));
            }
        });
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
                refreshFavoriteAlertIds(data);
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

    public boolean isAlertFavorite(String alertId) {
        return favoriteRepository.isFavorite(FavoriteRepository.keyForAlert(alertId));
    }

    public void toggleAlertFavorite(String alertId) {
        String key = FavoriteRepository.keyForAlert(alertId);
        favoriteRepository.setFavorite(key, !favoriteRepository.isFavorite(key));
        UiState<List<AlertNotification>> current = alertsState.getValue();
        refreshFavoriteAlertIds(current != null ? current.getData() : null);
    }

    private void refreshFavoriteAlertIds(List<AlertNotification> alerts) {
        Set<String> ids = new HashSet<>();
        if (alerts != null) {
            for (AlertNotification alert : alerts) {
                if (favoriteRepository.isFavorite(FavoriteRepository.keyForAlert(alert.getId()))) {
                    ids.add(alert.getId());
                }
            }
        }
        favoriteAlertIds.setValue(ids);
    }
}
