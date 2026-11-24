package com.animalloo.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.animalloo.data.model.User;
import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.PetProfileInput;

/**
 * Persists the backend session (JWT + signed-in user) in SharedPreferences.
 */
public final class SessionStore {

    private static final String PREFS_NAME = "animalloo_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_PET_NAME = "pet_name";
    private static final String KEY_PET_BREED = "pet_breed";
    private static final String KEY_PET_AGE = "pet_age";
    private static final String KEY_PET_SPECIES = "pet_species";
    private static final String KEY_PET_GENDER = "pet_gender";
    private static final String KEY_PET_NEUTERED = "pet_neutered";
    private static final String KEY_PET_WEIGHT = "pet_weight";
    private static final String KEY_PET_BIRTH = "pet_birth";
    private static final String KEY_PET_ADOPTION = "pet_adoption";
    private static final String KEY_PET_PHOTO = "pet_photo";

    private final SharedPreferences prefs;

    public SessionStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(String token, User user) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, user.getId())
                .putString(KEY_USER_NAME, user.getDisplayName())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .apply();
    }

    @Nullable
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    @Nullable
    public User getUser() {
        if (getToken() == null) {
            return null;
        }
        return new User(
                prefs.getString(KEY_USER_ID, ""),
                prefs.getString(KEY_USER_NAME, ""),
                prefs.getString(KEY_USER_EMAIL, ""));
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public void savePet(PetProfileInput pet, @Nullable String photoPath) {
        SharedPreferences.Editor editor = prefs.edit()
                .putString(KEY_PET_NAME, pet.name).putString(KEY_PET_BREED, pet.breed)
                .putString(KEY_PET_AGE, pet.age).putString(KEY_PET_SPECIES, pet.species)
                .putString(KEY_PET_GENDER, pet.gender).putString(KEY_PET_NEUTERED, pet.neutered)
                .putString(KEY_PET_BIRTH, pet.birthDate).putString(KEY_PET_ADOPTION, pet.adoptionDate);
        if (pet.weightKg == null) editor.remove(KEY_PET_WEIGHT); else editor.putString(KEY_PET_WEIGHT, String.valueOf(pet.weightKg));
        if (photoPath != null) editor.putString(KEY_PET_PHOTO, photoPath);
        editor.apply();
    }

    @Nullable
    public HomeProfile getCachedPetProfile(String fallbackName) {
        String name = prefs.getString(KEY_PET_NAME, null);
        if (name == null || name.isEmpty()) return null;
        String weight = prefs.getString(KEY_PET_WEIGHT, null);
        Double weightKg = weight == null ? null : Double.valueOf(weight);
        return new HomeProfile(prefs.getString(KEY_USER_NAME, fallbackName), null, 0, name,
                prefs.getString(KEY_PET_BREED, null), prefs.getString(KEY_PET_AGE, null),
                prefs.getString(KEY_PET_SPECIES, null), prefs.getString(KEY_PET_GENDER, null),
                prefs.getString(KEY_PET_NEUTERED, null), weightKg, prefs.getString(KEY_PET_BIRTH, null),
                prefs.getString(KEY_PET_ADOPTION, null), prefs.getString(KEY_PET_PHOTO, null));
    }
}
