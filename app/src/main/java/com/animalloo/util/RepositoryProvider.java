package com.animalloo.util;

import android.app.Application;

import com.animalloo.data.mock.MockAuthRepository;
import com.animalloo.data.mock.MockAlertRepository;
import com.animalloo.data.mock.MockAnimalRepository;
import com.animalloo.data.mock.MockAsyncHelper;
import com.animalloo.data.mock.MockDiagnosisRepository;
import com.animalloo.data.mock.MockFavoriteRepository;
import com.animalloo.data.mock.MockFacilityRepository;
import com.animalloo.data.mock.MockHomeRepository;
import com.animalloo.data.mock.MockHospitalRepository;
import com.animalloo.data.mock.MockLostAnimalRepository;
import com.animalloo.BuildConfig;
import com.animalloo.data.remote.AnimalLooApi;
import com.animalloo.data.remote.AnimalLooApiClient;
import com.animalloo.data.remote.RemoteAlertRepository;
import com.animalloo.data.remote.RemoteAnimalRepository;
import com.animalloo.data.remote.RemoteAuthRepository;
import com.animalloo.data.remote.RemoteDiagnosisRepository;
import com.animalloo.data.remote.RemoteFacilityRepository;
import com.animalloo.data.remote.RemoteFavoriteRepository;
import com.animalloo.data.remote.RemoteHomeRepository;
import com.animalloo.data.remote.RemoteHospitalRepository;
import com.animalloo.data.remote.RemoteLostAnimalRepository;
import com.animalloo.data.remote.RetrofitPublicDataRepository;
import com.animalloo.data.remote.SessionStore;
import com.animalloo.data.repository.AuthRepository;
import com.animalloo.data.repository.PublicDataRepository;
import com.animalloo.data.repository.AlertRepository;
import com.animalloo.data.repository.AnimalRepository;
import com.animalloo.data.repository.DiagnosisRepository;
import com.animalloo.data.repository.FacilityRepository;
import com.animalloo.data.repository.FavoriteRepository;
import com.animalloo.data.repository.HomeRepository;
import com.animalloo.data.repository.HospitalRepository;
import com.animalloo.data.repository.LostAnimalRepository;

/**
 * Singleton provider for repository instances.
 * 기본은 backend/ 서버를 쓰는 Remote 구현체이며, USE_MOCK_API=true면 Mock 구현체를 사용합니다.
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
    private final AuthRepository authRepository;
    private final FavoriteRepository favoriteRepository;
    private final Application application;

    private RepositoryProvider(Application application) {
        this.application = application;
        publicDataRepository = new RetrofitPublicDataRepository();

        if (BuildConfig.USE_MOCK_API) {
            facilityRepository = new MockFacilityRepository();
            hospitalRepository = new MockHospitalRepository();
            animalRepository = new MockAnimalRepository();
            lostAnimalRepository = new MockLostAnimalRepository(application);
            diagnosisRepository = new MockDiagnosisRepository();
            alertRepository = new MockAlertRepository();
            homeRepository = new MockHomeRepository();
            authRepository = new MockAuthRepository(application);
            favoriteRepository = new MockFavoriteRepository(application);
            return;
        }

        SessionStore sessionStore = new SessionStore(application);
        AnimalLooApi api = AnimalLooApiClient.create(sessionStore);
        RemoteFavoriteRepository remoteFavorites = new RemoteFavoriteRepository(api, sessionStore, application);
        facilityRepository = new RemoteFacilityRepository(api, application);
        hospitalRepository = new RemoteHospitalRepository(api, application);
        animalRepository = new RemoteAnimalRepository(api);
        lostAnimalRepository = new RemoteLostAnimalRepository(api);
        diagnosisRepository = new RemoteDiagnosisRepository(api);
        alertRepository = new RemoteAlertRepository(api);
        homeRepository = new RemoteHomeRepository(api, sessionStore);
        authRepository = new RemoteAuthRepository(api, sessionStore, remoteFavorites::refresh);
        favoriteRepository = remoteFavorites;
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

    public AuthRepository getAuthRepository() {
        return authRepository;
    }

    public FavoriteRepository getFavoriteRepository() {
        return favoriteRepository;
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
