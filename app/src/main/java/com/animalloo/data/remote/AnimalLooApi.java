package com.animalloo.data.remote;

import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.DiagnosisResult;
import com.animalloo.data.model.Facility;
import com.animalloo.data.model.HomeStats;
import com.animalloo.data.model.Hospital;
import com.animalloo.data.model.LostAnimalReport;
import com.animalloo.data.model.MatchResult;
import com.animalloo.data.model.RescuedAnimal;
import com.animalloo.data.model.Symptom;
import com.animalloo.data.remote.dto.AuthResponse;
import com.animalloo.data.remote.dto.HomeProfileResponse;
import com.animalloo.data.remote.dto.InferRequest;
import com.animalloo.data.remote.dto.LoginRequest;
import com.animalloo.data.remote.dto.LostReportMatchRequest;
import com.animalloo.data.remote.dto.SignUpRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * AnimalLoo backend (backend/ in this repo). Null query parameters are omitted.
 * Response JSON fields match the app models, so most calls return them directly.
 */
public interface AnimalLooApi {

    // --- 로그인 ---
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest body);

    @POST("auth/signup")
    Call<AuthResponse> signUp(@Body SignUpRequest body);

    // --- 홈 대시보드 ---
    @GET("home/stats")
    Call<HomeStats> getHomeStats();

    @GET("home/profile")
    Call<HomeProfileResponse> getHomeProfile();

    @GET("alerts")
    Call<List<AlertNotification>> getAlerts(@Query("limit") Integer limit);

    @GET("alerts/{id}")
    Call<AlertNotification> getAlert(@Path("id") String id);

    // --- 시설 지도 / 동반시설 ---
    @GET("facilities")
    Call<List<Facility>> getFacilities(@Query("category") String category,
                                       @Query("lat") Double lat,
                                       @Query("lng") Double lng,
                                       @Query("radiusKm") Double radiusKm,
                                       @Query("limit") Integer limit);

    @GET("facilities/pet-friendly")
    Call<List<Facility>> getPetFriendlyFacilities(@Query("petType") String petType,
                                                  @Query("size") String size,
                                                  @Query("indoor") Boolean indoor,
                                                  @Query("carrier") Boolean carrier,
                                                  @Query("leash") Boolean leash,
                                                  @Query("lat") Double lat,
                                                  @Query("lng") Double lng,
                                                  @Query("radiusKm") Double radiusKm,
                                                  @Query("limit") Integer limit);

    @GET("facilities/{id}")
    Call<Facility> getFacility(@Path("id") String id);

    // --- 증상 확인 ---
    @GET("hospitals")
    Call<List<Hospital>> getHospitals(@Query("lat") Double lat,
                                      @Query("lng") Double lng,
                                      @Query("radiusKm") Double radiusKm,
                                      @Query("limit") Integer limit);

    @GET("hospitals/nearby")
    Call<List<Hospital>> getNearbyHospitals(@Query("lat") double lat,
                                            @Query("lng") double lng,
                                            @Query("radiusKm") Double radiusKm,
                                            @Query("limit") Integer limit);

    @GET("hospitals/{id}")
    Call<Hospital> getHospital(@Path("id") String id);

    @GET("symptoms")
    Call<List<Symptom>> getSymptoms();

    @POST("diagnosis/infer")
    Call<List<DiagnosisResult>> inferDisease(@Body InferRequest body);

    @GET("diagnosis/{id}")
    Call<DiagnosisResult> getDiagnosis(@Path("id") String id);

    // --- 구조동물 ---
    @GET("animals")
    Call<List<RescuedAnimal>> getRescuedAnimals(@Query("breed") String breed,
                                                @Query("region") String region,
                                                @Query("gender") String gender,
                                                @Query("size") Integer size);

    @GET("animals/{id}")
    Call<RescuedAnimal> getRescuedAnimal(@Path("id") String id);

    // --- 분실신고 ---
    @Multipart
    @POST("lost-reports")
    Call<LostAnimalReport> submitLostReport(@PartMap Map<String, RequestBody> fields,
                                            @Part MultipartBody.Part photo);

    @POST("lost-reports/matches")
    Call<List<MatchResult>> findMatches(@Body LostReportMatchRequest body);

    @GET("lost-reports/{id}")
    Call<LostAnimalReport> getLostReport(@Path("id") String id);

    // --- 즐겨찾기 ---
    @GET("favorites")
    Call<List<String>> getFavorites();

    @PUT("favorites/{key}")
    Call<Void> addFavorite(@Path("key") String key);

    @DELETE("favorites/{key}")
    Call<Void> removeFavorite(@Path("key") String key);
}
