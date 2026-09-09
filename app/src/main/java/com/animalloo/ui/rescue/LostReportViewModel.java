package com.animalloo.ui.rescue;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.data.model.MatchResult;
import com.animalloo.data.model.UiState;
import com.animalloo.data.repository.LostAnimalRepository;
import com.animalloo.data.repository.RepositoryCallback;
import com.animalloo.util.RepositoryProvider;

import java.util.List;

public class LostReportViewModel extends ViewModel {

    private final LostAnimalRepository lostAnimalRepository;

    private final MutableLiveData<UiState<LostAnimalReport>> submitState = new MutableLiveData<>();
    private final MutableLiveData<UiState<List<MatchResult>>> matchResultsState = new MutableLiveData<>();
    private final MutableLiveData<String> validationError = new MutableLiveData<>();

    public LostReportViewModel() {
        lostAnimalRepository = RepositoryProvider.getInstance().getLostAnimalRepository();
    }

    public LiveData<UiState<LostAnimalReport>> getSubmitState() {
        return submitState;
    }

    public LiveData<UiState<List<MatchResult>>> getMatchResultsState() {
        return matchResultsState;
    }

    public LiveData<String> getValidationError() {
        return validationError;
    }

    public void clearValidationError() {
        validationError.setValue(null);
    }

    public void submitReport(LostAnimalReport report) {
        String validationMessage = validateReport(report);
        if (validationMessage != null) {
            validationError.setValue(validationMessage);
            return;
        }

        validationError.setValue(null);
        submitState.setValue(UiState.loading());
        matchResultsState.setValue(UiState.loading());

        lostAnimalRepository.submitReport(report, new RepositoryCallback<LostAnimalReport>() {
            @Override
            public void onSuccess(LostAnimalReport savedReport) {
                submitState.setValue(UiState.success(savedReport));
                findMatches(savedReport);
            }

            @Override
            public void onError(String message) {
                submitState.setValue(UiState.error(message));
                matchResultsState.setValue(UiState.error(message));
            }
        });
    }

    private void findMatches(LostAnimalReport report) {
        lostAnimalRepository.findMatches(report, new RepositoryCallback<List<MatchResult>>() {
            @Override
            public void onSuccess(List<MatchResult> data) {
                if (data == null || data.isEmpty()) {
                    matchResultsState.setValue(UiState.empty());
                } else {
                    matchResultsState.setValue(UiState.success(data));
                }
            }

            @Override
            public void onError(String message) {
                matchResultsState.setValue(UiState.error(message));
            }
        });
    }

    public void resetSubmissionState() {
        submitState.setValue(null);
        matchResultsState.setValue(null);
        validationError.setValue(null);
    }

    private String validateReport(LostAnimalReport report) {
        if (report.getName() == null || report.getName().trim().isEmpty()) {
            return "동물 이름을 입력해 주세요.";
        }
        if (report.getRegion() == null || report.getRegion().trim().isEmpty()) {
            return "실종 지역을 입력해 주세요.";
        }
        if (report.getLostDate() == null || report.getLostDate().trim().isEmpty()) {
            return "실종 날짜를 선택해 주세요.";
        }
        if (report.getPhotoPath() == null || report.getPhotoPath().trim().isEmpty()) {
            return "사진을 선택해 주세요.";
        }
        return null;
    }
}
