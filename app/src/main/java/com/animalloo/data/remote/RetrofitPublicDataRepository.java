package com.animalloo.data.remote;

import com.animalloo.data.model.PublicDataInfo;
import com.animalloo.data.remote.dto.PublicDataStatusResponse;
import com.animalloo.data.repository.PublicDataRepository;
import com.animalloo.data.repository.RepositoryCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AnimalLoo 백엔드가 보유한 공공데이터의 실제 상태를 조회합니다.
 */
public class RetrofitPublicDataRepository implements PublicDataRepository {

    private final AnimalLooApi api;

    public RetrofitPublicDataRepository(AnimalLooApi api) {
        this.api = api;
    }

    @Override
    public void fetchPublicDataStatus(RepositoryCallback<PublicDataInfo> callback) {
        api.getPublicDataStatus().enqueue(new Callback<PublicDataStatusResponse>() {
            @Override
            public void onResponse(Call<PublicDataStatusResponse> call, Response<PublicDataStatusResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onError("공공데이터 상태를 확인할 수 없습니다. 잠시 후 다시 시도해 주세요.");
                    return;
                }

                PublicDataStatusResponse body = response.body();
                if (body == null) {
                    callback.onSuccess(null);
                    return;
                }

                PublicDataInfo info = new PublicDataInfo(
                        "AnimalLoo 백엔드 공공데이터",
                        String.format("시설 %d건 · 구조 %d건 · 분실 %d건",
                                body.getFacilityCount(), body.getRescuedAnimalCount(), body.getPublicLostReportCount()),
                        String.format("최근 구조: %s\n최근 분실: %s\n자동 동기화: %s",
                                dateOrUnknown(body.getLatestRescuedDate()), dateOrUnknown(body.getLatestLostDate()),
                                body.isPublicDataSyncConfigured() ? "설정됨" : "미설정"),
                        System.currentTimeMillis());
                callback.onSuccess(info);
            }

            @Override
            public void onFailure(Call<PublicDataStatusResponse> call, Throwable t) {
                callback.onError("AnimalLoo 백엔드에 연결하지 못했습니다. 서버 주소와 실행 상태를 확인해 주세요.");
            }
        });
    }

    private String dateOrUnknown(String date) {
        return date == null || date.trim().isEmpty() ? "없음" : date;
    }
}
