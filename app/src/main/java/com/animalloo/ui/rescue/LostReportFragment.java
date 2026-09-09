package com.animalloo.ui.rescue;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.animalloo.R;
import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.databinding.FragmentLostReportBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.util.ImageFileHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class LostReportFragment extends BaseFragment {

    private FragmentLostReportBinding binding;
    private LostReportViewModel viewModel;
    private RescueHost rescueHost;

    private String selectedPhotoPath;
    private String selectedLostDate;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleSelectedImage);

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof RescueHost) {
            rescueHost = (RescueHost) getParentFragment();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLostReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireParentFragment()).get(LostReportViewModel.class);
        setupSpinners();
        setupDatePicker();
        setupButtons();
        observeViewModel();

        if (savedInstanceState != null) {
            selectedPhotoPath = savedInstanceState.getString("photo_path");
            selectedLostDate = savedInstanceState.getString("lost_date");
            binding.etAnimalName.setText(savedInstanceState.getString("animal_name", ""));
            binding.etRegion.setText(savedInstanceState.getString("region", ""));
            binding.etFeatures.setText(savedInstanceState.getString("features", ""));
            binding.etContact.setText(savedInstanceState.getString("contact", ""));
            binding.spinnerSpecies.setSelection(savedInstanceState.getInt("species_index", 0));
            binding.spinnerBreed.setSelection(savedInstanceState.getInt("breed_index", 0));
            binding.spinnerGender.setSelection(savedInstanceState.getInt("gender_index", 0));
            if (selectedLostDate != null) {
                binding.etLostDate.setText(selectedLostDate);
            }
            updatePhotoPreview();
        }
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> speciesAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.animal_species, android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSpecies.setAdapter(speciesAdapter);

        ArrayAdapter<CharSequence> breedAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.dog_breeds, android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerBreed.setAdapter(breedAdapter);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.animal_gender, android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGender.setAdapter(genderAdapter);
    }

    private void setupDatePicker() {
        binding.etLostDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedLostDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        binding.etLostDate.setText(selectedLostDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
    }

    private void setupButtons() {
        binding.btnSelectPhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        binding.btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    private void handleSelectedImage(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            if (selectedPhotoPath != null) {
                ImageFileHelper.deleteCachedFile(selectedPhotoPath);
            }
            File cachedFile = ImageFileHelper.copyUriToCache(requireContext(), uri);
            selectedPhotoPath = cachedFile.getAbsolutePath();
            updatePhotoPreview();
        } catch (IOException exception) {
            Snackbar.make(binding.getRoot(), exception.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void updatePhotoPreview() {
        if (selectedPhotoPath == null || selectedPhotoPath.isEmpty()) {
            binding.ivPhotoPreview.setImageResource(R.drawable.img_placeholder);
            return;
        }
        Glide.with(this)
                .load(new File(selectedPhotoPath))
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .into(binding.ivPhotoPreview);
    }

    private void submitReport() {
        String breed = binding.spinnerBreed.getSelectedItem().toString();
        if ("전체".equals(breed)) {
            breed = "믹스견";
        }

        LostAnimalReport report = new LostAnimalReport(
                null,
                binding.etAnimalName.getText() != null ? binding.etAnimalName.getText().toString().trim() : "",
                binding.spinnerSpecies.getSelectedItem().toString(),
                breed,
                binding.spinnerGender.getSelectedItem().toString(),
                binding.etRegion.getText() != null ? binding.etRegion.getText().toString().trim() : "",
                selectedLostDate != null ? selectedLostDate : "",
                binding.etFeatures.getText() != null ? binding.etFeatures.getText().toString().trim() : "",
                binding.etContact.getText() != null ? binding.etContact.getText().toString().trim() : "",
                selectedPhotoPath != null ? selectedPhotoPath : "",
                System.currentTimeMillis()
        );

        viewModel.submitReport(report);
    }

    private void observeViewModel() {
        viewModel.getValidationError().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.getSubmitState().observe(getViewLifecycleOwner(), state -> {
            if (state != null && state.isLoading()) {
                binding.btnSubmitReport.setEnabled(false);
                binding.btnSubmitReport.setText(R.string.lost_report_submitting);
            } else if (binding != null) {
                binding.btnSubmitReport.setEnabled(true);
                binding.btnSubmitReport.setText(R.string.lost_report_submit);
            }
        });

        viewModel.getMatchResultsState().observe(getViewLifecycleOwner(), state -> {
            if (state != null && (state.isSuccess() || state.isEmpty() || state.isError())) {
                if (rescueHost != null && (state.isSuccess() || state.isEmpty())) {
                    rescueHost.showMatchingResults();
                } else if (state.isError()) {
                    Snackbar.make(binding.getRoot(), state.getErrorMessage(), Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("photo_path", selectedPhotoPath);
        outState.putString("lost_date", selectedLostDate);
        outState.putString("animal_name", binding.etAnimalName.getText() != null
                ? binding.etAnimalName.getText().toString() : "");
        outState.putString("region", binding.etRegion.getText() != null
                ? binding.etRegion.getText().toString() : "");
        outState.putString("features", binding.etFeatures.getText() != null
                ? binding.etFeatures.getText().toString() : "");
        outState.putString("contact", binding.etContact.getText() != null
                ? binding.etContact.getText().toString() : "");
        outState.putInt("species_index", binding.spinnerSpecies.getSelectedItemPosition());
        outState.putInt("breed_index", binding.spinnerBreed.getSelectedItemPosition());
        outState.putInt("gender_index", binding.spinnerGender.getSelectedItemPosition());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
