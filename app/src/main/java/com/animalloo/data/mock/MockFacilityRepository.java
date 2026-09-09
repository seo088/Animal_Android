package com.animalloo.data.mock;

import com.animalloo.data.model.Facility;
import com.animalloo.data.model.FacilityCategory;
import com.animalloo.data.model.PetFriendlyFilter;
import com.animalloo.data.repository.FacilityRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class MockFacilityRepository implements FacilityRepository {

    private final MockDataProvider dataProvider;
    private final MockAsyncHelper asyncHelper;

    public MockFacilityRepository() {
        dataProvider = MockDataProvider.getInstance();
        asyncHelper = MockAsyncHelper.getInstance();
    }

    @Override
    public void getAllFacilities(RepositoryCallback<List<Facility>> callback) {
        asyncHelper.execute(() -> new ArrayList<>(dataProvider.getFacilities()), callback);
    }

    @Override
    public void getFacilitiesByCategory(FacilityCategory category,
                                        RepositoryCallback<List<Facility>> callback) {
        asyncHelper.execute(() -> {
            List<Facility> filtered = new ArrayList<>();
            for (Facility facility : dataProvider.getFacilities()) {
                if (facility.getCategory() == category) {
                    filtered.add(facility);
                }
            }
            return filtered;
        }, callback);
    }

    @Override
    public void getPetFriendlyFacilities(PetFriendlyFilter filter,
                                         RepositoryCallback<List<Facility>> callback) {
        asyncHelper.execute(() -> {
            List<Facility> filtered = new ArrayList<>();
            for (Facility facility : dataProvider.getFacilities()) {
                if (!facility.isPetFriendly()) {
                    continue;
                }
                if (!matchesPetType(facility, filter.getPetType())) {
                    continue;
                }
                if (!matchesSizeLimit(facility, filter.getSizeLimit())) {
                    continue;
                }
                if (filter.getIndoorAllowed() != null
                        && facility.isIndoorAllowed() != filter.getIndoorAllowed()) {
                    continue;
                }
                if (filter.getCarrierRequired() != null
                        && facility.isCarrierRequired() != filter.getCarrierRequired()) {
                    continue;
                }
                if (filter.getLeashRequired() != null
                        && facility.isLeashRequired() != filter.getLeashRequired()) {
                    continue;
                }
                filtered.add(facility);
            }
            return filtered;
        }, callback);
    }

    @Override
    public void getFacilityById(String id, RepositoryCallback<Facility> callback) {
        asyncHelper.execute(() -> {
            Facility facility = dataProvider.findFacilityById(id);
            if (facility == null) {
                throw new IllegalStateException("시설 정보를 찾을 수 없습니다.");
            }
            return facility;
        }, callback);
    }

    private boolean matchesPetType(Facility facility, String petType) {
        if (petType == null || "전체".equals(petType)) {
            return true;
        }
        return "전체".equals(facility.getAllowedPetType())
                || petType.equals(facility.getAllowedPetType());
    }

    private boolean matchesSizeLimit(Facility facility, String sizeLimit) {
        if (sizeLimit == null || "전체".equals(sizeLimit)) {
            return true;
        }
        return "전체".equals(facility.getSizeLimit())
                || sizeLimit.equals(facility.getSizeLimit());
    }
}
