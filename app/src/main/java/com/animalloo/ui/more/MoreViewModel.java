package com.animalloo.ui.more;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.HomeProfile;
import com.animalloo.data.model.UiState;
import com.animalloo.data.model.User;
import com.animalloo.data.repository.AuthRepository;
import com.animalloo.data.repository.FavoriteRepository;
import com.animalloo.data.repository.HomeRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

public class MoreViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final HomeRepository homeRepository;
    private final FavoriteRepository favoriteRepository;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<UiState<HomeProfile>> profileState = new MutableLiveData<>();
    private final MutableLiveData<Integer> favoriteRescuedCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> logoutComplete = new MutableLiveData<>();

    public MoreViewModel() {
        RepositoryProvider provider = RepositoryProvider.getInstance();
        authRepository = provider.getAuthRepository();
        homeRepository = provider.getHomeRepository();
        favoriteRepository = provider.getFavoriteRepository();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<UiState<HomeProfile>> getProfileState() {
        return profileState;
    }

    public LiveData<Integer> getFavoriteRescuedCount() {
        return favoriteRescuedCount;
    }

    public LiveData<Boolean> getLogoutComplete() {
        return logoutComplete;
    }

    public void loadHubData() {
        currentUser.setValue(authRepository.getCurrentUser());
        loadProfile();
        refreshFavoriteCount();
    }

    public void refreshFavoriteCount() {
        int count = 0;
        for (String key : favoriteRepository.getFavoriteKeys()) {
            if (key.startsWith(FavoriteRepository.PREFIX_RESCUED_ANIMAL)) {
                count++;
            }
        }
        favoriteRescuedCount.setValue(count);
    }

    private void loadProfile() {
        profileState.setValue(UiState.loading());
        User user = authRepository.getCurrentUser();
        String displayName = user != null ? user.getDisplayName() : null;
        homeRepository.getHomeProfile(displayName, new RepositoryCallback<HomeProfile>() {
            @Override
            public void onSuccess(HomeProfile data) {
                profileState.setValue(UiState.success(data));
            }

            @Override
            public void onError(String message) {
                profileState.setValue(UiState.error(message));
            }
        });
    }

    public void logout() {
        authRepository.logout();
        logoutComplete.setValue(true);
    }
}
