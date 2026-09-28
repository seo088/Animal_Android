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

    private boolean isFetching;
    private int requestGeneration;

    public DataSourceViewModel() {
        publicDataRepository = RepositoryProvider.getInstance().getPublicDataRepository();
    }

    public LiveData<UiState<PublicDataInfo>> getPublicDataState() {
        return publicDataState;
    }

    public boolean isFetching() {
        return isFetching;
    }

    public void fetchDemoPublicData() {
        if (isFetching) {
            return;
        }

        isFetching = true;
        int generation = ++requestGeneration;
        publicDataState.setValue(UiState.loading());

        publicDataRepository.fetchDemoPublicData(new RepositoryCallback<PublicDataInfo>() {
            @Override
            public void onSuccess(PublicDataInfo data) {
                if (generation != requestGeneration) {
                    return;
                }
                isFetching = false;
                if (data == null) {
                    publicDataState.setValue(UiState.empty());
                } else {
                    publicDataState.setValue(UiState.success(data));
                }
            }

            @Override
            public void onError(String message) {
                if (generation != requestGeneration) {
                    return;
                }
                isFetching = false;
                publicDataState.setValue(UiState.error(message));
            }
        });
    }
}
