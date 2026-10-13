package com.animalloo.data.remote;

import androidx.annotation.NonNull;

import com.animalloo.data.repository.RepositoryCallback;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Adapts Retrofit callbacks (delivered on the main thread) to RepositoryCallback,
 * turning the backend's {"detail": ...} errors into user-facing messages.
 */
public final class ApiCallbacks {

    private static final String NETWORK_ERROR = "서버에 연결할 수 없습니다. 네트워크 상태를 확인해 주세요.";
    private static final String UNKNOWN_ERROR = "요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.";
    // Gson 2.8.5 (via converter-gson) has no static JsonParser.parseString.
    private static final Gson GSON = new Gson();

    private ApiCallbacks() {
    }

    public interface Mapper<S, T> {
        T map(S source);
    }

    public static <T> Callback<T> of(RepositoryCallback<T> callback) {
        return map(callback, source -> source);
    }

    public static <S, T> Callback<S> map(RepositoryCallback<T> callback, Mapper<S, T> mapper) {
        return new Callback<S>() {
            @Override
            public void onResponse(@NonNull Call<S> call, @NonNull Response<S> response) {
                if (!response.isSuccessful()) {
                    callback.onError(errorMessage(response));
                    return;
                }
                T result;
                try {
                    result = mapper.map(response.body());
                } catch (RuntimeException exception) {
                    callback.onError(UNKNOWN_ERROR);
                    return;
                }
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(@NonNull Call<S> call, @NonNull Throwable throwable) {
                callback.onError(NETWORK_ERROR);
            }
        };
    }

    static String errorMessage(Response<?> response) {
        try (ResponseBody body = response.errorBody()) {
            if (body == null) {
                return UNKNOWN_ERROR;
            }
            JsonElement root = GSON.fromJson(body.string(), JsonElement.class);
            if (root == null) {
                return UNKNOWN_ERROR;
            }
            if (!root.isJsonObject()) {
                return UNKNOWN_ERROR;
            }
            JsonElement detail = root.getAsJsonObject().get("detail");
            if (detail != null && detail.isJsonPrimitive()) {
                return detail.getAsString();
            }
            if (detail != null && detail.isJsonArray()) {
                // FastAPI validation error: [{"loc": [...], "msg": "..."}]
                JsonArray errors = detail.getAsJsonArray();
                if (errors.size() > 0 && errors.get(0).isJsonObject()) {
                    JsonObject first = errors.get(0).getAsJsonObject();
                    if (first.has("msg")) {
                        return "입력값을 확인해 주세요. (" + first.get("msg").getAsString() + ")";
                    }
                }
            }
        } catch (Exception ignored) {
            // fall through to the generic message
        }
        return UNKNOWN_ERROR;
    }
}
