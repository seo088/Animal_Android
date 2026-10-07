package com.animalloo.ui.diagnosis;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.animalloo.R;
import com.animalloo.adapter.DiagnosisChatAdapter;
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.Hospital;
import com.animalloo.data.model.Symptom;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentDiagnosisBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.ui.detail.DetailNavigator;
import com.animalloo.util.FacilityActions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Set;

public class DiagnosisFragment extends BaseFragment {

    private FragmentDiagnosisBinding binding;
    private DiagnosisViewModel viewModel;
    private DiagnosisChatAdapter chatAdapter;
    private DetailNavigator detailNavigator;
    private boolean syncingChipSelection;
    private OnBackPressedCallback backPressedCallback;

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof DetailNavigator) {
            detailNavigator = (DetailNavigator) context;
        } else {
            throw new IllegalStateException("Host Activity must implement DetailNavigator");
        }
    }

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull LayoutInflater inflater,
                                          @Nullable ViewGroup container,
                                          @Nullable android.os.Bundle savedInstanceState) {
        binding = FragmentDiagnosisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable android.os.Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DiagnosisViewModel.class);
        setupChat();
        setupSymptomPanel();
        setupBackNavigation();
        observeViewModel();
        viewModel.initialize();
    }

    private void setupChat() {
        chatAdapter = new DiagnosisChatAdapter();
        chatAdapter.setOnChatActionListener(new DiagnosisChatAdapter.OnChatActionListener() {
            @Override
            public void onFindHospitals() {
                viewModel.loadNearbyHospitals();
            }

            @Override
            public void onModifySymptoms() {
                viewModel.modifySymptoms();
            }
        });
        chatAdapter.setOnHospitalActionListener(new DiagnosisChatAdapter.OnHospitalActionListener() {
            @Override
            public void onCall(Hospital hospital) {
                FacilityActions.dial(DiagnosisFragment.this, binding.getRoot(), hospital);
            }

            @Override
            public void onDirections(Hospital hospital) {
                FacilityActions.directions(DiagnosisFragment.this, binding.getRoot(), hospital);
            }

            @Override
            public void onDetail(Hospital hospital) {
                detailNavigator.navigateToDetail(DetailType.HOSPITAL, hospital.getId());
            }
        });
        binding.rvChat.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvChat.setAdapter(chatAdapter);
    }

    private void setupSymptomPanel() {
        binding.btnConfirmSymptoms.setOnClickListener(v -> viewModel.confirmSymptoms());
    }

    private void setupBackNavigation() {
        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                DiagnosisChatPhase phase = viewModel.getChatPhase().getValue();
                if (phase == DiagnosisChatPhase.HOSPITAL) {
                    viewModel.backToResults();
                } else if (phase == DiagnosisChatPhase.RESULT) {
                    viewModel.modifySymptoms();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), backPressedCallback);
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            chatAdapter.setMessages(messages);
            if (messages != null && !messages.isEmpty()) {
                binding.rvChat.scrollToPosition(messages.size() - 1);
            }
        });
        viewModel.getChatPhase().observe(getViewLifecycleOwner(), this::renderChatPhase);
        viewModel.getSymptomsState().observe(getViewLifecycleOwner(), this::renderSymptomsState);
        viewModel.getSelectedSymptomIds().observe(getViewLifecycleOwner(), this::syncChipSelection);
        viewModel.getSymptomValidationMessage().observe(getViewLifecycleOwner(), message -> {
            if (TextUtils.isEmpty(message)) {
                binding.tvSymptomValidation.setVisibility(View.GONE);
            } else {
                binding.tvSymptomValidation.setText(message);
                binding.tvSymptomValidation.setVisibility(View.VISIBLE);
            }
        });
    }

    private void renderChatPhase(DiagnosisChatPhase phase) {
        if (phase == null) {
            return;
        }

        boolean showSymptomPanel = phase == DiagnosisChatPhase.SYMPTOM_SELECT
                || phase == DiagnosisChatPhase.GREETING;
        binding.layoutSymptomPanel.setVisibility(showSymptomPanel ? View.VISIBLE : View.GONE);
        syncBackCallbackEnabled(phase);
    }

    private void syncBackCallbackEnabled(DiagnosisChatPhase phase) {
        if (backPressedCallback == null) {
            return;
        }
        backPressedCallback.setEnabled(shouldHandleBack(phase));
    }

    private boolean shouldHandleBack(@Nullable DiagnosisChatPhase phase) {
        if (phase == null
                || phase == DiagnosisChatPhase.SYMPTOM_SELECT
                || phase == DiagnosisChatPhase.GREETING
                || phase == DiagnosisChatPhase.ANALYZING) {
            return false;
        }
        if (!isResumed() || isHidden() || !isVisible()) {
            return false;
        }
        if (getActivity() == null) {
            return false;
        }
        View detailContainer = getActivity().findViewById(R.id.detail_container);
        return detailContainer == null || detailContainer.getVisibility() != View.VISIBLE;
    }

    @Override
    public void onResume() {
        super.onResume();
        DiagnosisChatPhase phase = viewModel != null ? viewModel.getChatPhase().getValue() : null;
        syncBackCallbackEnabled(phase);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        DiagnosisChatPhase phase = viewModel != null ? viewModel.getChatPhase().getValue() : null;
        syncBackCallbackEnabled(phase);
    }

    private void renderSymptomsState(UiState<List<Symptom>> state) {
        if (state == null) {
            return;
        }

        if (state.isLoading()) {
            binding.chipGroupSymptoms.setVisibility(View.GONE);
            binding.btnConfirmSymptoms.setEnabled(false);
            showStateView(binding.symptomStateContainer, R.layout.layout_loading, null);
            return;
        }

        hideStateView(binding.symptomStateContainer);

        if (state.isError()) {
            binding.chipGroupSymptoms.setVisibility(View.GONE);
            binding.btnConfirmSymptoms.setEnabled(false);
            showStateView(binding.symptomStateContainer, R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setOnClickListener(v -> viewModel.loadSymptoms());
            });
            return;
        }

        if (state.isEmpty()) {
            binding.chipGroupSymptoms.setVisibility(View.GONE);
            binding.btnConfirmSymptoms.setEnabled(false);
            showStateView(binding.symptomStateContainer, R.layout.layout_empty, null);
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.chipGroupSymptoms.setVisibility(View.VISIBLE);
            binding.btnConfirmSymptoms.setEnabled(true);
            bindSymptomChips(state.getData());
        }
    }

    private void syncChipSelection(Set<String> selectedIds) {
        syncingChipSelection = true;
        for (int i = 0; i < binding.chipGroupSymptoms.getChildCount(); i++) {
            View child = binding.chipGroupSymptoms.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                Object tag = chip.getTag();
                if (tag instanceof String) {
                    chip.setChecked(selectedIds != null && selectedIds.contains((String) tag));
                }
            }
        }
        syncingChipSelection = false;
    }

    private void bindSymptomChips(List<Symptom> symptoms) {
        binding.chipGroupSymptoms.removeAllViews();
        Set<String> selectedIds = viewModel.getSelectedSymptomIds().getValue();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Symptom symptom : symptoms) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_symptom_chip, binding.chipGroupSymptoms, false);
            chip.setText(symptom.getName());
            chip.setTag(symptom.getId());
            chip.setChecked(selectedIds != null && selectedIds.contains(symptom.getId()));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (syncingChipSelection) {
                    return;
                }
                viewModel.toggleSymptomSelection(symptom.getId(), isChecked);
            });
            binding.chipGroupSymptoms.addView(chip);
        }
    }

    private void showStateView(ViewGroup container, int layoutRes, StateViewSetup setup) {
        container.removeAllViews();
        container.setVisibility(View.VISIBLE);
        View stateView = getLayoutInflater().inflate(layoutRes, container, false);
        container.addView(stateView);
        if (setup != null) {
            setup.setup(stateView);
        }
    }

    private void hideStateView(ViewGroup container) {
        container.removeAllViews();
        container.setVisibility(View.GONE);
    }

    private interface StateViewSetup {
        void setup(View stateView);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
