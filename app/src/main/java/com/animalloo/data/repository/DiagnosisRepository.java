package com.animalloo.data.repository;

import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.Symptom;

import java.util.List;

public interface DiagnosisRepository {

    void getSymptoms(RepositoryCallback<List<Symptom>> callback);

    void inferDisease(List<String> symptomIds, RepositoryCallback<List<DiagnosisResult>> callback);

    void getDiagnosisById(String id, RepositoryCallback<DiagnosisResult> callback);
}
