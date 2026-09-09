package com.animalloo.data.mock;

import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.repository.AlertRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class MockAlertRepository implements AlertRepository {

    private final MockDataProvider dataProvider;
    private final MockAsyncHelper asyncHelper;

    public MockAlertRepository() {
        dataProvider = MockDataProvider.getInstance();
        asyncHelper = MockAsyncHelper.getInstance();
    }

    @Override
    public void getRecentAlerts(RepositoryCallback<List<AlertNotification>> callback) {
        asyncHelper.execute(() -> new ArrayList<>(dataProvider.getAlerts()), callback);
    }

    @Override
    public void getAlertById(String id, RepositoryCallback<AlertNotification> callback) {
        asyncHelper.execute(() -> {
            AlertNotification alert = dataProvider.findAlertById(id);
            if (alert == null) {
                throw new IllegalStateException("알림 정보를 찾을 수 없습니다.");
            }
            return alert;
        }, callback);
    }
}
