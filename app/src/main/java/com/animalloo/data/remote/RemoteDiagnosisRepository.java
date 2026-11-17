package com.animalloo.data.remote;

import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.Symptom;
import com.animalloo.data.remote.dto.InferRequest;
import com.animalloo.data.repository.DiagnosisRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.util.List;

/**
 * Server-side Animal-Route proxy. The API key never reaches this Android client.
 */
public class RemoteDiagnosisRepository implements DiagnosisRepository {

    private final AnimalLooApi api;

    public RemoteDiagnosisRepository(AnimalLooApi api) {
        this.api = api;
    }

    @Override
    public void getSymptoms(String species, RepositoryCallback<List<Symptom>> callback) {
        api.getSymptoms(species).enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void inferDisease(String species, List<String> symptomIds,
                             RepositoryCallback<List<DiagnosisResult>> callback) {
        api.inferDisease(new InferRequest(species, symptomIds)).enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void getDiagnosisById(String id, RepositoryCallback<DiagnosisResult> callback) {
        api.getDiagnosis(id).enqueue(ApiCallbacks.of(callback));
    }
}
