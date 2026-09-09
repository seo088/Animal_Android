package com.animalloo.data.remote;

import com.animalloo.data.model.PublicDataInfo;
import com.animalloo.data.remote.dto.DemoPostResponse;
import com.animalloo.data.repository.PublicDataRepository;
import com.animalloo.data.repository.RepositoryCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Retrofit enqueue 기반 공공데이터 데모 Repository.
 * 실제 AnimalLoo 백엔드 연동 전 네트워크 스레드/콜백 구조를 시연합니다.
 */
public class RetrofitPublicDataRepository implements PublicDataRepository {

    private final PublicDataApi publicDataApi;

    public RetrofitPublicDataRepository() {
        publicDataApi = RetrofitClient.getPublicDataApi();
    }

    @Override
    public void fetchDemoPublicData(RepositoryCallback<PublicDataInfo> callback) {
        publicDataApi.getDemoPost().enqueue(new Callback<DemoPostResponse>() {
            @Override
            public void onResponse(Call<DemoPostResponse> call, Response<DemoPostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DemoPostResponse body = response.body();
                    PublicDataInfo info = new PublicDataInfo(
                            "JSONPlaceholder (데모 API)",
                            body.getTitle(),
                            body.getBody(),
                            System.currentTimeMillis());
                    callback.onSuccess(info);
                } else {
                    callback.onError("공공데이터 데모 응답을 처리할 수 없습니다. (HTTP "
                            + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<DemoPostResponse> call, Throwable t) {
                callback.onError("네트워크 오류: " + t.getMessage());
            }
        });
    }
}
