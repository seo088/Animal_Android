package com.animalloo.ui.diagnosis;

import android.text.TextUtils;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.animalloo.R;
import com.animalloo.adapter.DiagnosisChatAdapter;
import com.animalloo.adapter.SymptomSelectionAdapter;
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
import java.util.ArrayList;
import java.util.Set;

public class DiagnosisFragment extends BaseFragment {

    private FragmentDiagnosisBinding binding;
    private DiagnosisViewModel viewModel;
    private DiagnosisChatAdapter chatAdapter;
    private DetailNavigator detailNavigator;
    private SymptomSelectionAdapter symptomSelectionAdapter;
    private List<Symptom> allSymptoms = new ArrayList<>();
    private String selectedSymptomCategory;
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
        binding.ivDiagnosisHome.setOnClickListener(v -> requireActivity().findViewById(R.id.nav_home).performClick());
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
        binding.btnSpeciesDogChat.setOnClickListener(v -> viewModel.selectSpecies("dog"));
        binding.btnSpeciesCatChat.setOnClickListener(v -> viewModel.selectSpecies("cat"));
        symptomSelectionAdapter = new SymptomSelectionAdapter();
        symptomSelectionAdapter.setOnSelectionChangeListener(
                (symptom, selected) -> viewModel.toggleSymptomSelection(symptom.getId(), selected));
        binding.rvSymptomSelection.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSymptomSelection.setAdapter(symptomSelectionAdapter);
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
        viewModel.getSelectedSymptomIds().observe(getViewLifecycleOwner(), this::renderSelectedSymptoms);
        viewModel.getSpeciesSelectionRequired().observe(getViewLifecycleOwner(), required -> {
            boolean showChoice = Boolean.TRUE.equals(required);
            binding.layoutSpeciesChoice.setVisibility(showChoice ? View.VISIBLE : View.GONE);
            binding.rvSymptomSelection.setVisibility(showChoice ? View.GONE : binding.rvSymptomSelection.getVisibility());
            binding.tvSymptomPanelGuide.setVisibility(showChoice ? View.GONE : View.VISIBLE);
            binding.scrollSelectedSymptoms.setVisibility(showChoice ? View.GONE : binding.scrollSelectedSymptoms.getVisibility());
            binding.tvSelectedSymptomCount.setVisibility(showChoice ? View.GONE : binding.tvSelectedSymptomCount.getVisibility());
            binding.btnConfirmSymptoms.setVisibility(showChoice ? View.GONE : View.VISIBLE);
        });
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
            binding.rvSymptomSelection.setVisibility(View.GONE);
            binding.btnConfirmSymptoms.setEnabled(false);
            showStateView(binding.symptomStateContainer, R.layout.layout_loading, null);
            return;
        }

        hideStateView(binding.symptomStateContainer);

        if (state.isError()) {
            binding.rvSymptomSelection.setVisibility(View.GONE);
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
            binding.rvSymptomSelection.setVisibility(View.GONE);
            binding.btnConfirmSymptoms.setEnabled(false);
            showStateView(binding.symptomStateContainer, R.layout.layout_empty, null);
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.rvSymptomSelection.setVisibility(View.VISIBLE);
            binding.btnConfirmSymptoms.setEnabled(true);
            bindSymptomRows(state.getData());
        }
    }

    private void bindSymptomRows(List<Symptom> symptoms) {
        allSymptoms = new ArrayList<>(symptoms);
        buildCategoryChips();
        showSelectedCategory();
    }

    private void buildCategoryChips() {
        List<String> categories = new ArrayList<>();
        String[] order = {"눈", "귀", "피부", "소화", "호흡", "움직임", "행동·기타"};
        for (String category : order) {
            for (Symptom symptom : allSymptoms) {
                if (category.equals(symptom.getCategory())) {
                    categories.add(category);
                    break;
                }
            }
        }
        if (categories.isEmpty()) {
            selectedSymptomCategory = null;
            binding.chipGroupSymptomCategories.removeAllViews();
            binding.scrollSymptomCategories.setVisibility(View.GONE);
            binding.tvSymptomCategoryTitle.setVisibility(View.GONE);
            symptomSelectionAdapter.submit(allSymptoms, viewModel.getSelectedSymptomIds().getValue());
            return;
        }
        if (!categories.contains(selectedSymptomCategory)) {
            selectedSymptomCategory = categories.get(0);
        }
        binding.chipGroupSymptomCategories.removeAllViews();
        for (String category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setId(View.generateViewId());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setEnsureMinTouchTargetSize(true);
            chip.setTextAppearance(R.style.TextAppearance_AnimalLoo_Body);
            boolean selected = category.equals(selectedSymptomCategory);
            applyCategoryChipState(chip, selected);
            chip.setChecked(selected);
            chip.setOnClickListener(v -> {
                selectedSymptomCategory = category;
                for (int index = 0; index < binding.chipGroupSymptomCategories.getChildCount(); index++) {
                    Chip sibling = (Chip) binding.chipGroupSymptomCategories.getChildAt(index);
                    boolean isSelected = sibling == chip;
                    sibling.setChecked(isSelected);
                    applyCategoryChipState(sibling, isSelected);
                }
                showSelectedCategory();
            });
            binding.chipGroupSymptomCategories.addView(chip);
        }
        binding.scrollSymptomCategories.setVisibility(View.VISIBLE);
        binding.tvSymptomCategoryTitle.setVisibility(View.VISIBLE);
    }

    private void applyCategoryChipState(Chip chip, boolean selected) {
        int background = ContextCompat.getColor(requireContext(), selected
                ? R.color.color_primary : R.color.color_surface);
        int text = ContextCompat.getColor(requireContext(), selected
                ? R.color.color_on_primary : R.color.color_text_primary);
        chip.setChipBackgroundColor(ColorStateList.valueOf(background));
        chip.setTextColor(text);
        chip.setChipStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), selected
                ? R.color.color_primary : R.color.color_outline)));
        chip.setChipStrokeWidth(1f);
    }

    private void showSelectedCategory() {
        if (selectedSymptomCategory == null) {
            symptomSelectionAdapter.submit(allSymptoms, viewModel.getSelectedSymptomIds().getValue());
            return;
        }
        List<Symptom> filtered = new ArrayList<>();
        for (Symptom symptom : allSymptoms) {
            if (selectedSymptomCategory.equals(symptom.getCategory())) {
                filtered.add(symptom);
            }
        }
        binding.tvSymptomCategoryTitle.setText(selectedSymptomCategory + "에서 보이는 변화");
        symptomSelectionAdapter.submit(filtered, viewModel.getSelectedSymptomIds().getValue());
    }

    private void renderSelectedSymptoms(Set<String> selectedIds) {
        int count = selectedIds == null ? 0 : selectedIds.size();
        binding.tvSelectedSymptomCount.setText(getString(R.string.diagnosis_selected_count,
                count, viewModel.getMaxSelectedSymptoms()));
        binding.btnConfirmSymptoms.setText(getString(R.string.diagnosis_confirm_selected, count));
        binding.btnConfirmSymptoms.setEnabled(count > 0);
        if (symptomSelectionAdapter != null) {
            symptomSelectionAdapter.updateSelection(selectedIds);
        }

        binding.chipGroupSelectedSymptoms.removeAllViews();
        if (allSymptoms == null || selectedIds == null || selectedIds.isEmpty()) {
            binding.scrollSelectedSymptoms.setVisibility(View.GONE);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Symptom symptom : allSymptoms) {
            if (!selectedIds.contains(symptom.getId())) {
                continue;
            }
            Chip chip = (Chip) inflater.inflate(R.layout.item_symptom_chip,
                    binding.chipGroupSelectedSymptoms, false);
            chip.setText(symptom.getName());
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> viewModel.toggleSymptomSelection(symptom.getId(), false));
            chip.setContentDescription(symptom.getName() + ", 선택 해제");
            binding.chipGroupSelectedSymptoms.addView(chip);
        }
        binding.scrollSelectedSymptoms.setVisibility(
                binding.chipGroupSelectedSymptoms.getChildCount() > 0 ? View.VISIBLE : View.GONE);
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
