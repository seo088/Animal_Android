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
import com.animalloo.data.model.DetailType;
import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.data.model.UiState;
import com.animalloo.databinding.FragmentLostReportBinding;
import com.animalloo.ui.common.BaseFragment;
import com.animalloo.ui.detail.DetailNavigator;
import com.animalloo.util.ImageFileHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class LostReportFragment extends BaseFragment {

    private static final String SPECIES_DOG = "개";
    private static final String SPECIES_CAT = "고양이";
    private static final String SPECIES_OTHER = "기타";

    private FragmentLostReportBinding binding;
    private LostReportViewModel viewModel;
    private RescueHost rescueHost;
    private DetailNavigator detailNavigator;

    private String selectedPhotoPath;
    private String selectedLostDate;
    private boolean matchingScreenShown;
    private boolean suppressSpeciesCallback;
    private String currentBreedSpecies;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleSelectedImage);

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof RescueHost) {
            rescueHost = (RescueHost) getParentFragment();
        }
        if (context instanceof DetailNavigator) {
            detailNavigator = (DetailNavigator) context;
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
            matchingScreenShown = savedInstanceState.getBoolean("matching_screen_shown", false);
            binding.etAnimalName.setText(savedInstanceState.getString("animal_name", ""));
            binding.etRegion.setText(savedInstanceState.getString("region", ""));
            binding.etFeatures.setText(savedInstanceState.getString("features", ""));
            binding.etContact.setText(savedInstanceState.getString("contact", ""));
            suppressSpeciesCallback = true;
            int speciesIndex = savedInstanceState.getInt("species_index", 0);
            int breedIndex = savedInstanceState.getInt("breed_index", 0);
            binding.spinnerSpecies.setSelection(speciesIndex);
            updateBreedSpinner(binding.spinnerSpecies.getSelectedItem().toString(), breedIndex);
            suppressSpeciesCallback = false;
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
        binding.spinnerSpecies.setOnItemSelectedListener(new SimpleItemSelectedListener(value -> {
            if (suppressSpeciesCallback) {
                return;
            }
            updateBreedSpinner(value, null);
            viewModel.clearValidationError();
        }));

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.animal_gender, android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGender.setAdapter(genderAdapter);

        updateBreedSpinner(SPECIES_DOG, null);
    }

    private void updateBreedSpinner(String species, @Nullable Integer breedIndex) {
        if (currentBreedSpecies != null && species.equals(currentBreedSpecies)) {
            if (breedIndex != null
                    && binding.spinnerBreed.getAdapter() != null
                    && breedIndex >= 0
                    && breedIndex < binding.spinnerBreed.getAdapter().getCount()) {
                binding.spinnerBreed.setSelection(breedIndex, false);
            }
            return;
        }

        currentBreedSpecies = species;
        int arrayRes;
        if (SPECIES_CAT.equals(species)) {
            arrayRes = R.array.cat_breeds;
        } else if (SPECIES_OTHER.equals(species)) {
            arrayRes = R.array.other_breeds;
        } else {
            arrayRes = R.array.dog_breeds;
        }

        ArrayAdapter<CharSequence> breedAdapter = ArrayAdapter.createFromResource(
                requireContext(), arrayRes, android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerBreed.setAdapter(breedAdapter);
        if (breedIndex != null && breedIndex >= 0 && breedIndex < breedAdapter.getCount()) {
            binding.spinnerBreed.setSelection(breedIndex, false);
        }
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
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
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
            File cachedFile = ImageFileHelper.copyUriToCache(requireContext(), uri);
            if (selectedPhotoPath != null && !isPersistedReportPhoto(selectedPhotoPath)) {
                ImageFileHelper.deleteCachedFile(selectedPhotoPath);
            }
            selectedPhotoPath = cachedFile.getAbsolutePath();
            updatePhotoPreview();
        } catch (IOException exception) {
            Snackbar.make(binding.getRoot(), R.string.lost_report_photo_copy_failed, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void updatePhotoPreview() {
        if (selectedPhotoPath == null || selectedPhotoPath.isEmpty()
                || !new File(selectedPhotoPath).exists()) {
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
        if (viewModel.isSubmitting()) {
            return;
        }

        LostAnimalReport report = new LostAnimalReport(
                null,
                binding.etAnimalName.getText() != null ? binding.etAnimalName.getText().toString().trim() : "",
                binding.spinnerSpecies.getSelectedItem().toString(),
                binding.spinnerBreed.getSelectedItem().toString(),
                binding.spinnerGender.getSelectedItem().toString(),
                binding.etRegion.getText() != null ? binding.etRegion.getText().toString().trim() : "",
                selectedLostDate != null ? selectedLostDate : "",
                binding.etFeatures.getText() != null ? binding.etFeatures.getText().toString().trim() : "",
                binding.etContact.getText() != null ? binding.etContact.getText().toString().trim() : "",
                selectedPhotoPath != null ? selectedPhotoPath : "",
                System.currentTimeMillis()
        );

        matchingScreenShown = false;
        viewModel.submitReport(report);
    }

    private void observeViewModel() {
        viewModel.getValidationError().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getSubmitState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                binding.btnSubmitReport.setEnabled(true);
                binding.btnSubmitReport.setText(R.string.lost_report_submit);
                return;
            }

            if (state.isLoading()) {
                binding.btnSubmitReport.setEnabled(false);
                binding.btnSubmitReport.setText(R.string.lost_report_submitting);
                return;
            }

            binding.btnSubmitReport.setEnabled(true);
            binding.btnSubmitReport.setText(R.string.lost_report_submit);

            if (state.isError()) {
                Snackbar.make(binding.getRoot(), state.getErrorMessage(), Snackbar.LENGTH_LONG).show();
            } else if (state.isSuccess()) {
                openMatchingScreenIfNeeded();
                Snackbar.make(binding.getRoot(), R.string.lost_report_submit_success, Snackbar.LENGTH_LONG)
                        .setAction(R.string.lost_report_view_detail, v -> {
                            if (detailNavigator != null && state.getData() != null) {
                                detailNavigator.navigateToDetail(
                                        DetailType.LOST_ANIMAL, state.getData().getId());
                            }
                        })
                        .show();
            }
        });

        viewModel.getMatchResultsState().observe(getViewLifecycleOwner(), this::handleMatchState);
    }

    private void handleMatchState(UiState<List<com.animalloo.data.model.MatchResult>> state) {
        if (state == null || rescueHost == null) {
            return;
        }

        UiState<LostAnimalReport> submitState = viewModel.getSubmitState().getValue();
        if (submitState == null || !submitState.isSuccess()) {
            return;
        }

        openMatchingScreenIfNeeded();
    }

    private boolean isPersistedReportPhoto(String photoPath) {
        LostAnimalReport lastSubmittedReport = viewModel.getLastSubmittedReport();
        return lastSubmittedReport != null
                && photoPath != null
                && photoPath.equals(lastSubmittedReport.getPhotoPath());
    }

    private void openMatchingScreenIfNeeded() {
        if (!matchingScreenShown) {
            matchingScreenShown = true;
            rescueHost.showMatchingResults();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getParentFragment() != null
                && getParentFragment().getChildFragmentManager().getBackStackEntryCount() == 0) {
            matchingScreenShown = false;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("photo_path", selectedPhotoPath);
        outState.putString("lost_date", selectedLostDate);
        outState.putBoolean("matching_screen_shown", matchingScreenShown);
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

    private static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {

        interface SelectionCallback {
            void onSelected(String value);
        }

        private final SelectionCallback callback;

        SimpleItemSelectedListener(SelectionCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            Object item = parent.getItemAtPosition(position);
            if (item != null) {
                callback.onSelected(item.toString());
            }
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
            // no-op
        }
    }
}
