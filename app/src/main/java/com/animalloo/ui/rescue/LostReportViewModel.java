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

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class LostReportViewModel extends ViewModel {

    private final LostAnimalRepository lostAnimalRepository;

    private final MutableLiveData<UiState<LostAnimalReport>> submitState = new MutableLiveData<>();
    private final MutableLiveData<UiState<List<MatchResult>>> matchResultsState = new MutableLiveData<>();
    private final MutableLiveData<String> validationError = new MutableLiveData<>();

    private LostAnimalReport lastSubmittedReport;
    private boolean isSubmitting;
    private int submitRequestGeneration;
    private int matchRequestGeneration;

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

    public LostAnimalReport getLastSubmittedReport() {
        return lastSubmittedReport;
    }

    public boolean isSubmitting() {
        return isSubmitting;
    }

    public void clearValidationError() {
        validationError.setValue(null);
    }

    public void submitReport(LostAnimalReport report) {
        if (isSubmitting) {
            return;
        }

        String validationMessage = validateReport(report);
        if (validationMessage != null) {
            validationError.setValue(validationMessage);
            return;
        }

        validationError.setValue(null);
        isSubmitting = true;
        int generation = ++submitRequestGeneration;
        submitState.setValue(UiState.loading());

        lostAnimalRepository.submitReport(report, new RepositoryCallback<LostAnimalReport>() {
            @Override
            public void onSuccess(LostAnimalReport savedReport) {
                if (generation != submitRequestGeneration) {
                    return;
                }
                isSubmitting = false;
                lastSubmittedReport = savedReport;
                submitState.setValue(UiState.success(savedReport));
                findMatches(savedReport);
            }

            @Override
            public void onError(String message) {
                if (generation != submitRequestGeneration) {
                    return;
                }
                isSubmitting = false;
                submitState.setValue(UiState.error(message));
            }
        });
    }

    public void retryMatching() {
        if (lastSubmittedReport == null) {
            matchResultsState.setValue(UiState.error("신고 정보가 없어 매칭을 다시 시도할 수 없습니다."));
            return;
        }
        findMatches(lastSubmittedReport);
    }

    private void findMatches(LostAnimalReport report) {
        matchResultsState.setValue(UiState.loading());
        int generation = ++matchRequestGeneration;

        lostAnimalRepository.findMatches(report, new RepositoryCallback<List<MatchResult>>() {
            @Override
            public void onSuccess(List<MatchResult> data) {
                if (generation != matchRequestGeneration) {
                    return;
                }
                if (data == null || data.isEmpty()) {
                    matchResultsState.setValue(UiState.empty());
                } else {
                    matchResultsState.setValue(UiState.success(data));
                }
            }

            @Override
            public void onError(String message) {
                if (generation != matchRequestGeneration) {
                    return;
                }
                matchResultsState.setValue(UiState.error(message));
            }
        });
    }

    public void resetSubmissionState() {
        submitRequestGeneration++;
        matchRequestGeneration++;
        isSubmitting = false;
        submitState.setValue(null);
        matchResultsState.setValue(null);
        validationError.setValue(null);
    }

    private String validateReport(LostAnimalReport report) {
        if (report.getName() == null || report.getName().trim().isEmpty()) {
            return "동물 이름을 입력해 주세요.";
        }
        if (report.getBreed() == null || report.getBreed().trim().isEmpty()
                || "전체".equals(report.getBreed())) {
            return "품종을 선택해 주세요.";
        }
        if (report.getRegion() == null || report.getRegion().trim().isEmpty()) {
            return "실종 지역을 입력해 주세요.";
        }
        if (report.getLostDate() == null || report.getLostDate().trim().isEmpty()) {
            return "실종 날짜를 선택해 주세요.";
        }
        if (isFutureDate(report.getLostDate())) {
            return "실종 날짜는 오늘 이전이어야 합니다.";
        }
        if (report.getContactInfo() == null || report.getContactInfo().trim().isEmpty()) {
            return "연락 정보를 입력해 주세요.";
        }
        if (report.getPhotoPath() == null || report.getPhotoPath().trim().isEmpty()) {
            return "사진을 선택해 주세요.";
        }
        if (!new File(report.getPhotoPath()).exists()) {
            return "선택한 사진을 찾을 수 없습니다. 다시 선택해 주세요.";
        }
        return null;
    }

    private boolean isFutureDate(String dateText) {
        try {
            LocalDate lostDate = LocalDate.parse(dateText);
            return lostDate.isAfter(LocalDate.now());
        } catch (DateTimeParseException exception) {
            return true;
        }
    }
}
