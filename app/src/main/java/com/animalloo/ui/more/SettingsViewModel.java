package com.animalloo.ui.more;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.User;
import com.animalloo.data.repository.AuthRepository;
import com.animalloo.util.RepositoryProvider;

public class SettingsViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutComplete = new MutableLiveData<>();

    public SettingsViewModel() {
        authRepository = RepositoryProvider.getInstance().getAuthRepository();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getLogoutComplete() {
        return logoutComplete;
    }

    public void loadAccount() {
        currentUser.setValue(authRepository.getCurrentUser());
    }

    public void logout() {
        authRepository.logout();
        logoutComplete.setValue(true);
    }
}
