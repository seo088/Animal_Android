package com.animalloo.data.repository;

import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.Symptom;

import java.util.List;

public interface DiagnosisRepository {

    void getSymptoms(String species, RepositoryCallback<List<Symptom>> callback);

    void inferDisease(String species, List<String> symptomIds,
                      RepositoryCallback<List<DiagnosisResult>> callback);

    void getDiagnosisById(String id, RepositoryCallback<DiagnosisResult> callback);
}
