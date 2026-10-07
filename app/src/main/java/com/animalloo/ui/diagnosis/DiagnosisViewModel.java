package com.animalloo.ui.diagnosis;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.animalloo.R;
import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.Hospital;
import com.animalloo.data.model.Symptom;
import com.animalloo.data.model.UiState;
import com.animalloo.data.model.User;
import com.animalloo.data.repository.AuthRepository;
import com.animalloo.data.repository.DiagnosisRepository;
import com.animalloo.data.repository.HomeRepository;
import com.animalloo.data.repository.HospitalRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DiagnosisViewModel extends AndroidViewModel {

    private final DiagnosisRepository diagnosisRepository;
    private final HospitalRepository hospitalRepository;
    private final HomeRepository homeRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<DiagnosisChatPhase> chatPhase =
            new MutableLiveData<>(DiagnosisChatPhase.GREETING);
    private final MutableLiveData<UiState<List<Symptom>>> symptomsState = new MutableLiveData<>();
    private final MutableLiveData<UiState<List<DiagnosisResult>>> diagnosisState = new MutableLiveData<>();
    private final MutableLiveData<UiState<List<Hospital>>> hospitalsState = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> selectedSymptomIds = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<String> symptomValidationMessage = new MutableLiveData<>();

    private int diagnosisRequestGeneration;
    private int hospitalRequestGeneration;
    private boolean conversationStarted;
    private String petName;
    private String loadingMessageId;

    public DiagnosisViewModel(@NonNull Application application) {
        super(application);
        RepositoryProvider provider = RepositoryProvider.getInstance();
        diagnosisRepository = provider.getDiagnosisRepository();
        hospitalRepository = provider.getHospitalRepository();
        homeRepository = provider.getHomeRepository();
        authRepository = provider.getAuthRepository();
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public LiveData<DiagnosisChatPhase> getChatPhase() {
        return chatPhase;
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

    public void initialize() {
        ensureSymptomsLoaded();
        if (!conversationStarted) {
            loadPetProfileAndStartConversation();
        }
    }

    private void loadPetProfileAndStartConversation() {
        User user = authRepository.getCurrentUser();
        String displayName = user != null ? user.getDisplayName() : null;
        homeRepository.getHomeProfile(displayName, new RepositoryCallback<HomeProfile>() {
            @Override
            public void onSuccess(HomeProfile data) {
                if (data != null && data.getPetName() != null && !data.getPetName().isEmpty()) {
                    petName = data.getPetName();
                }
                startConversationIfNeeded();
            }

            @Override
            public void onError(String message) {
                startConversationIfNeeded();
            }
        });
    }

    private void startConversationIfNeeded() {
        if (!conversationStarted) {
            startConversation();
        }
    }

    private void startConversation() {
        conversationStarted = true;
        chatPhase.setValue(DiagnosisChatPhase.SYMPTOM_SELECT);
        addBotMessage(getApplication().getString(R.string.diagnosis_chat_intro));
        addBotMessage(getSymptomPromptText());
    }

    private String getSymptomPromptText() {
        if (petName != null && !petName.isEmpty()) {
            return getApplication().getString(R.string.diagnosis_chat_greeting_with_pet, petName);
        }
        return getApplication().getString(R.string.diagnosis_chat_greeting_generic);
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

    public void confirmSymptoms() {
        List<String> symptomIds = new ArrayList<>(getSelectedIdsSnapshot());
        if (symptomIds.isEmpty()) {
            symptomValidationMessage.setValue(
                    getApplication().getString(R.string.diagnosis_chat_select_symptom_validation));
            return;
        }

        symptomValidationMessage.setValue(null);
        addUserMessage(buildSelectedSymptomSummary(symptomIds));
        showLoadingMessage();
        chatPhase.setValue(DiagnosisChatPhase.ANALYZING);
        runDiagnosis(symptomIds);
    }

    public void retryDiagnosis() {
        List<String> symptomIds = new ArrayList<>(getSelectedIdsSnapshot());
        if (symptomIds.isEmpty()) {
            modifySymptoms();
            symptomValidationMessage.setValue(
                    getApplication().getString(R.string.diagnosis_chat_select_symptom_validation));
            return;
        }
        showLoadingMessage();
        chatPhase.setValue(DiagnosisChatPhase.ANALYZING);
        runDiagnosis(symptomIds);
    }

    private void runDiagnosis(List<String> symptomIds) {
        diagnosisState.setValue(UiState.loading());
        hospitalsState.setValue(null);
        int generation = ++diagnosisRequestGeneration;

        diagnosisRepository.inferDisease(symptomIds, new RepositoryCallback<List<DiagnosisResult>>() {
            @Override
            public void onSuccess(List<DiagnosisResult> data) {
                if (generation != diagnosisRequestGeneration) {
                    return;
                }
                removeLoadingMessage();
                if (data == null || data.isEmpty()) {
                    diagnosisState.setValue(UiState.empty());
                    chatPhase.setValue(DiagnosisChatPhase.ERROR);
                    addBotMessage(getApplication().getString(R.string.diagnosis_chat_result_empty));
                } else {
                    diagnosisState.setValue(UiState.success(data));
                    chatPhase.setValue(DiagnosisChatPhase.RESULT);
                    addResultsMessage(data);
                    addBotMessage(getApplication().getString(R.string.diagnosis_chat_find_hospitals_prompt));
                    addActionButtonsMessage();
                }
            }

            @Override
            public void onError(String message) {
                if (generation != diagnosisRequestGeneration) {
                    return;
                }
                removeLoadingMessage();
                diagnosisState.setValue(UiState.error(message));
                chatPhase.setValue(DiagnosisChatPhase.ERROR);
                addBotMessage(message);
            }
        });
    }

    public void loadNearbyHospitals() {
        hospitalsState.setValue(UiState.loading());
        chatPhase.setValue(DiagnosisChatPhase.HOSPITAL);
        showLoadingMessage();
        int generation = ++hospitalRequestGeneration;

        hospitalRepository.getNearbyHospitals(new RepositoryCallback<List<Hospital>>() {
            @Override
            public void onSuccess(List<Hospital> data) {
                if (generation != hospitalRequestGeneration) {
                    return;
                }
                removeLoadingMessage();
                List<Hospital> openHospitals = filterOpenHospitals(data, 3);
                if (openHospitals.isEmpty()) {
                    hospitalsState.setValue(UiState.empty());
                    addBotMessage(getApplication().getString(R.string.diagnosis_chat_hospitals_empty));
                } else {
                    hospitalsState.setValue(UiState.success(openHospitals));
                    addHospitalsMessage(openHospitals);
                }
            }

            @Override
            public void onError(String message) {
                if (generation != hospitalRequestGeneration) {
                    return;
                }
                removeLoadingMessage();
                hospitalsState.setValue(UiState.error(message));
                addBotMessage(message);
            }
        });
    }

    public void modifySymptoms() {
        diagnosisRequestGeneration++;
        hospitalRequestGeneration++;
        diagnosisState.setValue(null);
        hospitalsState.setValue(null);
        chatPhase.setValue(DiagnosisChatPhase.SYMPTOM_SELECT);
        addBotMessage(getApplication().getString(R.string.diagnosis_chat_modify_symptoms));
    }

    public void backToResults() {
        hospitalRequestGeneration++;
        hospitalsState.setValue(null);
        chatPhase.setValue(DiagnosisChatPhase.RESULT);
    }

    public void restartDiagnosis() {
        diagnosisRequestGeneration++;
        hospitalRequestGeneration++;
        selectedSymptomIds.setValue(new HashSet<>());
        diagnosisState.setValue(null);
        hospitalsState.setValue(null);
        symptomValidationMessage.setValue(null);
        chatPhase.setValue(DiagnosisChatPhase.SYMPTOM_SELECT);
        messages.setValue(new ArrayList<>());
        addBotMessage(getApplication().getString(R.string.diagnosis_chat_intro));
        addBotMessage(getSymptomPromptText());
    }

    private void addBotMessage(String text) {
        List<ChatMessage> current = new ArrayList<>(getMessagesSnapshot());
        current.add(ChatMessage.bot(text));
        messages.setValue(current);
    }

    private void addUserMessage(String text) {
        List<ChatMessage> current = new ArrayList<>(getMessagesSnapshot());
        current.add(ChatMessage.user(text));
        messages.setValue(current);
    }

    private void addResultsMessage(List<DiagnosisResult> results) {
        List<ChatMessage> current = new ArrayList<>(getMessagesSnapshot());
        current.add(ChatMessage.results(results));
        messages.setValue(current);
    }

    private void addHospitalsMessage(List<Hospital> hospitals) {
        List<ChatMessage> current = new ArrayList<>(getMessagesSnapshot());
        current.add(ChatMessage.hospitals(hospitals));
        messages.setValue(current);
    }

    private void addActionButtonsMessage() {
        List<ChatMessage> current = new ArrayList<>(getMessagesSnapshot());
        current.add(ChatMessage.actionButtons());
        messages.setValue(current);
    }

    private void showLoadingMessage() {
        loadingMessageId = UUID.randomUUID().toString();
        List<ChatMessage> current = new ArrayList<>(getMessagesSnapshot());
        current.add(ChatMessage.loading(loadingMessageId));
        messages.setValue(current);
    }

    private void removeLoadingMessage() {
        if (loadingMessageId == null) {
            return;
        }
        List<ChatMessage> current = new ArrayList<>(getMessagesSnapshot());
        current.removeIf(message -> message.getId().equals(loadingMessageId));
        messages.setValue(current);
        loadingMessageId = null;
    }

    private String buildSelectedSymptomSummary(List<String> symptomIds) {
        UiState<List<Symptom>> state = symptomsState.getValue();
        if (state == null || !state.isSuccess() || state.getData() == null) {
            return String.join(", ", symptomIds);
        }

        List<String> names = new ArrayList<>();
        for (Symptom symptom : state.getData()) {
            if (symptomIds.contains(symptom.getId())) {
                names.add(symptom.getName());
            }
        }
        return String.join(", ", names);
    }

    private List<ChatMessage> getMessagesSnapshot() {
        List<ChatMessage> current = messages.getValue();
        if (current == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(current);
    }

    private Set<String> getSelectedIdsSnapshot() {
        Set<String> current = selectedSymptomIds.getValue();
        if (current == null) {
            return new HashSet<>();
        }
        return new HashSet<>(current);
    }

    private List<Hospital> filterOpenHospitals(List<Hospital> hospitals, int limit) {
        List<Hospital> openHospitals = new ArrayList<>();
        if (hospitals == null) {
            return openHospitals;
        }
        for (Hospital hospital : hospitals) {
            if (hospital.isOpenNow()) {
                openHospitals.add(hospital);
            }
        }
        openHospitals.sort((a, b) -> Double.compare(a.getDistanceKm(), b.getDistanceKm()));
        if (openHospitals.size() <= limit) {
            return openHospitals;
        }
        return new ArrayList<>(openHospitals.subList(0, limit));
    }
}
