package com.animalloo.data.repository;

import com.animalloo.data.model.RescuedAnimal;

import java.util.List;

public interface AnimalRepository {

    void getAllRescuedAnimals(RepositoryCallback<List<RescuedAnimal>> callback);

    void getFilteredRescuedAnimals(String breed, String region, String gender,
                                   RepositoryCallback<List<RescuedAnimal>> callback);

    void getRescuedAnimalById(String id, RepositoryCallback<RescuedAnimal> callback);
}
