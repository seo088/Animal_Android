package com.animalloo.ui.diagnosis;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.animalloo.R;
import com.animalloo.adapter.DiagnosisResultAdapter;
import com.animalloo.adapter.HospitalAdapter;
import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.Hospital;
import com.animalloo.data.model.Symptom;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentDiagnosisBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.ui.detail.DetailNavigator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Set;

public class DiagnosisFragment extends BaseFragment {

    private FragmentDiagnosisBinding binding;
    private DiagnosisViewModel viewModel;
    private DiagnosisResultAdapter diagnosisResultAdapter;
    private HospitalAdapter hospitalAdapter;
    private DetailNavigator detailNavigator;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDiagnosisBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DiagnosisViewModel.class);
        setupRecyclerViews();
        setupButtons();
        observeViewModel();

        if (savedInstanceState == null) {
            viewModel.loadSymptoms();
        }
    }

    private void setupRecyclerViews() {
        diagnosisResultAdapter = new DiagnosisResultAdapter();
        binding.rvDiagnosisResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDiagnosisResults.setAdapter(diagnosisResultAdapter);

        hospitalAdapter = new HospitalAdapter();
        binding.rvHospitals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHospitals.setAdapter(hospitalAdapter);
        hospitalAdapter.setOnHospitalClickListener(hospital ->
                detailNavigator.navigateToDetail(DetailType.HOSPITAL, hospital.getId()));
    }

    private void setupButtons() {
        binding.btnDiagnose.setOnClickListener(v -> viewModel.diagnoseSelectedSymptoms());
        binding.btnFindHospitals.setOnClickListener(v -> viewModel.loadNearbyHospitals());
        binding.btnRetryDiagnosis.setOnClickListener(v -> viewModel.resetDiagnosis());
        binding.btnRetryFromHospital.setOnClickListener(v -> viewModel.resetDiagnosis());
    }

    private void observeViewModel() {
        viewModel.getCurrentStep().observe(getViewLifecycleOwner(), this::renderStep);
        viewModel.getSymptomsState().observe(getViewLifecycleOwner(), this::renderSymptomsState);
        viewModel.getDiagnosisState().observe(getViewLifecycleOwner(), this::renderDiagnosisState);
        viewModel.getHospitalsState().observe(getViewLifecycleOwner(), this::renderHospitalsState);
        viewModel.getSelectedSymptomIds().observe(getViewLifecycleOwner(), ids -> {
            // Chip states are updated directly on click.
        });
    }

    private void renderStep(DiagnosisStep step) {
        if (step == null) {
            return;
        }

        binding.layoutStepSymptom.setVisibility(
                step == DiagnosisStep.SYMPTOM ? View.VISIBLE : View.GONE);
        binding.layoutStepResult.setVisibility(
                step == DiagnosisStep.RESULT ? View.VISIBLE : View.GONE);
        binding.layoutStepHospital.setVisibility(
                step == DiagnosisStep.HOSPITAL ? View.VISIBLE : View.GONE);

        binding.tvDisclaimer.setVisibility(
                step == DiagnosisStep.SYMPTOM ? View.GONE : View.VISIBLE);

        updateStepIndicator(step);
    }

    private void updateStepIndicator(DiagnosisStep step) {
        styleStepLabel(binding.tvStepSymptom, step == DiagnosisStep.SYMPTOM);
        styleStepLabel(binding.tvStepResult, step == DiagnosisStep.RESULT);
        styleStepLabel(binding.tvStepHospital, step == DiagnosisStep.HOSPITAL);
    }

    private void styleStepLabel(TextView textView, boolean active) {
        int color = ContextCompat.getColor(requireContext(),
                active ? R.color.color_primary : R.color.color_text_secondary);
        textView.setTextColor(color);
        textView.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void renderSymptomsState(UiState<List<Symptom>> state) {
        if (state == null) {
            return;
        }

        if (state.isLoading()) {
            binding.chipGroupSymptoms.setVisibility(View.GONE);
            showStateView(binding.symptomStateContainer, R.layout.layout_loading, null);
            return;
        }

        hideStateView(binding.symptomStateContainer);

        if (state.isError()) {
            binding.chipGroupSymptoms.setVisibility(View.GONE);
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
            showStateView(binding.symptomStateContainer, R.layout.layout_empty, null);
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.chipGroupSymptoms.setVisibility(View.VISIBLE);
            bindSymptomChips(state.getData());
        }
    }

    private void bindSymptomChips(List<Symptom> symptoms) {
        binding.chipGroupSymptoms.removeAllViews();
        Set<String> selectedIds = viewModel.getSelectedSymptomIds().getValue();

        for (Symptom symptom : symptoms) {
            Chip chip = new Chip(requireContext(), null, R.style.Widget_AnimalLoo_Chip);
            chip.setText(symptom.getName());
            chip.setCheckable(true);
            chip.setTag(symptom.getId());
            chip.setChecked(selectedIds != null && selectedIds.contains(symptom.getId()));

            chip.setOnCheckedChangeListener((buttonView, isChecked) ->
                    viewModel.toggleSymptomSelection(symptom.getId(), isChecked));

            binding.chipGroupSymptoms.addView(chip);
        }
    }

    private void renderDiagnosisState(UiState<List<DiagnosisResult>> state) {
        if (state == null) {
            binding.rvDiagnosisResults.setVisibility(View.GONE);
            binding.btnFindHospitals.setVisibility(View.GONE);
            hideStateView(binding.resultStateContainer);
            return;
        }

        if (state.isLoading()) {
            binding.rvDiagnosisResults.setVisibility(View.GONE);
            binding.btnFindHospitals.setVisibility(View.GONE);
            showStateView(binding.resultStateContainer, R.layout.layout_loading, null);
            return;
        }

        hideStateView(binding.resultStateContainer);

        if (state.isError()) {
            binding.rvDiagnosisResults.setVisibility(View.GONE);
            binding.btnFindHospitals.setVisibility(View.GONE);
            showStateView(binding.resultStateContainer, R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setOnClickListener(v -> viewModel.resetDiagnosis());
            });
            return;
        }

        if (state.isEmpty()) {
            binding.rvDiagnosisResults.setVisibility(View.GONE);
            binding.btnFindHospitals.setVisibility(View.GONE);
            showStateView(binding.resultStateContainer, R.layout.layout_empty, null);
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.rvDiagnosisResults.setVisibility(View.VISIBLE);
            binding.btnFindHospitals.setVisibility(View.VISIBLE);
            diagnosisResultAdapter.setItems(state.getData());
        }
    }

    private void renderHospitalsState(UiState<List<Hospital>> state) {
        if (state == null) {
            binding.rvHospitals.setVisibility(View.GONE);
            hideStateView(binding.hospitalStateContainer);
            return;
        }

        if (state.isLoading()) {
            binding.rvHospitals.setVisibility(View.GONE);
            showStateView(binding.hospitalStateContainer, R.layout.layout_loading, null);
            return;
        }

        hideStateView(binding.hospitalStateContainer);

        if (state.isError()) {
            binding.rvHospitals.setVisibility(View.GONE);
            showStateView(binding.hospitalStateContainer, R.layout.layout_error, stateView -> {
                TextView messageView = stateView.findViewById(R.id.tv_error_message);
                MaterialButton retryButton = stateView.findViewById(R.id.btn_retry);
                messageView.setText(state.getErrorMessage());
                retryButton.setOnClickListener(v -> viewModel.loadNearbyHospitals());
            });
            return;
        }

        if (state.isEmpty()) {
            binding.rvHospitals.setVisibility(View.GONE);
            showStateView(binding.hospitalStateContainer, R.layout.layout_empty, null);
            return;
        }

        if (state.isSuccess() && state.getData() != null) {
            binding.rvHospitals.setVisibility(View.VISIBLE);
            hospitalAdapter.setItems(state.getData());
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
