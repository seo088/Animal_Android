package com.animalloo.data.mock;

import android.os.Handler;
import android.os.Looper;

import com.animalloo.data.repository.RepositoryCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared async helper: ExecutorService (background) → Handler (main thread) → callback.
 */
public final class MockAsyncHelper {

    private static final long MOCK_DELAY_MS = 350L;

    private static MockAsyncHelper instance;

    private final ExecutorService executorService;
    private final Handler mainHandler;

    private MockAsyncHelper() {
        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized MockAsyncHelper getInstance() {
        if (instance == null) {
            instance = new MockAsyncHelper();
        }
        return instance;
    }

    public <T> void execute(MockTask<T> task, RepositoryCallback<T> callback) {
        executorService.execute(() -> {
            try {
                Thread.sleep(MOCK_DELAY_MS);
                T result = task.run();
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess(result);
                    }
                });
            } catch (Exception exception) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(exception.getMessage() != null
                                ? exception.getMessage()
                                : "알 수 없는 오류가 발생했습니다.");
                    }
                });
            }
        });
    }

    public interface MockTask<T> {
        T run() throws Exception;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
