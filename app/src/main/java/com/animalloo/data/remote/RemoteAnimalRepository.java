package com.animalloo.data.remote;

import com.animalloo.data.model.RescuedAnimal;
import com.animalloo.data.repository.AnimalRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.util.List;

/** GET /animals, /animals/{id} — 농림축산식품부 구조동물. "전체" filters are ignored by the server. */
public class RemoteAnimalRepository implements AnimalRepository {

    private static final int PAGE_SIZE = 200;

    private final AnimalLooApi api;

    public RemoteAnimalRepository(AnimalLooApi api) {
        this.api = api;
    }

    @Override
    public void getAllRescuedAnimals(RepositoryCallback<List<RescuedAnimal>> callback) {
        api.getRescuedAnimals(null, null, null, PAGE_SIZE).enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void getFilteredRescuedAnimals(String breed, String region, String gender,
                                          RepositoryCallback<List<RescuedAnimal>> callback) {
        api.getRescuedAnimals(breed, region, gender, PAGE_SIZE).enqueue(ApiCallbacks.of(callback));
    }

    @Override
    public void getRescuedAnimalById(String id, RepositoryCallback<RescuedAnimal> callback) {
        api.getRescuedAnimal(id).enqueue(ApiCallbacks.of(callback));
    }
}
