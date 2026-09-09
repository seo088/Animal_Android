package com.animalloo.data.remote;

import com.animalloo.data.remote.dto.DemoPostResponse;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * 향후 공공데이터 API 연동을 위한 Retrofit 인터페이스.
 * 현재는 JSONPlaceholder 데모 엔드포인트를 사용합니다.
 */
public interface PublicDataApi {

    @GET("posts/1")
    Call<DemoPostResponse> getDemoPost();
}
