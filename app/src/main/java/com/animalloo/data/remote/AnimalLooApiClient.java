package com.animalloo.data.remote;

import com.animalloo.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Builds the Retrofit client for the AnimalLoo backend (BuildConfig.API_BASE_URL).
 * Attaches the stored JWT and drops the session when the server rejects it.
 */
public final class AnimalLooApiClient {

    private AnimalLooApiClient() {
    }

    public static AnimalLooApi create(SessionStore sessionStore) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    String token = sessionStore.getToken();
                    Request request = chain.request();
                    if (token != null) {
                        request = request.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                    }
                    Response response = chain.proceed(request);
                    if (response.code() == 401 && token != null) {
                        sessionStore.clear();
                    }
                    return response;
                })
                .addInterceptor(logging)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AnimalLooApi.class);
    }
}
