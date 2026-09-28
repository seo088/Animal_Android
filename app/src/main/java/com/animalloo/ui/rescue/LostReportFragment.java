package com.animalloo.ui.rescue;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
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
import com.google.android.material.textfield.TextInputLayout;

import androidx.core.widget.NestedScrollView;

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
        setupDropdowns();
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
            String species = savedInstanceState.getString("species", "");
            if (!TextUtils.isEmpty(species)) {
                binding.actvSpecies.setText(species, false);
                updateBreedDropdown(species, savedInstanceState.getString("breed", null));
            }
            String gender = savedInstanceState.getString("gender", "");
            if (!TextUtils.isEmpty(gender)) {
                binding.actvGender.setText(gender, false);
            }
            if (selectedLostDate != null) {
                binding.etLostDate.setText(selectedLostDate);
            }
            updatePhotoPreview();
        }
    }

    private void setupDropdowns() {
        String[] species = getResources().getStringArray(R.array.animal_species);
        ArrayAdapter<String> speciesAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, species);
        binding.actvSpecies.setAdapter(speciesAdapter);
        binding.actvSpecies.setText(species[0], false);
        binding.actvSpecies.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            updateBreedDropdown(selected, null);
            viewModel.clearValidationError();
        });

        String[] genders = getResources().getStringArray(R.array.animal_gender);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, genders);
        binding.actvGender.setAdapter(genderAdapter);
        binding.actvGender.setText(genders[0], false);

        updateBreedDropdown(SPECIES_DOG, null);
    }

    private void updateBreedDropdown(String species, @Nullable String breedText) {
        if (currentBreedSpecies != null
                && species.equals(currentBreedSpecies)
                && breedText != null
                && !breedText.isEmpty()) {
            binding.actvBreed.setText(breedText, false);
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

        String[] breeds = getResources().getStringArray(arrayRes);
        ArrayAdapter<String> breedAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, breeds);
        binding.actvBreed.setAdapter(breedAdapter);
        if (!TextUtils.isEmpty(breedText)) {
            binding.actvBreed.setText(breedText, false);
        } else if (breeds.length > 0) {
            binding.actvBreed.setText(breeds[0], false);
        }
    }

    private void setupDatePicker() {
        View.OnClickListener dateClickListener = v -> showDatePicker();
        binding.etLostDate.setOnClickListener(dateClickListener);
        binding.tilLostDate.setEndIconOnClickListener(dateClickListener);
    }

    private void showDatePicker() {
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
    }

    private void setupButtons() {
        View.OnClickListener pickPhotoListener = v -> pickImageLauncher.launch("image/*");
        binding.btnSelectPhoto.setOnClickListener(pickPhotoListener);
        binding.photoPreviewOverlay.setOnClickListener(pickPhotoListener);
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
        boolean hasPhoto = selectedPhotoPath != null
                && !selectedPhotoPath.isEmpty()
                && new File(selectedPhotoPath).exists();
        binding.tvPhotoHint.setVisibility(hasPhoto ? View.GONE : View.VISIBLE);

        if (!hasPhoto) {
            binding.ivPhotoPreview.setImageResource(R.drawable.img_placeholder);
            return;
        }
        Glide.with(this)
                .load(new File(selectedPhotoPath))
                .placeholder(R.drawable.img_placeholder)
                .error(R.drawable.img_placeholder)
                .into(binding.ivPhotoPreview);
    }

    public void resetFormFields() {
        if (binding == null) {
            return;
        }

        String[] species = getResources().getStringArray(R.array.animal_species);
        String[] genders = getResources().getStringArray(R.array.animal_gender);

        binding.etAnimalName.setText("");
        binding.actvSpecies.setText(species.length > 0 ? species[0] : "", false);
        binding.actvGender.setText(genders.length > 0 ? genders[0] : "", false);
        binding.etRegion.setText("");
        binding.etFeatures.setText("");
        binding.etContact.setText("");
        binding.etLostDate.setText("");

        if (selectedPhotoPath != null && !isPersistedReportPhoto(selectedPhotoPath)) {
            ImageFileHelper.deleteCachedFile(selectedPhotoPath);
        }
        selectedPhotoPath = null;
        selectedLostDate = null;
        matchingScreenShown = false;
        updateBreedDropdown(SPECIES_DOG, null);
        updatePhotoPreview();
        viewModel.clearValidationError();
    }

    private void submitReport() {
        if (viewModel.isSubmitting()) {
            return;
        }

        LostAnimalReport report = new LostAnimalReport(
                null,
                binding.etAnimalName.getText() != null ? binding.etAnimalName.getText().toString().trim() : "",
                binding.actvSpecies.getText() != null ? binding.actvSpecies.getText().toString().trim() : "",
                binding.actvBreed.getText() != null ? binding.actvBreed.getText().toString().trim() : "",
                binding.actvGender.getText() != null ? binding.actvGender.getText().toString().trim() : "",
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
        viewModel.getValidationError().observe(getViewLifecycleOwner(), error -> {
            if (error == null) {
                clearFieldErrors();
                return;
            }
            showValidationError(error);
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

    private void showValidationError(LostReportViewModel.FieldValidationError error) {
        clearFieldErrors();
        View target = binding.getRoot().findViewById(error.getFocusViewId());
        if (target != null) {
            TextInputLayout inputLayout = findTextInputLayout(target);
            if (inputLayout != null) {
                inputLayout.setError(error.getMessage());
            }
            scrollToView(target);
            target.post(() -> {
                target.requestFocus();
                target.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            });
        }
        Snackbar.make(binding.getRoot(), error.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    private void clearFieldErrors() {
        binding.tilSpecies.setError(null);
        binding.tilBreed.setError(null);
        binding.tilGender.setError(null);
        binding.tilLostDate.setError(null);
        ViewParent nameParent = binding.etAnimalName.getParent();
        if (nameParent instanceof TextInputLayout) {
            ((TextInputLayout) nameParent).setError(null);
        }
        ViewParent regionParent = binding.etRegion.getParent();
        if (regionParent instanceof TextInputLayout) {
            ((TextInputLayout) regionParent).setError(null);
        }
        ViewParent contactParent = binding.etContact.getParent();
        if (contactParent instanceof TextInputLayout) {
            ((TextInputLayout) contactParent).setError(null);
        }
    }

    @Nullable
    private TextInputLayout findTextInputLayout(View view) {
        ViewParent parent = view.getParent();
        while (parent instanceof View) {
            if (parent instanceof TextInputLayout) {
                return (TextInputLayout) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private void scrollToView(View target) {
        View root = binding.getRoot();
        if (!(root instanceof NestedScrollView)) {
            return;
        }
        NestedScrollView scrollView = (NestedScrollView) root;
        scrollView.post(() -> {
            int scrollY = 0;
            View current = target;
            while (current != null && current != scrollView) {
                scrollY += current.getTop();
                ViewParent parent = current.getParent();
                current = parent instanceof View ? (View) parent : null;
            }
            scrollView.smoothScrollTo(0, scrollY);
        });
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
        outState.putString("species", binding.actvSpecies.getText() != null
                ? binding.actvSpecies.getText().toString() : "");
        outState.putString("breed", binding.actvBreed.getText() != null
                ? binding.actvBreed.getText().toString() : "");
        outState.putString("gender", binding.actvGender.getText() != null
                ? binding.actvGender.getText().toString() : "");
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
