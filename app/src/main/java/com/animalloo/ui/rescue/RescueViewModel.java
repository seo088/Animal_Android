package com.animalloo.ui.rescue;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.AlertType;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.data.model.MatchResult;
import com.animalloo.data.model.UiState;
import com.animalloo.data.repository.AlertRepository;
import com.animalloo.data.repository.HomeRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

import java.util.ArrayList;
import java.util.List;

public class RescueViewModel extends ViewModel {

    private final HomeRepository homeRepository;
    private final AlertRepository alertRepository;

    private final MutableLiveData<UiState<HomeStats>> statsState = new MutableLiveData<>();
    private final MutableLiveData<UiState<List<AlertNotification>>> alertsState = new MutableLiveData<>();
    private final MutableLiveData<RescueTimelineFilter> filterType =
            new MutableLiveData<>(RescueTimelineFilter.RESCUE);
    private final MediatorLiveData<List<RescueTimelineItem>> timelineItems = new MediatorLiveData<>();

    @Nullable
    private LostAnimalReport myReport;
    private int myReportMatchCount;

    public RescueViewModel() {
        RepositoryProvider provider = RepositoryProvider.getInstance();
        homeRepository = provider.getHomeRepository();
        alertRepository = provider.getAlertRepository();

        timelineItems.addSource(alertsState, ignored -> rebuildTimeline());
        timelineItems.addSource(filterType, ignored -> rebuildTimeline());
    }

    public LiveData<UiState<HomeStats>> getStatsState() {
        return statsState;
    }

    public LiveData<UiState<List<AlertNotification>>> getAlertsState() {
        return alertsState;
    }

    public LiveData<RescueTimelineFilter> getFilterType() {
        return filterType;
    }

    public LiveData<List<RescueTimelineItem>> getTimelineItems() {
        return timelineItems;
    }

    public void loadData() {
        loadStats();
        loadAlerts();
    }

    public void refresh() {
        loadData();
    }

    public void setFilter(RescueTimelineFilter filter) {
        filterType.setValue(filter);
    }

    public void updateMyReport(@Nullable LostAnimalReport report,
                               @Nullable UiState<List<MatchResult>> matchState) {
        myReport = report;
        myReportMatchCount = 0;
        if (matchState != null && matchState.isSuccess() && matchState.getData() != null) {
            myReportMatchCount = matchState.getData().size();
        }
        rebuildTimeline();
    }

    public void clearMyReport() {
        myReport = null;
        myReportMatchCount = 0;
        rebuildTimeline();
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

    private void rebuildTimeline() {
        List<RescueTimelineItem> items = new ArrayList<>();
        if (myReport != null) {
            items.add(RescueTimelineItem.myReport(myReport, myReportMatchCount));
        }

        List<AlertNotification> filteredAlerts = getFilteredAlerts();
        for (AlertNotification alert : filteredAlerts) {
            items.add(RescueTimelineItem.timelineEvent(alert));
        }
        timelineItems.setValue(items);
    }

    private List<AlertNotification> getFilteredAlerts() {
        UiState<List<AlertNotification>> state = alertsState.getValue();
        if (state == null || !state.isSuccess() || state.getData() == null) {
            return new ArrayList<>();
        }

        RescueTimelineFilter filter = filterType.getValue();
        if (filter == null) {
            filter = RescueTimelineFilter.RESCUE;
        }

        List<AlertNotification> filtered = new ArrayList<>();
        for (AlertNotification alert : state.getData()) {
            if (filter == RescueTimelineFilter.RESCUE && alert.getType() == AlertType.RESCUE) {
                filtered.add(alert);
            } else if (filter == RescueTimelineFilter.LOST && alert.getType() == AlertType.LOST) {
                filtered.add(alert);
            }
        }
        return filtered;
    }
}
