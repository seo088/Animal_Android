package com.animalloo.data.repository;

import com.animalloo.data.model.AlertNotification;

import java.util.List;

public interface AlertRepository {

    void getRecentAlerts(RepositoryCallback<List<AlertNotification>> callback);

    void getAlertById(String id, RepositoryCallback<AlertNotification> callback);
}
