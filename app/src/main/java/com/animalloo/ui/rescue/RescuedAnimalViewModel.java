package com.animalloo.ui.rescue;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.RescuedAnimal;
import com.animalloo.data.model.UiState;
import com.animalloo.data.repository.AnimalRepository;
import com.animalloo.data.repository.FavoriteRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RescuedAnimalViewModel extends ViewModel {

    private final AnimalRepository animalRepository;
    private final FavoriteRepository favoriteRepository;

    private final MutableLiveData<UiState<List<RescuedAnimal>>> animalsState = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> favoriteAnimalIds = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<String> selectedBreed = new MutableLiveData<>("전체");
    private final MutableLiveData<String> selectedRegion = new MutableLiveData<>("전체");
    private final MutableLiveData<String> selectedGender = new MutableLiveData<>("전체");

    private int requestGeneration;

    public RescuedAnimalViewModel() {
        RepositoryProvider provider = RepositoryProvider.getInstance();
        animalRepository = provider.getAnimalRepository();
        favoriteRepository = provider.getFavoriteRepository();
    }

    public LiveData<UiState<List<RescuedAnimal>>> getAnimalsState() {
        return animalsState;
    }

    public LiveData<Set<String>> getFavoriteAnimalIds() {
        return favoriteAnimalIds;
    }

    public LiveData<String> getSelectedBreed() {
        return selectedBreed;
    }

    public LiveData<String> getSelectedRegion() {
        return selectedRegion;
    }

    public LiveData<String> getSelectedGender() {
        return selectedGender;
    }

    public void loadAnimals() {
        animalsState.setValue(UiState.loading());
        int generation = ++requestGeneration;

        animalRepository.getFilteredRescuedAnimals(
                selectedBreed.getValue(),
                selectedRegion.getValue(),
                selectedGender.getValue(),
                new RepositoryCallback<List<RescuedAnimal>>() {
                    @Override
                    public void onSuccess(List<RescuedAnimal> data) {
                        if (generation != requestGeneration) {
                            return;
                        }
                        if (data == null || data.isEmpty()) {
                            animalsState.setValue(UiState.empty());
                        } else {
                            animalsState.setValue(UiState.success(data));
                        }
                        refreshFavoriteAnimalIds(data);
                    }

                    @Override
                    public void onError(String message) {
                        if (generation != requestGeneration) {
                            return;
                        }
                        animalsState.setValue(UiState.error(message));
                    }
                });
    }

    public void setBreedFilter(String breed) {
        if (Objects.equals(selectedBreed.getValue(), breed)) {
            return;
        }
        selectedBreed.setValue(breed);
        loadAnimals();
    }

    public void setRegionFilter(String region) {
        if (Objects.equals(selectedRegion.getValue(), region)) {
            return;
        }
        selectedRegion.setValue(region);
        loadAnimals();
    }

    public void setGenderFilter(String gender) {
        if (Objects.equals(selectedGender.getValue(), gender)) {
            return;
        }
        selectedGender.setValue(gender);
        loadAnimals();
    }

    public void resetFilters() {
        selectedBreed.setValue("전체");
        selectedRegion.setValue("전체");
        selectedGender.setValue("전체");
        loadAnimals();
    }

    public boolean isAnimalFavorite(String animalId) {
        return favoriteRepository.isFavorite(FavoriteRepository.keyForRescuedAnimal(animalId));
    }

    public void toggleAnimalFavorite(String animalId) {
        String key = FavoriteRepository.keyForRescuedAnimal(animalId);
        favoriteRepository.setFavorite(key, !favoriteRepository.isFavorite(key));
        UiState<List<RescuedAnimal>> current = animalsState.getValue();
        refreshFavoriteAnimalIds(current != null ? current.getData() : null);
    }

    private void refreshFavoriteAnimalIds(List<RescuedAnimal> animals) {
        Set<String> ids = new HashSet<>();
        if (animals != null) {
            for (RescuedAnimal animal : animals) {
                if (favoriteRepository.isFavorite(
                        FavoriteRepository.keyForRescuedAnimal(animal.getId()))) {
                    ids.add(animal.getId());
                }
            }
        }
        favoriteAnimalIds.setValue(ids);
    }
}
