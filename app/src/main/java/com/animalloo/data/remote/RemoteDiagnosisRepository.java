package com.animalloo.data.remote;

import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.Symptom;
import com.animalloo.data.remote.dto.InferRequest;
import com.animalloo.data.repository.DiagnosisRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.util.List;

/**
 * GET /symptoms, POST /diagnosis/infer, GET /diagnosis/{id} (KISTI knowledge graph).
 * Symptom ids are KISTI codes such as "i012", as returned by /symptoms.
 */
public class RemoteDiagnosisRepository implements DiagnosisRepository {

    private final AnimalLooApi api;

    public RemoteDiagnosisRepository(AnimalLooApi api) {
        this.api = api;
    }

    @Override
    public void getSymptoms(RepositoryCallback<List<Symptom>> callback) {
        api.getSymptoms().enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void inferDisease(List<String> symptomIds, RepositoryCallback<List<DiagnosisResult>> callback) {
        api.inferDisease(new InferRequest(symptomIds)).enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void getDiagnosisById(String id, RepositoryCallback<DiagnosisResult> callback) {
        api.getDiagnosis(id).enqueue(ApiCallbacks.of(callback));
    }
}
