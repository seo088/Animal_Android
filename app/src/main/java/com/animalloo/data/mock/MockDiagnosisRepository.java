package com.animalloo.data.mock;

import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.Symptom;
import com.animalloo.data.repository.DiagnosisRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockDiagnosisRepository implements DiagnosisRepository {

    private final MockDataProvider dataProvider;
    private final MockAsyncHelper asyncHelper;

    public MockDiagnosisRepository() {
        dataProvider = MockDataProvider.getInstance();
        asyncHelper = MockAsyncHelper.getInstance();
    }

    @Override
    public void getSymptoms(RepositoryCallback<List<Symptom>> callback) {
        asyncHelper.execute(() -> new ArrayList<>(dataProvider.getSymptoms()), callback);
    }

    @Override
    public void inferDisease(List<String> symptomIds, RepositoryCallback<List<DiagnosisResult>> callback) {
        asyncHelper.execute(() -> {
            if (symptomIds == null || symptomIds.isEmpty()) {
                throw new IllegalArgumentException("증상을 하나 이상 선택해 주세요.");
            }

            Map<String, List<String>> symptomMap = dataProvider.getSymptomToDiseaseMap();
            Set<String> diseaseIds = new HashSet<>();

            for (String symptomId : symptomIds) {
                List<String> mapped = symptomMap.get(symptomId);
                if (mapped != null) {
                    diseaseIds.addAll(mapped);
                }
            }

            List<DiagnosisResult> results = new ArrayList<>();
            for (String diseaseId : diseaseIds) {
                DiagnosisResult result = dataProvider.findDiagnosisById(diseaseId);
                if (result != null) {
                    results.add(result);
                }
            }

            results.sort((a, b) -> Integer.compare(b.getProbabilityPercent(), a.getProbabilityPercent()));
            return results;
        }, callback);
    }

    @Override
    public void getDiagnosisById(String id, RepositoryCallback<DiagnosisResult> callback) {
        asyncHelper.execute(() -> {
            DiagnosisResult result = dataProvider.findDiagnosisById(id);
            if (result == null) {
                throw new IllegalStateException("진단 결과를 찾을 수 없습니다.");
            }
            return result;
        }, callback);
    }
}
