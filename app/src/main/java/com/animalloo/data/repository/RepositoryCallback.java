package com.animalloo.data.repository;

/**
 * Async callback for repository operations.
 */
public interface RepositoryCallback<T> {

    void onSuccess(T data);

    void onError(String message);
}
