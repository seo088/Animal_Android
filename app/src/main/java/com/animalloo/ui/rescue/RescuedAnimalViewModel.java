package com.animalloo.ui.rescue;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.RescuedAnimal;
import com.animalloo.data.model.UiState;
import com.animalloo.data.repository.AnimalRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

import java.util.List;

public class RescuedAnimalViewModel extends ViewModel {

    private final AnimalRepository animalRepository;

    private final MutableLiveData<UiState<List<RescuedAnimal>>> animalsState = new MutableLiveData<>();
    private final MutableLiveData<String> selectedBreed = new MutableLiveData<>("전체");
    private final MutableLiveData<String> selectedRegion = new MutableLiveData<>("전체");
    private final MutableLiveData<String> selectedGender = new MutableLiveData<>("전체");

    public RescuedAnimalViewModel() {
        animalRepository = RepositoryProvider.getInstance().getAnimalRepository();
    }

    public LiveData<UiState<List<RescuedAnimal>>> getAnimalsState() {
        return animalsState;
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
        animalRepository.getFilteredRescuedAnimals(
                selectedBreed.getValue(),
                selectedRegion.getValue(),
                selectedGender.getValue(),
                new RepositoryCallback<List<RescuedAnimal>>() {
                    @Override
                    public void onSuccess(List<RescuedAnimal> data) {
                        if (data == null || data.isEmpty()) {
                            animalsState.setValue(UiState.empty());
                        } else {
                            animalsState.setValue(UiState.success(data));
                        }
                    }

                    @Override
                    public void onError(String message) {
                        animalsState.setValue(UiState.error(message));
                    }
                });
    }

    public void setBreedFilter(String breed) {
        selectedBreed.setValue(breed);
        loadAnimals();
    }

    public void setRegionFilter(String region) {
        selectedRegion.setValue(region);
        loadAnimals();
    }

    public void setGenderFilter(String gender) {
        selectedGender.setValue(gender);
        loadAnimals();
    }
}
