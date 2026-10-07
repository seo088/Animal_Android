package com.animalloo.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.animalloo.R;
import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.Hospital;
import com.animalloo.ui.diagnosis.ChatMessage;
import com.animalloo.ui.diagnosis.ChatMessageType;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DiagnosisChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnChatActionListener {
        void onFindHospitals();

        void onModifySymptoms();
    }

    public interface OnHospitalActionListener {
        void onCall(Hospital hospital);

        void onDirections(Hospital hospital);

        void onDetail(Hospital hospital);
    }

    private static final int VIEW_TYPE_BOT = 0;
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final int VIEW_TYPE_RESULTS = 3;
    private static final int VIEW_TYPE_HOSPITALS = 4;
    private static final int VIEW_TYPE_ACTIONS = 5;

    private final List<ChatMessage> items = new ArrayList<>();
    private OnChatActionListener actionListener;
    private OnHospitalActionListener hospitalActionListener;

    public void setMessages(List<ChatMessage> messages) {
        items.clear();
        if (messages != null) {
            items.addAll(messages);
        }
        notifyDataSetChanged();
    }

    public void setOnChatActionListener(OnChatActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setOnHospitalActionListener(OnHospitalActionListener hospitalActionListener) {
        this.hospitalActionListener = hospitalActionListener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageType type = items.get(position).getType();
        if (type == ChatMessageType.USER_TEXT) {
            return VIEW_TYPE_USER;
        }
        if (type == ChatMessageType.BOT_LOADING) {
            return VIEW_TYPE_LOADING;
        }
        if (type == ChatMessageType.RESULT_LIST) {
            return VIEW_TYPE_RESULTS;
        }
        if (type == ChatMessageType.HOSPITAL_LIST) {
            return VIEW_TYPE_HOSPITALS;
        }
        if (type == ChatMessageType.ACTION_BUTTONS) {
            return VIEW_TYPE_ACTIONS;
        }
        return VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            return new TextViewHolder(inflater.inflate(R.layout.item_chat_user, parent, false));
        }
        if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(inflater.inflate(R.layout.item_chat_loading, parent, false));
        }
        if (viewType == VIEW_TYPE_RESULTS) {
            return new ResultsViewHolder(inflater.inflate(R.layout.item_chat_results, parent, false));
        }
        if (viewType == VIEW_TYPE_HOSPITALS) {
            return new HospitalsViewHolder(inflater.inflate(R.layout.item_chat_hospitals, parent, false));
        }
        if (viewType == VIEW_TYPE_ACTIONS) {
            return new ActionsViewHolder(inflater.inflate(R.layout.item_chat_actions, parent, false));
        }
        return new TextViewHolder(inflater.inflate(R.layout.item_chat_bot, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = items.get(position);
        boolean showBotAvatar = shouldShowBotAvatar(position);
        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).bind(message.getText(), showBotAvatar);
        } else if (holder instanceof LoadingViewHolder) {
            ((LoadingViewHolder) holder).bind(showBotAvatar);
        } else if (holder instanceof ResultsViewHolder) {
            ((ResultsViewHolder) holder).bind(message.getResults(), showBotAvatar);
        } else if (holder instanceof HospitalsViewHolder) {
            ((HospitalsViewHolder) holder).bind(
                    message.getHospitals(), hospitalActionListener, showBotAvatar);
        } else if (holder instanceof ActionsViewHolder) {
            ((ActionsViewHolder) holder).bind(actionListener, showBotAvatar);
        }
    }

    private boolean shouldShowBotAvatar(int position) {
        if (position <= 0) {
            return true;
        }
        return !isBotMessage(items.get(position - 1));
    }

    private boolean isBotMessage(ChatMessage message) {
        ChatMessageType type = message.getType();
        return type != ChatMessageType.USER_TEXT;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {

        private final TextView messageView;
        private final View botAvatar;

        TextViewHolder(@NonNull View itemView) {
            super(itemView);
            messageView = itemView.findViewById(R.id.tv_message);
            botAvatar = itemView.findViewById(R.id.chat_bot_avatar);
        }

        void bind(String text, boolean showBotAvatar) {
            messageView.setText(text);
            bindBotAvatarVisibility(botAvatar, showBotAvatar);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {

        private final View botAvatar;

        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            botAvatar = itemView.findViewById(R.id.chat_bot_avatar);
        }

        void bind(boolean showBotAvatar) {
            bindBotAvatarVisibility(botAvatar, showBotAvatar);
        }
    }

    static class ResultsViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout resultsContainer;
        private final View botAvatar;
        private final LayoutInflater inflater;

        ResultsViewHolder(@NonNull View itemView) {
            super(itemView);
            resultsContainer = itemView.findViewById(R.id.results_container);
            botAvatar = itemView.findViewById(R.id.chat_bot_avatar);
            inflater = LayoutInflater.from(itemView.getContext());
        }

        void bind(List<DiagnosisResult> results, boolean showBotAvatar) {
            bindBotAvatarVisibility(botAvatar, showBotAvatar);
            resultsContainer.removeAllViews();
            if (results == null || results.isEmpty()) {
                return;
            }

            int topSpacing = itemView.getResources().getDimensionPixelSize(R.dimen.spacing_sm);
            for (int i = 0; i < results.size(); i++) {
                DiagnosisResult result = results.get(i);
                View entry = inflater.inflate(R.layout.item_chat_result_entry, resultsContainer, false);
                bindResultEntry(entry, result);

                View divider = entry.findViewById(R.id.view_result_divider);
                divider.setVisibility(i == results.size() - 1 ? View.GONE : View.VISIBLE);

                if (i > 0) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.topMargin = topSpacing;
                    entry.setLayoutParams(params);
                }

                resultsContainer.addView(entry);
            }
        }

        private void bindResultEntry(View entry, DiagnosisResult result) {
            TextView diseaseName = entry.findViewById(R.id.tv_disease_name);
            TextView probability = entry.findViewById(R.id.tv_probability);
            TextView relatedSymptoms = entry.findViewById(R.id.tv_related_symptoms);
            TextView description = entry.findViewById(R.id.tv_description);
            TextView caution = entry.findViewById(R.id.tv_caution);

            diseaseName.setText(result.getDiseaseName());
            probability.setText(
                    entry.getContext().getString(
                            R.string.diagnosis_probability_format,
                            result.getProbabilityPercent()));
            relatedSymptoms.setText(
                    entry.getContext().getString(
                            R.string.diagnosis_related_symptoms,
                            TextUtils.join(", ", result.getRelatedSymptoms())));
            description.setText(result.getDescription());
            caution.setText(result.getCaution());
        }
    }

    static class HospitalsViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout hospitalsContainer;
        private final View botAvatar;
        private final LayoutInflater inflater;

        HospitalsViewHolder(@NonNull View itemView) {
            super(itemView);
            hospitalsContainer = itemView.findViewById(R.id.hospitals_container);
            botAvatar = itemView.findViewById(R.id.chat_bot_avatar);
            inflater = LayoutInflater.from(itemView.getContext());
        }

        void bind(List<Hospital> hospitals, OnHospitalActionListener listener, boolean showBotAvatar) {
            bindBotAvatarVisibility(botAvatar, showBotAvatar);
            hospitalsContainer.removeAllViews();
            if (hospitals == null || hospitals.isEmpty()) {
                return;
            }

            int topSpacing = itemView.getResources().getDimensionPixelSize(R.dimen.spacing_sm);
            for (int i = 0; i < hospitals.size(); i++) {
                Hospital hospital = hospitals.get(i);
                View entry = inflater.inflate(R.layout.item_chat_hospital_entry, hospitalsContainer, false);
                bindHospitalEntry(entry, hospital, listener);

                View divider = entry.findViewById(R.id.view_hospital_divider);
                divider.setVisibility(i == hospitals.size() - 1 ? View.GONE : View.VISIBLE);

                if (i > 0) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.topMargin = topSpacing;
                    entry.setLayoutParams(params);
                }

                hospitalsContainer.addView(entry);
            }
        }

        private void bindHospitalEntry(View entry, Hospital hospital, OnHospitalActionListener listener) {
            TextView name = entry.findViewById(R.id.tv_hospital_name);
            TextView openStatus = entry.findViewById(R.id.tv_hospital_open_status);
            TextView distance = entry.findViewById(R.id.tv_hospital_distance);
            TextView phone = entry.findViewById(R.id.tv_hospital_phone);
            TextView address = entry.findViewById(R.id.tv_hospital_address);
            MaterialButton callButton = entry.findViewById(R.id.btn_hospital_call);
            MaterialButton directionsButton = entry.findViewById(R.id.btn_hospital_directions);
            MaterialButton detailButton = entry.findViewById(R.id.btn_hospital_detail);

            name.setText(hospital.getName());
            address.setText(hospital.getAddress());
            phone.setText(hospital.getPhone());
            distance.setText(
                    entry.getContext().getString(
                            R.string.diagnosis_hospital_distance_sample,
                            hospital.getDistanceKm()));
            openStatus.setText(
                    entry.getContext().getString(
                            hospital.isOpenNow() ? R.string.hospital_open : R.string.hospital_closed));
            openStatus.setBackgroundResource(
                    hospital.isOpenNow() ? R.drawable.bg_badge_success : R.drawable.bg_badge_warning);

            callButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCall(hospital);
                }
            });
            directionsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDirections(hospital);
                }
            });
            detailButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetail(hospital);
                }
            });
        }
    }

    static class ActionsViewHolder extends RecyclerView.ViewHolder {

        private final MaterialButton findHospitalsButton;
        private final MaterialButton modifySymptomsButton;
        private final View botAvatar;

        ActionsViewHolder(@NonNull View itemView) {
            super(itemView);
            findHospitalsButton = itemView.findViewById(R.id.btn_chat_find_hospitals);
            modifySymptomsButton = itemView.findViewById(R.id.btn_chat_modify_symptoms);
            botAvatar = itemView.findViewById(R.id.chat_bot_avatar);
        }

        void bind(OnChatActionListener listener, boolean showBotAvatar) {
            bindBotAvatarVisibility(botAvatar, showBotAvatar);
            findHospitalsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFindHospitals();
                }
            });
            modifySymptomsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onModifySymptoms();
                }
            });
        }
    }

    private static void bindBotAvatarVisibility(View botAvatar, boolean showBotAvatar) {
        if (botAvatar == null) {
            return;
        }
        botAvatar.setVisibility(showBotAvatar ? View.VISIBLE : View.INVISIBLE);
    }
}
