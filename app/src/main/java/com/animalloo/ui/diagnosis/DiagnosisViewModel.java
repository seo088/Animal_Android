package com.animalloo.ui.diagnosis;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.Hospital;
import com.animalloo.data.model.Symptom;
import com.animalloo.data.model.UiState;
import com.animalloo.data.repository.DiagnosisRepository;
import com.animalloo.data.repository.HospitalRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiagnosisViewModel extends ViewModel {

    private final DiagnosisRepository diagnosisRepository;
    private final HospitalRepository hospitalRepository;

    private final MutableLiveData<DiagnosisStep> currentStep = new MutableLiveData<>(DiagnosisStep.SYMPTOM);
    private final MutableLiveData<UiState<List<Symptom>>> symptomsState = new MutableLiveData<>();
    private final MutableLiveData<UiState<List<DiagnosisResult>>> diagnosisState = new MutableLiveData<>();
    private final MutableLiveData<UiState<List<Hospital>>> hospitalsState = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> selectedSymptomIds = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<String> symptomValidationMessage = new MutableLiveData<>();

    private int diagnosisRequestGeneration;
    private int hospitalRequestGeneration;

    public DiagnosisViewModel() {
        RepositoryProvider provider = RepositoryProvider.getInstance();
        diagnosisRepository = provider.getDiagnosisRepository();
        hospitalRepository = provider.getHospitalRepository();
    }

    public LiveData<DiagnosisStep> getCurrentStep() {
        return currentStep;
    }

    public LiveData<UiState<List<Symptom>>> getSymptomsState() {
        return symptomsState;
    }

    public LiveData<UiState<List<DiagnosisResult>>> getDiagnosisState() {
        return diagnosisState;
    }

    public LiveData<UiState<List<Hospital>>> getHospitalsState() {
        return hospitalsState;
    }

    public LiveData<Set<String>> getSelectedSymptomIds() {
        return selectedSymptomIds;
    }

    public LiveData<String> getSymptomValidationMessage() {
        return symptomValidationMessage;
    }

    public void ensureSymptomsLoaded() {
        UiState<List<Symptom>> current = symptomsState.getValue();
        if (current == null) {
            loadSymptoms();
        }
    }

    public void loadSymptoms() {
        symptomsState.setValue(UiState.loading());
        diagnosisRepository.getSymptoms(new RepositoryCallback<List<Symptom>>() {
            @Override
            public void onSuccess(List<Symptom> data) {
                if (data == null || data.isEmpty()) {
                    symptomsState.setValue(UiState.empty());
                } else {
                    symptomsState.setValue(UiState.success(data));
                }
            }

            @Override
            public void onError(String message) {
                symptomsState.setValue(UiState.error(message));
            }
        });
    }

    public void toggleSymptomSelection(String symptomId, boolean selected) {
        Set<String> current = new HashSet<>(getSelectedIdsSnapshot());
        if (selected) {
            current.add(symptomId);
        } else {
            current.remove(symptomId);
        }
        selectedSymptomIds.setValue(current);
        if (!current.isEmpty()) {
            symptomValidationMessage.setValue(null);
        }
    }

    public void diagnoseSelectedSymptoms() {
        List<String> symptomIds = new ArrayList<>(getSelectedIdsSnapshot());
        if (symptomIds.isEmpty()) {
            symptomValidationMessage.setValue("증상을 하나 이상 선택해 주세요.");
            return;
        }

        symptomValidationMessage.setValue(null);
        runDiagnosis(symptomIds);
    }

    public void retryDiagnosis() {
        List<String> symptomIds = new ArrayList<>(getSelectedIdsSnapshot());
        if (symptomIds.isEmpty()) {
            backToSymptoms();
            symptomValidationMessage.setValue("증상을 하나 이상 선택해 주세요.");
            return;
        }
        runDiagnosis(symptomIds);
    }

    private void runDiagnosis(List<String> symptomIds) {
        diagnosisState.setValue(UiState.loading());
        currentStep.setValue(DiagnosisStep.RESULT);
        hospitalsState.setValue(null);
        int generation = ++diagnosisRequestGeneration;

        diagnosisRepository.inferDisease(symptomIds, new RepositoryCallback<List<DiagnosisResult>>() {
            @Override
            public void onSuccess(List<DiagnosisResult> data) {
                if (generation != diagnosisRequestGeneration) {
                    return;
                }
                if (data == null || data.isEmpty()) {
                    diagnosisState.setValue(UiState.empty());
                } else {
                    diagnosisState.setValue(UiState.success(data));
                }
            }

            @Override
            public void onError(String message) {
                if (generation != diagnosisRequestGeneration) {
                    return;
                }
                diagnosisState.setValue(UiState.error(message));
            }
        });
    }

    public void loadNearbyHospitals() {
        hospitalsState.setValue(UiState.loading());
        currentStep.setValue(DiagnosisStep.HOSPITAL);
        int generation = ++hospitalRequestGeneration;

        hospitalRepository.getNearbyHospitals(new RepositoryCallback<List<Hospital>>() {
            @Override
            public void onSuccess(List<Hospital> data) {
                if (generation != hospitalRequestGeneration) {
                    return;
                }
                if (data == null || data.isEmpty()) {
                    hospitalsState.setValue(UiState.empty());
                } else {
                    hospitalsState.setValue(UiState.success(data));
                }
            }

            @Override
            public void onError(String message) {
                if (generation != hospitalRequestGeneration) {
                    return;
                }
                hospitalsState.setValue(UiState.error(message));
            }
        });
    }

    public void backToSymptoms() {
        diagnosisRequestGeneration++;
        hospitalRequestGeneration++;
        currentStep.setValue(DiagnosisStep.SYMPTOM);
        diagnosisState.setValue(null);
        hospitalsState.setValue(null);
    }

    public void backToResults() {
        hospitalRequestGeneration++;
        currentStep.setValue(DiagnosisStep.RESULT);
        hospitalsState.setValue(null);
    }

    public void restartDiagnosis() {
        diagnosisRequestGeneration++;
        hospitalRequestGeneration++;
        selectedSymptomIds.setValue(new HashSet<>());
        diagnosisState.setValue(null);
        hospitalsState.setValue(null);
        symptomValidationMessage.setValue(null);
        currentStep.setValue(DiagnosisStep.SYMPTOM);
        ensureSymptomsLoaded();
    }

    private Set<String> getSelectedIdsSnapshot() {
        Set<String> current = selectedSymptomIds.getValue();
        if (current == null) {
            return new HashSet<>();
        }
        return new HashSet<>(current);
    }
}
