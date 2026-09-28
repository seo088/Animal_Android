package com.animalloo.data.remote;

import com.animalloo.data.model.PublicDataInfo;
import com.animalloo.data.remote.dto.DemoPostResponse;
import com.animalloo.data.repository.PublicDataRepository;
import com.animalloo.data.repository.RepositoryCallback;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Retrofit enqueue 기반 공공데이터 데모 Repository.
 * 실제 AnimalLoo 백엔드 연동 전 네트워크 스레드/콜백 구조를 시연합니다.
 */
public class RetrofitPublicDataRepository implements PublicDataRepository {

    private static final String DEMO_SOURCE_NAME = "JSONPlaceholder (데모 API)";

    private final PublicDataApi publicDataApi;

    public RetrofitPublicDataRepository() {
        publicDataApi = RetrofitClient.getPublicDataApi();
    }

    @Override
    public void fetchDemoPublicData(RepositoryCallback<PublicDataInfo> callback) {
        publicDataApi.getDemoPost().enqueue(new Callback<DemoPostResponse>() {
            @Override
            public void onResponse(Call<DemoPostResponse> call, Response<DemoPostResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onError("데모 API 응답을 처리할 수 없습니다. 잠시 후 다시 시도해 주세요.");
                    return;
                }

                DemoPostResponse body = response.body();
                if (body == null) {
                    callback.onSuccess(null);
                    return;
                }

                String title = body.getTitle() != null ? body.getTitle().trim() : "";
                String summary = body.getBody() != null ? body.getBody().trim() : "";
                if (title.isEmpty() && summary.isEmpty()) {
                    callback.onSuccess(null);
                    return;
                }

                PublicDataInfo info = new PublicDataInfo(
                        DEMO_SOURCE_NAME,
                        title.isEmpty() ? "(제목 없음)" : title,
                        summary.isEmpty() ? "(본문 없음)" : summary,
                        System.currentTimeMillis());
                callback.onSuccess(info);
            }

            @Override
            public void onFailure(Call<DemoPostResponse> call, Throwable t) {
                callback.onError(toUserFriendlyMessage(t));
            }
        });
    }

    private String toUserFriendlyMessage(Throwable throwable) {
        if (throwable instanceof UnknownHostException) {
            return "인터넷 연결을 확인한 후 다시 시도해 주세요.";
        }
        if (throwable instanceof SocketTimeoutException) {
            return "요청 시간이 초과되었습니다. 잠시 후 다시 시도해 주세요.";
        }
        if (throwable instanceof IOException) {
            return "네트워크 연결에 실패했습니다. 연결 상태를 확인해 주세요.";
        }
        return "데모 API 호출에 실패했습니다. 잠시 후 다시 시도해 주세요.";
    }
}
