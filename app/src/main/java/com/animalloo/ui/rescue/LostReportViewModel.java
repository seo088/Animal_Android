package com.animalloo.ui.rescue;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.animalloo.R;
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

public class LostReportViewModel extends AndroidViewModel {

    public static final class FieldValidationError {
        private final String message;
        private final int focusViewId;

        public FieldValidationError(String message, int focusViewId) {
            this.message = message;
            this.focusViewId = focusViewId;
        }

        public String getMessage() {
            return message;
        }

        public int getFocusViewId() {
            return focusViewId;
        }
    }

    private final LostAnimalRepository lostAnimalRepository;

    private final MutableLiveData<UiState<LostAnimalReport>> submitState = new MutableLiveData<>();
    private final MutableLiveData<UiState<List<MatchResult>>> matchResultsState = new MutableLiveData<>();
    private final MutableLiveData<FieldValidationError> validationError = new MutableLiveData<>();

    private LostAnimalReport lastSubmittedReport;
    private boolean isSubmitting;
    private boolean restoredLastReport;
    private int submitRequestGeneration;
    private int matchRequestGeneration;

    public LostReportViewModel(@NonNull Application application) {
        super(application);
        lostAnimalRepository = RepositoryProvider.getInstance().getLostAnimalRepository();
    }

    public LiveData<UiState<LostAnimalReport>> getSubmitState() {
        return submitState;
    }

    public LiveData<UiState<List<MatchResult>>> getMatchResultsState() {
        return matchResultsState;
    }

    public LiveData<FieldValidationError> getValidationError() {
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

        FieldValidationError validationIssue = validateReport(report);
        if (validationIssue != null) {
            validationError.setValue(validationIssue);
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
                LastLostReportStore.save(getApplication(), savedReport.getId());
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
        loadSavedMatches(lastSubmittedReport);
    }

    public void restoreLastReport() {
        if (restoredLastReport) {
            return;
        }
        restoredLastReport = true;
        String reportId = LastLostReportStore.get(getApplication());
        if (reportId == null || reportId.trim().isEmpty()) {
            return;
        }
        lostAnimalRepository.getReportById(reportId, new RepositoryCallback<LostAnimalReport>() {
            @Override
            public void onSuccess(LostAnimalReport report) {
                lastSubmittedReport = report;
                loadSavedMatches(report);
            }

            @Override
            public void onError(String message) {
                LastLostReportStore.clear(getApplication());
            }
        });
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

    private void loadSavedMatches(LostAnimalReport report) {
        if (report == null || report.getId() == null || report.getId().trim().isEmpty()) {
            findMatches(report);
            return;
        }
        matchResultsState.setValue(UiState.loading());
        int generation = ++matchRequestGeneration;
        lostAnimalRepository.getSavedReportMatches(report.getId(), new RepositoryCallback<List<MatchResult>>() {
            @Override
            public void onSuccess(List<MatchResult> data) {
                if (generation != matchRequestGeneration) {
                    return;
                }
                matchResultsState.setValue(data == null || data.isEmpty()
                        ? UiState.empty() : UiState.success(data));
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

    @Nullable
    private FieldValidationError validateReport(LostAnimalReport report) {
        if (report.getName() == null || report.getName().trim().isEmpty()) {
            return new FieldValidationError("동물 이름을 입력해 주세요.", R.id.et_animal_name);
        }
        if (report.getBreed() == null || report.getBreed().trim().isEmpty()
                || "전체".equals(report.getBreed())) {
            return new FieldValidationError("품종을 선택해 주세요.", R.id.actv_breed);
        }
        if (report.getRegion() == null || report.getRegion().trim().isEmpty()) {
            return new FieldValidationError("실종 지역을 입력해 주세요.", R.id.et_region);
        }
        if (report.getLostDate() == null || report.getLostDate().trim().isEmpty()) {
            return new FieldValidationError("실종 날짜를 선택해 주세요.", R.id.et_lost_date);
        }
        if (isFutureDate(report.getLostDate())) {
            return new FieldValidationError("실종 날짜는 오늘 이전이어야 합니다.", R.id.et_lost_date);
        }
        if (report.getContactInfo() == null || report.getContactInfo().trim().isEmpty()) {
            return new FieldValidationError("연락 정보를 입력해 주세요.", R.id.et_contact);
        }
        if (report.getPhotoPath() == null || report.getPhotoPath().trim().isEmpty()) {
            return new FieldValidationError("사진을 선택해 주세요.", R.id.btn_select_photo);
        }
        if (!new File(report.getPhotoPath()).exists()) {
            return new FieldValidationError(
                    "선택한 사진을 찾을 수 없습니다. 다시 선택해 주세요.",
                    R.id.btn_select_photo);
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
