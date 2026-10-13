package com.animalloo.data.remote;

import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.repository.AlertRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.util.List;

/** GET /alerts, /alerts/{id} — recent rescues and lost reports. */
public class RemoteAlertRepository implements AlertRepository {

    private static final int RECENT_LIMIT = 20;

    private final AnimalLooApi api;

    public RemoteAlertRepository(AnimalLooApi api) {
        this.api = api;
    }

    @Override
    public void getRecentAlerts(RepositoryCallback<List<AlertNotification>> callback) {
        api.getAlerts(RECENT_LIMIT).enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void getAlertById(String id, RepositoryCallback<AlertNotification> callback) {
        api.getAlert(id).enqueue(ApiCallbacks.of(callback));
    }
}
