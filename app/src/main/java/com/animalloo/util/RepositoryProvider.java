package com.animalloo.util;

import android.app.Application;

import com.animalloo.data.mock.MockAlertRepository;
import com.animalloo.data.mock.MockAnimalRepository;
import com.animalloo.data.mock.MockAsyncHelper;
import com.animalloo.data.mock.MockDiagnosisRepository;
import com.animalloo.data.mock.MockFacilityRepository;
import com.animalloo.data.mock.MockHomeRepository;
import com.animalloo.data.mock.MockHospitalRepository;
import com.animalloo.data.mock.MockLostAnimalRepository;
import com.animalloo.data.remote.RetrofitPublicDataRepository;
import com.animalloo.data.repository.PublicDataRepository;
import com.animalloo.data.repository.AlertRepository;
import com.animalloo.data.repository.AnimalRepository;
import com.animalloo.data.repository.DiagnosisRepository;
import com.animalloo.data.repository.FacilityRepository;
import com.animalloo.data.repository.HomeRepository;
import com.animalloo.data.repository.HospitalRepository;
import com.animalloo.data.repository.LostAnimalRepository;

/**
 * Singleton provider for repository instances.
 * Mock 구현체를 사용하며, 향후 Remote 구현체로 교체 가능합니다.
 */
public final class RepositoryProvider {

    private static RepositoryProvider instance;

    private final FacilityRepository facilityRepository;
    private final HospitalRepository hospitalRepository;
    private final AnimalRepository animalRepository;
    private final LostAnimalRepository lostAnimalRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final AlertRepository alertRepository;
    private final HomeRepository homeRepository;
    private final PublicDataRepository publicDataRepository;
    private final Application application;

    private RepositoryProvider(Application application) {
        this.application = application;
        facilityRepository = new MockFacilityRepository();
        hospitalRepository = new MockHospitalRepository();
        animalRepository = new MockAnimalRepository();
        lostAnimalRepository = new MockLostAnimalRepository();
        diagnosisRepository = new MockDiagnosisRepository();
        alertRepository = new MockAlertRepository();
        homeRepository = new MockHomeRepository();
        publicDataRepository = new RetrofitPublicDataRepository();
    }

    public static void init(Application application) {
        if (instance == null) {
            instance = new RepositoryProvider(application);
        }
    }

    public static RepositoryProvider getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RepositoryProvider is not initialized. Call init() in Application.");
        }
        return instance;
    }

    public FacilityRepository getFacilityRepository() {
        return facilityRepository;
    }

    public HospitalRepository getHospitalRepository() {
        return hospitalRepository;
    }

    public AnimalRepository getAnimalRepository() {
        return animalRepository;
    }

    public LostAnimalRepository getLostAnimalRepository() {
        return lostAnimalRepository;
    }

    public DiagnosisRepository getDiagnosisRepository() {
        return diagnosisRepository;
    }

    public AlertRepository getAlertRepository() {
        return alertRepository;
    }

    public HomeRepository getHomeRepository() {
        return homeRepository;
    }

    public PublicDataRepository getPublicDataRepository() {
        return publicDataRepository;
    }

    public android.content.Context getApplicationContext() {
        return application.getApplicationContext();
    }

    public static void shutdownIfInitialized() {
        if (instance != null) {
            MockAsyncHelper.getInstance().shutdown();
        }
    }
}
