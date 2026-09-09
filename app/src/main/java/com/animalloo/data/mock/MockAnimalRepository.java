package com.animalloo.data.mock;

import com.animalloo.data.model.RescuedAnimal;
import com.animalloo.data.repository.AnimalRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class MockAnimalRepository implements AnimalRepository {

    private final MockDataProvider dataProvider;
    private final MockAsyncHelper asyncHelper;

    public MockAnimalRepository() {
        dataProvider = MockDataProvider.getInstance();
        asyncHelper = MockAsyncHelper.getInstance();
    }

    @Override
    public void getAllRescuedAnimals(RepositoryCallback<List<RescuedAnimal>> callback) {
        asyncHelper.execute(() -> new ArrayList<>(dataProvider.getRescuedAnimals()), callback);
    }

    @Override
    public void getFilteredRescuedAnimals(String breed, String region, String gender,
                                          RepositoryCallback<List<RescuedAnimal>> callback) {
        asyncHelper.execute(() -> {
            List<RescuedAnimal> filtered = new ArrayList<>();
            for (RescuedAnimal animal : dataProvider.getRescuedAnimals()) {
                if (!matchesFilter(animal.getBreed(), breed)) {
                    continue;
                }
                if (!matchesFilter(animal.getRegion(), region)) {
                    continue;
                }
                if (!matchesFilter(animal.getGender(), gender)) {
                    continue;
                }
                filtered.add(animal);
            }
            return filtered;
        }, callback);
    }

    @Override
    public void getRescuedAnimalById(String id, RepositoryCallback<RescuedAnimal> callback) {
        asyncHelper.execute(() -> {
            RescuedAnimal animal = dataProvider.findRescuedAnimalById(id);
            if (animal == null) {
                throw new IllegalStateException("구조동물 정보를 찾을 수 없습니다.");
            }
            return animal;
        }, callback);
    }

    private boolean matchesFilter(String value, String filter) {
        if (filter == null || "전체".equals(filter) || filter.isEmpty()) {
            return true;
        }
        return filter.equals(value);
    }
}
