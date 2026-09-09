package com.animalloo.ui.more;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.PublicDataInfo;
import com.animalloo.data.model.UiState;
import com.animalloo.data.repository.PublicDataRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

public class DataSourceViewModel extends ViewModel {

    private final PublicDataRepository publicDataRepository;
    private final MutableLiveData<UiState<PublicDataInfo>> publicDataState = new MutableLiveData<>();

    public DataSourceViewModel() {
        publicDataRepository = RepositoryProvider.getInstance().getPublicDataRepository();
    }

    public LiveData<UiState<PublicDataInfo>> getPublicDataState() {
        return publicDataState;
    }

    public void fetchDemoPublicData() {
        publicDataState.setValue(UiState.loading());
        publicDataRepository.fetchDemoPublicData(new RepositoryCallback<PublicDataInfo>() {
            @Override
            public void onSuccess(PublicDataInfo data) {
                if (data == null) {
                    publicDataState.setValue(UiState.empty());
                } else {
                    publicDataState.setValue(UiState.success(data));
                }
            }

            @Override
            public void onError(String message) {
                publicDataState.setValue(UiState.error(message));
            }
        });
    }
}
