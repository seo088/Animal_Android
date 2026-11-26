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

    private static final int MAX_SELECTED_SYMPTOMS = 5;
    private static final Set<String> EMERGENCY_SYMPTOM_IDS = new HashSet<>(java.util.Arrays.asList(
            "i008", "c041", "e076", "g013"));

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
    private final MutableLiveData<Boolean> speciesSelectionRequired = new MutableLiveData<>(false);

    private int diagnosisRequestGeneration;
    private int hospitalRequestGeneration;
    private boolean conversationStarted;
    private String petName;
    private String animalRouteSpecies;
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

    public int getMaxSelectedSymptoms() {
        return MAX_SELECTED_SYMPTOMS;
    }

    public LiveData<String> getSymptomValidationMessage() {
        return symptomValidationMessage;
    }

    public LiveData<Boolean> getSpeciesSelectionRequired() {
        return speciesSelectionRequired;
    }

    public void initialize() {
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
                animalRouteSpecies = data == null ? null : toAnimalRouteSpecies(data.getPetSpecies());
                speciesSelectionRequired.setValue(animalRouteSpecies == null);
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
        if (animalRouteSpecies == null) {
            addBotMessage("우리 아이는 강아지인가요, 고양이인가요? 아래에서 선택해 주세요.");
            return;
        }
        loadSymptoms();
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
        if (animalRouteSpecies == null) {
            return;
        }
        symptomsState.setValue(UiState.loading());
        diagnosisRepository.getSymptoms(animalRouteSpecies, new RepositoryCallback<List<Symptom>>() {
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

    public void selectSpecies(String species) {
        if (!"dog".equals(species) && !"cat".equals(species)) return;
        animalRouteSpecies = species;
        speciesSelectionRequired.setValue(false);
        addUserMessage("dog".equals(species) ? "강아지" : "고양이");
        addBotMessage("선택했어요. 지금 보이는 증상을 골라 주세요.");
        loadSymptoms();
    }

    public void toggleSymptomSelection(String symptomId, boolean selected) {
        Set<String> current = new HashSet<>(getSelectedIdsSnapshot());
        if (selected) {
            if (current.size() >= MAX_SELECTED_SYMPTOMS) {
                symptomValidationMessage.setValue(getApplication().getString(
                        R.string.diagnosis_max_symptom_selection, MAX_SELECTED_SYMPTOMS));
                selectedSymptomIds.setValue(current);
                return;
            }
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
        if (containsEmergencySignal(symptomIds)) {
            diagnosisState.setValue(null);
            chatPhase.setValue(DiagnosisChatPhase.EMERGENCY);
            addBotMessage("긴급한 진료가 필요한 증상일 수 있어요. 지금 바로 동물병원에 연락하거나 방문해 주세요. 이 안내는 진단이 아닌 긴급 신호 참고 정보입니다.");
            addActionButtonsMessage();
            return;
        }
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

        if (animalRouteSpecies == null) {
            removeLoadingMessage();
            diagnosisState.setValue(UiState.error(
                    getApplication().getString(R.string.diagnosis_species_required)));
            chatPhase.setValue(DiagnosisChatPhase.ERROR);
            return;
        }
        diagnosisRepository.inferDisease(animalRouteSpecies, symptomIds, new RepositoryCallback<List<DiagnosisResult>>() {
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

    private boolean containsEmergencySignal(List<String> symptomIds) {
        for (String symptomId : symptomIds) {
            if (EMERGENCY_SYMPTOM_IDS.contains(symptomId)) return true;
        }
        return false;
    }

    private String toAnimalRouteSpecies(String petSpecies) {
        if (petSpecies == null) {
            return null;
        }
        String normalized = petSpecies.trim().toLowerCase();
        if ("개".equals(normalized) || "dog".equals(normalized) || normalized.contains("강아지")
                || normalized.contains("견") || "말티즈".equals(normalized) || "푸들".equals(normalized)
                || normalized.contains("리트리버")) {
            return "dog";
        }
        if ("고양이".equals(normalized) || "cat".equals(normalized) || normalized.contains("냥")
                || "코리안숏헤어".equals(normalized) || "러시안블루".equals(normalized)
                || "페르시안".equals(normalized)) {
            return "cat";
        }
        return null;
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
