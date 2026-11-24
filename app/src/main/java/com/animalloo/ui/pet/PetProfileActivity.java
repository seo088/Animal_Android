package com.animalloo.ui.pet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.animalloo.R;
import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.PetProfileInput;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.databinding.ActivityPetProfileBinding;
import com.animalloo.ui.main.MainActivity;
import com.animalloo.util.ImageFileHelper;
import com.animalloo.util.RepositoryProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;

public class PetProfileActivity extends AppCompatActivity {

    private ActivityPetProfileBinding binding;
    private File photoFile;
    private TextInputEditText petName, petBreed, petAge, petWeight, petBirthDate, petAdoptionDate;
    private TextInputLayout petNameLayout, petWeightLayout;
    private MaterialButtonToggleGroup speciesToggle, genderToggle, neuteredToggle;
    private com.google.android.material.button.MaterialButton birthDateUnknownButton;

    private final ActivityResultLauncher<String> picker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), this::picked);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPetProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarPetProfile.setNavigationOnClickListener(v -> finish());
        binding.btnGoHome.setOnClickListener(v -> goHome());
        binding.btnPetPhoto.setOnClickListener(v -> picker.launch("image/*"));
        binding.btnSavePet.setOnClickListener(v -> save());
        bindFormViews();
        setupBirthDateUnknownButton();
        load();
    }

    private void bindFormViews() {
        View root = binding.getRoot();
        petName = root.findViewById(R.id.et_pet_name);
        petBreed = root.findViewById(R.id.et_pet_breed);
        petAge = root.findViewById(R.id.et_pet_age);
        petWeight = root.findViewById(R.id.et_pet_weight);
        petBirthDate = root.findViewById(R.id.et_pet_birth_date);
        petAdoptionDate = root.findViewById(R.id.et_pet_adoption_date);
        petNameLayout = root.findViewById(R.id.til_pet_name);
        petWeightLayout = root.findViewById(R.id.til_pet_weight);
        speciesToggle = root.findViewById(R.id.toggle_pet_species);
        genderToggle = root.findViewById(R.id.toggle_pet_gender);
        neuteredToggle = root.findViewById(R.id.toggle_pet_neutered);
        birthDateUnknownButton = root.findViewById(R.id.btn_birth_date_unknown);
    }

    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void setupBirthDateUnknownButton() {
        birthDateUnknownButton.setOnClickListener(v -> {
            if (birthDateUnknownButton.isChecked()) {
                petBirthDate.setText("");
            }
        });
        petBirthDate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                if (s.length() > 0 && birthDateUnknownButton.isChecked()) {
                    birthDateUnknownButton.setChecked(false);
                }
            }
        });
    }

    private void load() {
        RepositoryProvider.getInstance().getHomeRepository().getHomeProfile(null,
                new RepositoryCallback<HomeProfile>() {
                    @Override public void onSuccess(HomeProfile profile) {
                        runOnUiThread(() -> bind(profile));
                    }
                    @Override public void onError(String message) { }
                });
    }

    private void bind(HomeProfile profile) {
        if (!profile.hasPet()) {
            return;
        }
        petName.setText(profile.getPetName());
        petBreed.setText(profile.getPetBreed());
        petAge.setText(profile.getPetAge());
        selectSpecies(normalizeSpeciesForSelection(profile.getPetSpecies()));
        selectGender(profile.getPetGender());
        selectNeutered(profile.getPetNeutered());
        if (profile.getPetWeightKg() != null) {
            petWeight.setText(String.valueOf(profile.getPetWeightKg()));
        }
        petBirthDate.setText(profile.getPetBirthDate());
        petAdoptionDate.setText(profile.getPetAdoptionDate());
        if (!TextUtils.isEmpty(profile.getPetPhotoUrl())) {
            Glide.with(this).load(profile.getPetPhotoUrl()).into(binding.ivPetPhoto);
        }
    }

    private void selectGender(String value) {
        selectToggle(genderToggle, value,
                "수컷", R.id.btn_gender_male,
                "암컷", R.id.btn_gender_female,
                "모름", R.id.btn_gender_unknown);
    }

    private void selectSpecies(String value) {
        if ("개".equals(value)) {
            speciesToggle.check(R.id.btn_species_dog);
        } else if ("고양이".equals(value)) {
            speciesToggle.check(R.id.btn_species_cat);
        } else {
            speciesToggle.clearChecked();
        }
    }

    @Nullable
    private String selectedSpecies() {
        int selectedId = speciesToggle.getCheckedButtonId();
        if (selectedId == R.id.btn_species_dog) return "개";
        if (selectedId == R.id.btn_species_cat) return "고양이";
        return null;
    }

    private void selectNeutered(String value) {
        selectToggle(neuteredToggle, value,
                "예", R.id.btn_neutered_yes,
                "아니요", R.id.btn_neutered_no,
                "모름", R.id.btn_neutered_unknown);
    }

    private static void selectToggle(MaterialButtonToggleGroup group, String value,
                                     String first, int firstId, String second, int secondId,
                                     String third, int thirdId) {
        if (first.equals(value)) {
            group.check(firstId);
        } else if (second.equals(value)) {
            group.check(secondId);
        } else if (third.equals(value)) {
            group.check(thirdId);
        }
    }

    private String selectedGender() {
        return selectedToggle(genderToggle,
                R.id.btn_gender_male, "수컷", R.id.btn_gender_female, "암컷",
                R.id.btn_gender_unknown, "모름");
    }

    private String selectedNeutered() {
        return selectedToggle(neuteredToggle,
                R.id.btn_neutered_yes, "예", R.id.btn_neutered_no, "아니요",
                R.id.btn_neutered_unknown, "모름");
    }

    private static String selectedToggle(MaterialButtonToggleGroup group,
                                         int firstId, String first, int secondId, String second,
                                         int thirdId, String third) {
        int selectedId = group.getCheckedButtonId();
        if (selectedId == firstId) return first;
        if (selectedId == secondId) return second;
        if (selectedId == thirdId) return third;
        return null;
    }

    private void picked(Uri uri) {
        if (uri == null) return;
        try {
            photoFile = ImageFileHelper.copyUriToCache(this, uri);
            binding.ivPetPhoto.setPadding(0, 0, 0, 0);
            binding.ivPetPhoto.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(photoFile).into(binding.ivPetPhoto);
            binding.ivPetPhoto.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            Snackbar.make(binding.getRoot(), R.string.lost_report_photo_copy_failed,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void save() {
        String name = value(petName);
        if (name.isEmpty()) {
            petNameLayout.setError(getString(R.string.pet_registration_name_required));
            return;
        }
        String species = selectedSpecies();
        if (species == null) {
            Snackbar.make(binding.getRoot(), "종은 개 또는 고양이 중에서 선택해 주세요.", Snackbar.LENGTH_LONG).show();
            return;
        }
        Double weight = null;
        try {
            if (!value(petWeight).isEmpty()) {
                weight = Double.valueOf(value(petWeight));
            }
        } catch (NumberFormatException e) {
            petWeightLayout.setError(getString(R.string.pet_weight_invalid));
            return;
        }
        binding.btnSavePet.setEnabled(false);
        PetProfileInput pet = new PetProfileInput(name, emptyToNull(value(petBreed)),
                emptyToNull(value(petAge)), species,
                selectedGender(), selectedNeutered(), weight,
                emptyToNull(value(petBirthDate)),
                emptyToNull(value(petAdoptionDate)));
        RepositoryProvider.getInstance().getHomeRepository().upsertPet(pet, photoFile,
                new RepositoryCallback<HomeProfile>() {
                    @Override public void onSuccess(HomeProfile profile) {
                        runOnUiThread(() -> finish());
                    }
                    @Override public void onError(String message) {
                        runOnUiThread(() -> {
                            binding.btnSavePet.setEnabled(true);
                            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private static String value(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private static String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    private static String normalizeSpeciesForSelection(String species) {
        if (species == null) return "";
        String normalized = species.trim().toLowerCase();
        if (normalized.equals("개") || normalized.equals("dog") || normalized.contains("강아지")
                || normalized.contains("견") || normalized.equals("말티즈") || normalized.equals("푸들")
                || normalized.contains("리트리버")) return "개";
        if (normalized.equals("고양이") || normalized.equals("cat") || normalized.contains("냥")
                || normalized.equals("코리안숏헤어") || normalized.equals("러시안블루")
                || normalized.equals("페르시안")) return "고양이";
        return "";
    }
}
