package com.animalloo.ui.diagnosis;

import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.Hospital;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChatMessage {

    private final String id;
    private final ChatMessageType type;
    private final String text;
    private final List<DiagnosisResult> results;
    private final List<Hospital> hospitals;

    private ChatMessage(String id, ChatMessageType type, String text,
                        List<DiagnosisResult> results, List<Hospital> hospitals) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.results = results;
        this.hospitals = hospitals;
    }

    public static ChatMessage bot(String text) {
        return new ChatMessage(UUID.randomUUID().toString(), ChatMessageType.BOT_TEXT, text, null, null);
    }

    public static ChatMessage user(String text) {
        return new ChatMessage(UUID.randomUUID().toString(), ChatMessageType.USER_TEXT, text, null, null);
    }

    public static ChatMessage loading(String id) {
        return new ChatMessage(id, ChatMessageType.BOT_LOADING, "", null, null);
    }

    public static ChatMessage results(List<DiagnosisResult> results) {
        List<DiagnosisResult> safeResults = results == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(results);
        return new ChatMessage(UUID.randomUUID().toString(), ChatMessageType.RESULT_LIST, "", safeResults, null);
    }

    public static ChatMessage hospitals(List<Hospital> hospitals) {
        List<Hospital> safeHospitals = hospitals == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(hospitals);
        return new ChatMessage(UUID.randomUUID().toString(), ChatMessageType.HOSPITAL_LIST, "", null, safeHospitals);
    }

    public static ChatMessage actionButtons() {
        return new ChatMessage(UUID.randomUUID().toString(), ChatMessageType.ACTION_BUTTONS, "", null, null);
    }

    public String getId() {
        return id;
    }

    public ChatMessageType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public List<DiagnosisResult> getResults() {
        return results;
    }

    public List<Hospital> getHospitals() {
        return hospitals;
    }
}
